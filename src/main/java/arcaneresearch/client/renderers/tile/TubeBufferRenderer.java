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
import arcaneresearch.common.tiles.essentia.TileTubeBuffer;



public class TubeBufferRenderer implements BlockEntityRenderer<TileTubeBuffer, TubeBufferRenderer.State> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/valve.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        
        public final boolean[] showRod = new boolean[6];
        public final float[] rodR = new float[6];
        public final float[] rodG = new float[6];
        public final float[] rodB = new float[6];
    }

    private final TubeValveModel model;

    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileTubeBuffer tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        if (!state.hasLevel) {
            return;
        }
        for (Direction dir : Direction.values()) {
            boolean show = false;
            float r = 0.3f, g = 0.3f, b = 1.0f;
            if (tile.chokedSides[dir.ordinal()] != 0 && tile.openSides[dir.ordinal()]) {
                BlockEntity connectedTile = ArcaneresearchApiHelper.getConnectableTile(
                        tile.getLevel(), tile.getBlockPos(), dir);
                if (connectedTile != null) {
                    show = true;
                    if (tile.chokedSides[dir.ordinal()] == 2) {
                        r = 1.0f;
                        g = 0.3f;
                        b = 0.3f;
                    }
                }
            }
            state.showRod[dir.ordinal()] = show;
            state.rodR[dir.ordinal()] = r;
            state.rodG[dir.ordinal()] = g;
            state.rodB[dir.ordinal()] = b;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;

        for (Direction dir : Direction.values()) {
            if (!state.showRod[dir.ordinal()]) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);

            applyDirectionRotation(poseStack, dir.getOpposite());

            float r = state.rodR[dir.ordinal()];
            float g = state.rodG[dir.ordinal()];
            float b = state.rodB[dir.ordinal()];

            poseStack.scale(0.0625f, 0.0625f, 0.0625f);
            poseStack.scale(2.0f, 1.0f, 2.0f);
            poseStack.translate(0, -0.5 / 0.0625, 0);

            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                PoseStack work = TubeValveRenderer.bridge(pose);
                model.renderRod(work, consumer, light, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
            });

            poseStack.popPose();
        }
    }

    private void applyDirectionRotation(PoseStack poseStack, Direction dir) {
        TubeValveRenderer.applyFacingRotation(poseStack, dir);
    }
}