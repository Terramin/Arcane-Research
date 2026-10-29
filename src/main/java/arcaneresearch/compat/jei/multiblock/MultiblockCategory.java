package arcaneresearch.compat.jei.multiblock;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import mezz.jei.api.gui.builder.ITooltipBuilder; 
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.client.gui.BlueprintBlockAccess;
import arcaneresearch.client.gui.IsoStructurePip;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MultiblockCategory implements IRecipeCategory<ArcaneresearchApi.BluePrint> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "multiblock");

    private static final int WIDTH = 150;
    private static final int HEIGHT = 150;

    private static final int CX = 75;
    private static final int CY = 62;

    private static final float ROT_X = 25.0f;
    private static final float ROT_Y = -45.0f;

    private static final int PARTS_Y = 124;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    private ArcaneresearchApi.BluePrint currentRecipe;
    private BlueprintBlockAccess access;

    public MultiblockCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.SALIS_MUNDUS.get()));
        this.title = Component.translatable("gui.arcaneresearch.multiblock");
    }

    @Override
    public @NotNull RecipeType<ArcaneresearchApi.BluePrint> getRecipeType() {
        return ArcaneresearchJEIPlugin.MULTIBLOCK_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
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
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ArcaneresearchApi.BluePrint recipe, @NotNull IFocusGroup focuses) {
        if (recipe == null) {
            return;
        }
        this.currentRecipe = recipe;
        Part[][][] parts = recipe.getParts();
        this.access = (parts == null) ? null : new BlueprintBlockAccess(parts);

        ItemStack[] ingredients = recipe.getIngredientList();
        if (ingredients != null && ingredients.length > 0) {
            List<ItemStack> list = new ArrayList<>();
            for (ItemStack stack : ingredients) {
                if (stack != null && !stack.isEmpty()) {
                    list.add(stack.copy());
                }
            }
            int n = list.size();
            int sx = (WIDTH - (n * 18 + (n - 1) * 4)) / 2;
            for (int i = 0; i < n; i++) {
                builder.addSlot(RecipeIngredientRole.INPUT, sx + i * 22, PARTS_Y)
                        .addItemStack(list.get(i));
            }
        }
    }

    
    
    
    
    @Override
    public void draw(@NotNull ArcaneresearchApi.BluePrint recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        if (recipe == null || guiGraphics == null) {
            return;
        }
        if (access == null || currentRecipe != recipe) {
            Part[][][] parts = recipe.getParts();
            currentRecipe = recipe;
            access = (parts == null) ? null : new BlueprintBlockAccess(parts);
        }
        Part[][][] parts = (access == null) ? null : recipe.getParts();
        int ySize = 0;
        int xSize = 0;
        int zSize = 0;
        if (parts != null) {
            ySize = parts.length;
            if (ySize > 0 && parts[0] != null) {
                xSize = parts[0].length;
                if (xSize > 0 && parts[0][0] != null) {
                    zSize = parts[0][0].length;
                }
            }
        }
        if (access != null) {
            int height = access.getHeight();
            if (height < 0) {
                height = 0;
            }
            access.sliceLine = (int) ((System.currentTimeMillis() / 1000L) % (height + 1L));
        }
        
        
        
        
        
        if (access != null && parts != null && ySize > 0 && xSize > 0 && zSize > 0) {
            float scale = Math.max(6f, 26 - Math.max(ySize, Math.max(xSize, zSize)) * 2);
            IsoStructurePip.submitFixedScale(guiGraphics, access, parts,
                    xSize, ySize, zSize, CX, CY, scale);
        }
        
        
        
        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull ArcaneresearchApi.BluePrint recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }
}
