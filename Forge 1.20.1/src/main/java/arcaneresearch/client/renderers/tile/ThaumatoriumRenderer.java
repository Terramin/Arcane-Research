package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
@OnlyIn(Dist.CLIENT)
public class ThaumatoriumRenderer implements BlockEntityRenderer<TileThaumatorium> {
    private final ItemRenderer itemRenderer;
    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    @Override
    public void render(TileThaumatorium tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile == null || tile.getLevel() == null) return;
        if (tile.recipeHash == null || tile.recipeHash.isEmpty()) return;
        Direction facing = Direction.NORTH;
        try {
            if (tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        } catch (Exception ignored) {
        }
        int ticks = 0;
        if (Minecraft.getInstance().getCameraEntity() != null) {
            ticks = Minecraft.getInstance().getCameraEntity().tickCount;
        }
        float time = ticks + partialTicks;
        int stack = (ticks / 20) % tile.recipeHash.size();
        CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(tile.recipeHash.get(stack));
        if (recipe == null) return;
        ItemStack display = recipe.getRecipeOutput().copy();
        display.setCount(1);
        if (display.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5f + facing.getStepX() / 1.99f,
                1.125f + net.minecraft.util.Mth.sin(time * 0.1f) * 0.06f,
                0.5f + facing.getStepZ() / 1.99f);
        float rotation = switch (facing) {
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.YP.rotationDegrees((time * 3.0f) % 360.0f));
        poseStack.scale(0.75f, 0.75f, 0.75f);
        itemRenderer.renderStatic(display, ItemDisplayContext.FIXED, packedLight,
                packedOverlay, poseStack, buffer, tile.getLevel(), 0);
        poseStack.popPose();
    }
}
