package arcaneresearch.compat.jei.arcane;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import mezz.jei.api.gui.builder.ITooltipBuilder; // NEOFORGE_PORT: getTooltipStrings -> getTooltip
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipelineExtractor
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
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

public class ArcaneWorkbenchCategory implements IRecipeCategory<IArcaneRecipe> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_workbench");

    private static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");

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
    public RecipeType<IArcaneRecipe> getRecipeType() {
        return ArcaneresearchJEIPlugin.ARCANE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    // NEOFORGE_PORT: IRecipeCategory.getBackground removed in JEI 26.3;
    // getWidth/getHeight are mandatory instead (background field kept unused).
    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IArcaneRecipe recipe, IFocusGroup focuses) {
        // NEOFORGE_PORT: Neo IArcaneRecipe.getResultItem() is no-arg.
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 85)
                .addItemStack(recipe.getResultItem());

        // NEOFORGE_PORT: IArcaneRecipe.getIngredients() is List (not NonNullList) on 26.3.
        java.util.List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size() && i < 9; i++) {
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
            int a = 0;
            int sz = crystals.size();
            for (Aspect aspect : crystals.getAspects()) {
                int amount = crystals.getAmount(aspect);
                ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect, amount);
                if (!crystal.isEmpty()) {
                    builder.addSlot(RecipeIngredientRole.INPUT,
                                    OX + 3 - sz * 10 + a * 20, OY + 58)
                            .addItemStack(crystal);
                }
                a++;
            }
        }
    }

    @Override
    public void draw(IArcaneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -26, -26, 112, 15, 52, 52, 512, 512);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -8, -46, 20, 3, 16, 16, 512, 512);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -6, 40, 68, 76, 12, 12, 512, 512);
        guiGraphics.pose().popMatrix();

        int visCost = recipe.getVis();
        if (visCost > 0) {
            guiGraphics.centeredText(font, "" + visCost, OX, OY + 90, 0xFF8080FF);
        }

        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, IArcaneRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }
}
