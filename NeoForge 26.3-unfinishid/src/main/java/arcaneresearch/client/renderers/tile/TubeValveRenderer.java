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
import arcaneresearch.client.models.block.TubeValveModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileTubeValve;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (facing-rotated valve ring + rod).
public class TubeValveRenderer implements BlockEntityRenderer<TileTubeValve, TubeValveRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/valve.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public Direction facing = Direction.NORTH;
        public float rotation;
    }

    private final TubeValveModel model;

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileTubeValve tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        state.facing = tile.facing;
        state.rotation = tile.rotation;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        Direction facing = state.facing;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        applyFacingRotation(poseStack, facing);

        float rotation = state.rotation;
        poseStack.rotate(Axis.YP.rotationDegrees(-rotation * 1.5f));

        float offset = -0.03f - (rotation / 360.0f) * 0.09f;
        poseStack.translate(0, offset, 0);

        poseStack.scale(0.0625f, 0.0625f, 0.0625f);

        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            model.renderRing(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });

        poseStack.scale(0.75f, 1.0f, 0.75f);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            model.renderRod(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });

        poseStack.popPose();
    }

    static PoseStack bridge(PoseStack.Pose pose) {
        PoseStack work = new PoseStack();
        work.last().pose().set(pose.pose());
        work.last().normal().set(pose.normal());
        return work;
    }

    static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        if (facing.getAxis() != Direction.Axis.Y) {
            poseStack.rotate(Axis.YP.rotationDegrees(90));
        } else {
            poseStack.rotate(Axis.XN.rotationDegrees(90));
            poseStack.rotate(Axis.YP.rotationDegrees(90.0f * facing.getStepY()));
        }

        poseStack.rotate(Axis.XP.rotationDegrees(90.0f * facing.getStepX()));
        poseStack.rotate(Axis.YP.rotationDegrees(90.0f * facing.getStepY()));
        poseStack.rotate(Axis.ZP.rotationDegrees(90.0f * facing.getStepZ()));
    }
}