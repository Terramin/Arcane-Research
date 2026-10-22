package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.tiles.devices.TileMirrorEssentia;



public class EssentiaMirrorRenderer implements BlockEntityRenderer<TileMirrorEssentia, EssentiaMirrorRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public net.minecraft.core.BlockPos mirrorPos;
        public net.minecraft.world.level.Level level;
        public net.minecraft.world.level.block.state.BlockState mirrorState;
        public boolean linked;
    }

    public EssentiaMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileMirrorEssentia tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.mirrorPos = tile.getBlockPos();
        state.level = tile.getLevel();
        state.mirrorState = tile.getBlockState();
        state.linked = tile.linked;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        MirrorRenderer.renderMirror(state.mirrorPos, state.level, state.mirrorState,
                state.linked, MirrorRenderer.frame2Texture(),
                poseStack, collector, state.lightCoords);
    }
}