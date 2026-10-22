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
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import mezz.jei.api.gui.builder.ITooltipBuilder; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
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

import java.util.Optional;

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
        
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 85)
                .addItemStack(recipe.getResultItem());

        
        
        
        
        java.util.List<Optional<Ingredient>> ingredients = gridIngredients(recipe);
        for (int i = 0; i < ingredients.size() && i < 9; i++) {
            Optional<Ingredient> ingredient = ingredients.get(i);
            if (ingredient.isEmpty() || ingredient.get().isEmpty()) {
                continue;
            }
            int gridX = i % 3;
            int gridY = i / 3;
            builder.addSlot(RecipeIngredientRole.INPUT,
                            OX - 41 + gridX * 32, OY - 41 + gridY * 32)
                    .addIngredients(ingredient.get());
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

    
    
    
    
    private static final int[] WAND_GRID = {2, 6, 4};
    private static final int[] SCEPTRE_GRID = {1, 5, 6, 4, 2};

    private static java.util.List<Optional<Ingredient>> gridIngredients(IArcaneRecipe recipe) {
        if (recipe instanceof arcaneresearch.common.lib.crafting.ShapedArcaneRecipe shaped) {
            return shaped.getOptionalIngredients();
        }
        if (recipe instanceof arcaneresearch.api.crafting.ShapedArcaneRecipe shaped) {
            return shaped.getOptionalIngredients();
        }
        java.util.List<Ingredient> flat = recipe.getIngredients();
        if (recipe instanceof arcaneresearch.common.lib.crafting.ArcaneWandDynamicRecipe) {
            int[] cells = flat.size() == WAND_GRID.length ? WAND_GRID
                    : (flat.size() == SCEPTRE_GRID.length ? SCEPTRE_GRID : null);
            if (cells != null) {
                java.util.List<Optional<Ingredient>> grid =
                        new java.util.ArrayList<>(java.util.Collections.nCopies(9, Optional.<Ingredient>empty()));
                for (int k = 0; k < cells.length; k++) {
                    grid.set(cells[k], Optional.ofNullable(flat.get(k)));
                }
                return grid;
            }
        }
        java.util.List<Optional<Ingredient>> grid = new java.util.ArrayList<>(flat.size());
        for (Ingredient ingredient : flat) {
            grid.add(Optional.ofNullable(ingredient));
        }
        return grid;
    }

    @Override
    public void draw(IArcaneRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        
        
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY);
        guiGraphics.pose().scale(2f, 2f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -26, -26, 112, 15, 52, 52, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -8, -46, 20, 3, 16, 16, 256, 256);
        guiGraphics.pose().popMatrix();

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY);
        guiGraphics.pose().scale(2f, 2f);
        
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -6, 40, 68, 76, 12, 12, 256, 256, 0x66FFFFFF);
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

    
    
    @Override
    public Identifier getIdentifier(IArcaneRecipe recipe) {
        return ArcaneresearchJEIPlugin.recipeId(recipe);
    }
}
