package arcaneresearch.compat.jei.multiblock;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import mezz.jei.api.gui.builder.ITooltipBuilder; // NEOFORGE_PORT: getTooltipStrings -> getTooltip
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
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
    public RecipeType<ArcaneresearchApi.BluePrint> getRecipeType() {
        return ArcaneresearchJEIPlugin.MULTIBLOCK_TYPE;
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
    public void setRecipe(IRecipeLayoutBuilder builder, ArcaneresearchApi.BluePrint recipe, IFocusGroup focuses) {
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

    // NEOFORGE_PORT (26.3 render rework, Forge logic preserved): GuiGraphicsExtractor.pose()
    // is now a 2D Matrix3x2f (no pushPose/translate3D/scale3D/mulPose/bufferSource/flush),
    // and BlockRenderer.renderSingleBlock is gone. Slots + slice animation state stay 1:1;
    // the isometric preview goes through our own PiP renderer (IsoStructurePip).
    @Override
    public void draw(ArcaneresearchApi.BluePrint recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        if (access == null || currentRecipe != recipe) {
            Part[][][] parts = recipe.getParts();
            currentRecipe = recipe;
            access = (parts == null) ? null : new BlueprintBlockAccess(parts);
        }
        if (access != null) {
            access.sliceLine = (int) ((System.currentTimeMillis() / 1000) % (access.getHeight() + 1));
        }
        // NEOFORGE_PORT (26.3): Forge isometric preview (GuiGraphics.pose() 3D
        // + BlockRenderDispatcher.renderSingleBlock, both removed in 26.x) is
        // a true picture-in-picture now — same route as the Thaumonomicon
        // blueprint (see IsoStructurePip). Anchor/scale 1:1 with Forge:
        // cube-centre projects onto (CX, CY), scale max(6, 26 - 2 * maxDim).
        if (access != null && recipe.getParts() != null) {
            Part[][][] parts = recipe.getParts();
            int ySize = parts.length;
            int xSize = parts[0].length;
            int zSize = parts[0][0].length;
            float scale = Math.max(6f, 26 - Math.max(ySize, Math.max(xSize, zSize)) * 2);
            IsoStructurePip.submitFixedScale(guiGraphics, access, parts,
                    xSize, ySize, zSize, CX, CY, scale);
        }
        // NEOFORGE_PORT (26.3 render rework, Forge logic preserved): GuiGraphicsExtractor.pose()
        // is now a 2D Matrix3x2f (no pushPose/translate3D/scale3D/mulPose/bufferSource/flush),
        // and BlockRenderer.renderSingleBlock is gone. Slots + slice animation state stay 1:1.
        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, ArcaneresearchApi.BluePrint recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }
}
