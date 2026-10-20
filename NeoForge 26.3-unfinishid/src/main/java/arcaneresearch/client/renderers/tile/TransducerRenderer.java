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
import arcaneresearch.common.tiles.aura.TransducerTile;

/**
 * Node transducer renderer, ported from TC4 TileNodeConverterRenderer.
 * Same lock + pistons device as the converter (shared OBJ in TC4):
 * glow tinted by status (green idle, orange charging, red energized).
 * NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
 * (delegates to NodeConverterRenderer.renderDevice, as in Forge).
 */
public class TransducerRenderer implements BlockEntityRenderer<TransducerTile, TransducerRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public int count;
        public int status;
    }

    public TransducerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TransducerTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        // NEOFORGE_PORT (26.3): Forge scaled the transducer count (0..1000) to the
        // converter's piston range 0..50 before drawing.
        state.count = Math.max(0, Math.min(50, tile.count * 50 / 1000));
        state.status = tile.status;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        NodeConverterRenderer.renderDevice(poseStack, collector,
                state.count, state.status, state.lightCoords);
    }
}