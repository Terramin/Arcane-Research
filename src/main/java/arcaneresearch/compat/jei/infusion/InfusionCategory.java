package arcaneresearch.compat.jei.infusion;

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
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class InfusionCategory implements IRecipeCategory<InfusionRecipeType> {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "infusion");

    private static final ResourceLocation OVERLAY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
                    "textures/gui/gui_researchbook_overlay.png");
    private static final int WIDTH = 140;
    private static final int HEIGHT = 205;
    private static final int OX = 75;
    private static final int OY = 95;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    private InfusionRecipeType cachedRecipe;
    private List<AspectSpot> cachedSpots = List.of();

    public InfusionCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.INFUSION_MATRIX.get()));
        this.title = Component.translatable("gui.arcaneresearch.infusion");
    }

    private static List<AspectSpot> layoutAspects(AspectList aspects) {
        List<AspectSpot> spots = new ArrayList<>();
        if (aspects == null || aspects.aspects.isEmpty()) {
            return spots;
        }
        Aspect[] sorted = aspects.getAspectsSortedByName();
        if (sorted == null || sorted.length == 0) {
            return spots;
        }
        int size = aspects.size();
        int rows = (size - 1) / 5;
        int shift = (5 - size % 5) * 10;
        int sx = OX - 48;
        int sy = OY + 50 - 10 * rows;
        int total = 0;
        for (Aspect tag : sorted) {
            if (tag == null) {
                continue;
            }
            int m = 0;
            if (total / 5 >= rows && (rows > 1 || size < 5)) {
                m = 1;
            }
            int vx = sx + total % 5 * 20 + shift * m;
            int vy = sy + total / 5 * 20;
            spots.add(new AspectSpot(tag, aspects.getAmount(tag), vx, vy));
            total++;
        }
        return spots;
    }

    private List<AspectSpot> spotsFor(InfusionRecipeType recipe) {
        if (recipe == null) {
            return List.of();
        }
        if (cachedRecipe != recipe) {
            cachedRecipe = recipe;
            cachedSpots = layoutAspects(recipe.getAspects());
        }
        return cachedSpots;
    }

    @Override
    public @NotNull RecipeType<InfusionRecipeType> getRecipeType() {
        return ArcaneresearchJEIPlugin.INFUSION_TYPE;
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull InfusionRecipeType recipe, @NotNull IFocusGroup focuses) {
        Minecraft mc = Minecraft.getInstance();
        RegistryAccess registryAccess = mc.level != null
                ? mc.level.registryAccess()
                : RegistryAccess.EMPTY;

        ItemStack result = recipe.getResultItem(registryAccess);
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 86)
                .addItemStack(result);

        Ingredient centralItem = recipe.getCentralItem();
        if (centralItem != null && !centralItem.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, OX - 9, OY - 17)
                    .addIngredients(centralItem);
        }

        NonNullList<Ingredient> components = recipe.getComponents();
        if (components == null) {
            return;
        }
        int numComponents = components.size();
        if (numComponents == 0) {
            return;
        }
        int radius = 40;
        for (int i = 0; i < numComponents; i++) {
            Ingredient component = components.get(i);
            if (component.isEmpty()) {
                continue;
            }
            double angle = (2 * Math.PI * i / numComponents) - Math.PI / 2;
            int compX = OX + (int) (Math.cos(angle) * radius) - 9;
            int compY = (OY - 8) + (int) (Math.sin(angle) * radius) - 9;
            builder.addSlot(RecipeIngredientRole.INPUT, compX, compY)
                    .addIngredients(component);
        }
    }

    @Override
    public void draw(@NotNull InfusionRecipeType recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        if (background != null) {
            background.draw(guiGraphics, 0, 0);
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        var pose = guiGraphics.pose();
        pose.pushPose();
        try {
            pose.translate(OX, OY + 20, 0);
            pose.scale(2f, 2f, 1f);
            guiGraphics.blit(OVERLAY_TEXTURE, -28, -56, 0, 3, 56, 17);
            guiGraphics.blit(OVERLAY_TEXTURE, -28, -36, 200, 77, 60, 44);
        } finally {
            pose.popPose();
        }

        for (AspectSpot spot : spotsFor(recipe)) {
            AspectRenderer.drawAspect(guiGraphics, spot.x(), spot.y(), spot.aspect(), spot.amount());
        }

        int instability = recipe.getInstability();
        if (instability > 0) {
            String instText = "Instability: " + instability;
            int instColor = instability > 3 ? 0xFF6060 : (instability > 1 ? 0xFFFF60 : 0x60FF60);
            guiGraphics.drawCenteredString(font, instText, OX, OY + 94, instColor);
        }

        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull InfusionRecipeType recipe,
                           @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        for (AspectSpot spot : spotsFor(recipe)) {
            if (AspectRenderer.isMouseOverAspect(spot.x(), spot.y(), (int) mouseX, (int) mouseY)) {
                List<Component> lines = AspectRenderer.getAspectTooltip(spot.aspect(), spot.amount());
                if (!lines.isEmpty()) {
                    tooltip.addAll(lines);
                }
                return;
            }
        }
        List<Component> lines = JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH);
        if (!lines.isEmpty()) {
            tooltip.addAll(lines);
        }
    }

    private record AspectSpot(Aspect aspect, int amount, int x, int y) {
    }
}