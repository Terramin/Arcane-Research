package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.entities.EntityFallingTaint;

// NEOFORGE_PORT (26.3): falling block via MovingBlockRenderState +
// SubmitNodeCollector.submitMovingBlock (vanilla FallingBlockRenderer shape).
// Block-model context (biome/lighting) filled in extract from the entity level.
public class FallingTaintRenderer extends EntityRenderer<EntityFallingTaint, FallingTaintRenderer.State> {

    public static class State extends EntityRenderState {
        public final MovingBlockRenderState movingBlock = new MovingBlockRenderState();
        public boolean visible;
    }

    public FallingTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityFallingTaint entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        BlockState blockState = entity.getBlockState();
        state.visible = blockState != null && blockState.getRenderShape() != RenderShape.INVISIBLE;
        if (!state.visible) {
            return;
        }
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();
        state.movingBlock.blockState = blockState;
        state.movingBlock.blockPos = pos;
        state.movingBlock.randomSeedPos = pos;
        state.movingBlock.biome = level.getBiome(pos);
        state.movingBlock.lightEngine = level.getLightEngine();
        state.movingBlock.cardinalLighting = level instanceof ClientLevel client
                ? client.cardinalLighting() : CardinalLighting.DEFAULT;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (!state.visible) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        collector.submitMovingBlock(poseStack, state.movingBlock, state.lightCoords);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}