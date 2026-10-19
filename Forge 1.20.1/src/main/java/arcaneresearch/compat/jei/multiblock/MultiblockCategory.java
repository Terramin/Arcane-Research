package arcaneresearch.compat.jei.multiblock;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.client.gui.BlueprintBlockAccess;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;
import java.util.ArrayList;
import java.util.List;
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class MultiblockCategory implements IRecipeCategory<ArcaneresearchApi.BluePrint> {
    public static final ResourceLocation UID = new ResourceLocation(Arcaneresearch.MODID, "multiblock");
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
    @Override
    public IDrawable getBackground() {
        return background;
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
    @Override
    public void draw(ArcaneresearchApi.BluePrint recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (access == null || currentRecipe != recipe) {
            Part[][][] parts = recipe.getParts();
            currentRecipe = recipe;
            access = (parts == null) ? null : new BlueprintBlockAccess(parts);
        }
        if (access == null) {
            JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
            return;
        }
        access.sliceLine = (int) ((System.currentTimeMillis() / 1000) % (access.getHeight() + 1));
        Part[][][] parts = recipe.getParts();
        int ySize = parts.length;
        int xSize = parts[0].length;
        int zSize = parts[0][0].length;
        float scale = Math.max(6f, 26 - Math.max(ySize, Math.max(xSize, zSize)) * 2);
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var buffer = guiGraphics.bufferSource();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(CX, CY, 200);
        guiGraphics.pose().scale(scale, -scale, scale);
        guiGraphics.pose().mulPose(com.mojang.math.Axis.XP.rotationDegrees(ROT_X));
        guiGraphics.pose().mulPose(com.mojang.math.Axis.YP.rotationDegrees(ROT_Y));
        guiGraphics.pose().translate(-xSize / 2f, -ySize / 2f, -zSize / 2f);
        for (int h = 0; h < ySize; ++h) {
            for (int l = 0; l < xSize; ++l) {
                for (int w = 0; w < zSize; ++w) {
                    BlockState state = access.getBlockState(new BlockPos(l, h, w));
                    if (state.isAir()) {
                        continue;
                    }
                    try {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(l, h, w);
                        blockRenderer.renderSingleBlock(state, guiGraphics.pose(), buffer,
                                15728880, OverlayTexture.NO_OVERLAY,
                                net.minecraftforge.client.model.data.ModelData.EMPTY, null);
                        BlueprintBlockAccess.renderNodeOverlay(guiGraphics.pose(), buffer, parts[h][l][w]);
                        guiGraphics.pose().popPose();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        guiGraphics.pose().popPose();
        guiGraphics.flush();
        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }
    @Override
    public List<Component> getTooltipStrings(ArcaneresearchApi.BluePrint recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH);
    }
}
