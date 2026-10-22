package arcaneresearch.compat.jei.crucible;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import mezz.jei.api.gui.builder.ITooltipBuilder; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class CrucibleCategory implements IRecipeCategory<CrucibleRecipeType> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crucible");

    private static final Identifier OVERLAY_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");

    private static final int WIDTH = 140;
    private static final int HEIGHT = 130;

    private static final int OX = 70;
    private static final int OY = 60;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public CrucibleCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CRUCIBLE.get()));
        this.title = Component.translatable("gui.arcaneresearch.crucible");
    }

    private static List<AspectSpot> layoutAspects(AspectList aspects) {
        List<AspectSpot> spots = new ArrayList<>();
        if (aspects == null || aspects.size() == 0) {
            return spots;
        }
        int total = 0;
        int rows = (aspects.size() - 1) / 3;
        int shift = (3 - aspects.size() % 3) * 10;
        int sx = OX - 28;
        int sy = OY + 8 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = 0;
            if (total / 3 >= rows && (rows > 1 || aspects.size() < 3)) {
                m = 1;
            }
            int vx = sx + total % 3 * 20 + shift * m;
            int vy = sy + total / 3 * 20;
            spots.add(new AspectSpot(tag, aspects.getAmount(tag), vx, vy));
            total++;
        }
        return spots;
    }

    @Override
    public RecipeType<CrucibleRecipeType> getRecipeType() {
        return ArcaneresearchJEIPlugin.CRUCIBLE_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipeType recipe, IFocusGroup focuses) {

        
        Ingredient catalyst = recipe.getCatalyst();
        if (catalyst != null && !catalyst.isEmpty()) {
            builder.addSlot(RecipeIngredientRole.INPUT, OX - 65, OY - 57)
                    .addIngredients(catalyst);
        }

        
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 51)
                .addItemStack(recipe.getResultItem());
    }

    @Override
    public void draw(CrucibleRecipeType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(OX, OY);
        guiGraphics.pose().scale(2f, 2f);
        
        
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -28, -29, 0, 3, 56, 17, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -28, -12, 0, 20, 56, 48, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_TEXTURE, -25, -26, 100, 84, 11, 13, 256, 256);
        guiGraphics.pose().popMatrix();

        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            AspectRenderer.drawAspect(guiGraphics, spot.x(), spot.y(), spot.aspect(), spot.amount());
        }

        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, CrucibleRecipeType recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            if (AspectRenderer.isMouseOverAspect(spot.x(), spot.y(), (int) mouseX, (int) mouseY)) {
                tooltip.addAll(AspectRenderer.getAspectTooltip(spot.aspect(), spot.amount()));
                return;
            }
        }
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }

    
    
    @Override
    public Identifier getIdentifier(CrucibleRecipeType recipe) {
        return ArcaneresearchJEIPlugin.recipeId(recipe);
    }

    private record AspectSpot(Aspect aspect, int amount, int x, int y) {
    }
}
