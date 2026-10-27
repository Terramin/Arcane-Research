package arcaneresearch.compat.jei.arcane;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ArcaneWorkbenchCategory implements IRecipeCategory<IArcaneRecipe> {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_workbench");

    private static final ResourceLocation OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
                    "textures/gui/gui_researchbook_overlay.png");
    private static final int WIDTH = 120;
    private static final int HEIGHT = 200;
    private static final int OX = 60;
    private static final int OY = 100;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public ArcaneWorkbenchCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.ARCANE_WORKBENCH.get()));
        this.title = Component.translatable("gui.arcaneresearch.arcane_workbench");
    }

    @Override
    public @NotNull RecipeType<IArcaneRecipe> getRecipeType() {
        return ArcaneresearchJEIPlugin.ARCANE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull IArcaneRecipe recipe, @NotNull IFocusGroup focuses) {
        Minecraft mc = Minecraft.getInstance();
        RegistryAccess registryAccess = mc.level != null
                ? mc.level.registryAccess()
                : RegistryAccess.EMPTY;

        ItemStack result = recipe.getResultItem(registryAccess);
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 85)
                .addItemStack(result);

        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int limit = Math.min(ingredients.size(), 9);
        for (int i = 0; i < limit; i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient.isEmpty()) {
                continue;
            }
            int gridX = i % 3;
            int gridY = i / 3;
            builder.addSlot(RecipeIngredientRole.INPUT,
                            OX - 41 + gridX * 32, OY - 41 + gridY * 32)
                    .addIngredients(ingredient);
        }

        AspectList crystals = recipe.getCrystals();
        if (crystals != null && crystals.size() > 0) {
            Aspect[] aspects = crystals.getAspects();
            if (aspects != null) {
                int sz = crystals.size();
                int a = 0;
                for (Aspect aspect : aspects) {
                    if (aspect == null) {
                        a++;
                        continue;
                    }
                    int amount = crystals.getAmount(aspect);
                    ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect, amount);
                    if (crystal != null && !crystal.isEmpty()) {
                        builder.addSlot(RecipeIngredientRole.INPUT,
                                        OX + 3 - sz * 10 + a * 20, OY + 58)
                                .addItemStack(crystal);
                    }
                    a++;
                }
            }
        }
    }

    @Override
    public void draw(@NotNull IArcaneRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        var pose = guiGraphics.pose();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        pose.pushPose();
        try {
            pose.translate(OX, OY, 0);
            pose.scale(2f, 2f, 1f);
            guiGraphics.blit(OVERLAY_TEXTURE, -26, -26, 112, 15, 52, 52);
            guiGraphics.blit(OVERLAY_TEXTURE, -8, -46, 20, 3, 16, 16);
        } finally {
            pose.popPose();
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 0.4f);
        pose.pushPose();
        try {
            pose.translate(OX, OY, 0);
            pose.scale(2f, 2f, 1f);
            guiGraphics.blit(OVERLAY_TEXTURE, -6, 40, 68, 76, 12, 12);
        } finally {
            pose.popPose();
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        int visCost = recipe.getVis();
        if (visCost > 0) {
            guiGraphics.drawCenteredString(font, "" + visCost, OX, OY + 90, 0x8080FF);
        }

        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull IArcaneRecipe recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> lines = JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH);
        if (!lines.isEmpty()) {
            tooltip.addAll(lines);
        }
    }
}