package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.BellowsModel;
import arcaneresearch.client.models.block.BellowsNozzleModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.devices.TileBellows;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (facing rotation, inflation, nozzle extension).
public class BellowsRenderer implements BlockEntityRenderer<TileBellows, BellowsRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/bellows.png");
    private static final Identifier BORE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/bore.png");

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.SOUTH;
        public float inflation = 1.0F;
        public boolean nozzle;
        public Direction nozzleFacing = Direction.SOUTH;
    }

    private final BellowsModel model;
    private final BellowsNozzleModel nozzleModel;

    public BellowsRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BellowsModel(context.bakeLayer(BellowsModel.LAYER_LOCATION));
        this.nozzleModel = new BellowsNozzleModel(context.bakeLayer(BellowsNozzleModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileBellows tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.facing = TileBellows.getFacing(tile.getBlockState());
        state.inflation = tile.inflation;
        boolean nozzle = false;
        if (tile.getLevel() != null) {
            net.minecraft.world.level.block.entity.BlockEntity te =
                    tile.getLevel().getBlockEntity(tile.getBlockPos().relative(state.facing));
            nozzle = te instanceof arcaneresearch.common.tiles.essentia.TileTubeBuffer;
        }
        state.nozzle = nozzle;
        state.nozzleFacing = state.facing;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();

        poseStack.translate(0.5, -0.5, 0.5);

        Direction facing = state.facing;
        applyRotation(poseStack, facing);

        // NEOFORGE_PORT (26.3): geometry is emitted later (deferred) and the model is
        // shared between all bellows, so the inflation is applied inside the callback.
        final float inflation = state.inflation;

        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.setInflation(inflation);
            model.renderToBuffer(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });

        poseStack.popPose();

        if (state.nozzle) {
            renderNozzleExtension(state.nozzleFacing, poseStack, collector, light);
        }
    }

    private void renderNozzleExtension(Direction facing, PoseStack poseStack,
                                       SubmitNodeCollector collector, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5 + facing.getStepX(), facing.getStepY(), 0.5 + facing.getStepZ());
        switch (facing.getOpposite()) {
            case DOWN -> {
                poseStack.translate(-0.5, 0.5, 0);
                poseStack.rotate(Axis.ZP.rotationDegrees(-90));
            }
            case UP -> {
                poseStack.translate(0.5, 0.5, 0);
                poseStack.rotate(Axis.ZP.rotationDegrees(90));
            }
            case NORTH -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(270));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case EAST -> {

            }
        }

        RenderType nozzleType = TCRenderTypes.objSolid(BORE_TEXTURE);
        collector.submitCustomGeometry(poseStack, nozzleType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            nozzleModel.renderToBuffer(work, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0f, 1.0f, 1.0f, 1.0f);
        });
        poseStack.popPose();
    }

    private void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> {
                poseStack.translate(0, 1, -1);
                poseStack.rotate(Axis.XP.rotationDegrees(90));
            }
            case UP -> {
                poseStack.translate(0, 1, 1);
                poseStack.rotate(Axis.XP.rotationDegrees(270));
            }
            case NORTH -> {
                poseStack.rotate(Axis.YP.rotationDegrees(180));
            }
            case SOUTH -> {

            }
            case WEST -> {
                poseStack.rotate(Axis.YP.rotationDegrees(270));
            }
            case EAST -> {
                poseStack.rotate(Axis.YP.rotationDegrees(90));
            }
        }
    }
}