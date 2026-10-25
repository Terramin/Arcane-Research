package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.BellowsModel;
import arcaneresearch.client.models.block.BellowsNozzleModel;
import arcaneresearch.common.tiles.devices.TileBellows;
@OnlyIn(Dist.CLIENT)
public class BellowsRenderer implements BlockEntityRenderer<TileBellows> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/bellows.png");
    private static final ResourceLocation BORE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/bore.png");
    private final BellowsModel model;
    private final BellowsNozzleModel nozzleModel;
    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BellowsModel(context.bakeLayer(BellowsModel.LAYER_LOCATION));
        this.nozzleModel = new BellowsNozzleModel(context.bakeLayer(BellowsNozzleModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileBellows tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, -0.5, 0.5);
        Direction facing = TileBellows.getFacing(tile.getBlockState());
        applyRotation(poseStack, facing);
        model.setInflation(tile.inflation);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
        renderNozzleExtension(tile, facing, poseStack, buffer, packedLight, packedOverlay);
    }
    private void renderNozzleExtension(TileBellows tile, Direction facing, PoseStack poseStack,
                                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        net.minecraft.world.level.block.entity.BlockEntity te =
                tile.getLevel().getBlockEntity(tile.getBlockPos().relative(facing));
        if (!(te instanceof arcaneresearch.common.tiles.essentia.TileTubeBuffer)) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5 + facing.getStepX(), facing.getStepY(), 0.5 + facing.getStepZ());
        switch (facing.getOpposite()) {
            case DOWN -> {
                poseStack.translate(-0.5, 0.5, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            }
            case UP -> {
                poseStack.translate(0.5, 0.5, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> {
            }
        }
        VertexConsumer nozzleConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BORE_TEXTURE));
        nozzleModel.renderToBuffer(poseStack, nozzleConsumer, packedLight, packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }
    private void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0, 1, -1);
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            case UP -> {
                poseStack.translate(0, 1, 1);
                poseStack.mulPose(Axis.XP.rotationDegrees(270));
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
        }
    }
}
