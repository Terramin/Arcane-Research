package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.client.models.block.TubeValveModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileTubeOneway;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (one-way flow indicator rod).
public class TubeOnewayRenderer implements BlockEntityRenderer<TileTubeOneway, TubeOnewayRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/valve.png");

    public static class State extends BlockEntityRenderState {
        public boolean connected;
        public Direction facing = Direction.NORTH;
    }

    private final TubeValveModel model;

    public TubeOnewayRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileTubeOneway tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        if (tile.getLevel() == null) {
            state.connected = false;
            return;
        }
        BlockEntity connectedTile = ArcaneresearchApiHelper.getConnectableTile(
                tile.getLevel(), tile.getBlockPos(), tile.facing.getOpposite());
        state.connected = connectedTile != null;
        state.facing = tile.facing;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.connected) {
            return;
        }
        Direction facing = state.facing;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        TubeValveRenderer.applyFacingRotation(poseStack, facing);

        float r = 0.45f;
        float g = 0.5f;
        float b = 1.0f;

        poseStack.scale(0.0625f, 0.0625f, 0.0625f);
        poseStack.scale(2.0f, 2.0f, 2.0f);
        poseStack.translate(0, -0.32 / 0.0625 / 2, 0);

        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = TubeValveRenderer.bridge(pose);
            model.renderRod(work, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        });

        poseStack.popPose();
    }
}