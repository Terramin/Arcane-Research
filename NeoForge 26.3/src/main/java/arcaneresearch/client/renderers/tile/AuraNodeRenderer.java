package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import arcaneresearch.common.tiles.aura.AuraNodeTile;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1 via a custom
// State (node visuals delegate to AuraNodeRenderHelper submit emission).
public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeTile, AuraNodeRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public arcaneresearch.common.lib.aura.INode node;
        public net.minecraft.core.BlockPos nodePos;
        public LivingEntity viewer;
        public Player player;
        public net.minecraft.world.entity.Entity drainTarget;
        public arcaneresearch.api.aspects.Aspect drainAspect;
        public long gameTime;
        public long lastTapGameTime;
        public net.minecraft.core.BlockPos hungryFlowFrom;
        public long hungryFlowTime;
        public boolean hasLevel;
        public arcaneresearch.api.aspects.Aspect[] flowAspects = new arcaneresearch.api.aspects.Aspect[0];
        public float partialTicks;
    }

    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AuraNodeTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        state.viewer = camEntity instanceof LivingEntity living ? living : null;
        state.player = state.viewer instanceof Player player ? player : null;
        state.node = tile;
        state.nodePos = tile.getBlockPos();
        state.drainTarget = tile.getDrainTarget();
        state.drainAspect = tile.getDrainAspect();
        state.gameTime = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0L;
        state.hasLevel = tile.getLevel() != null;
        state.lastTapGameTime = tile.getLastTapGameTime();
        state.hungryFlowFrom = tile.getHungryFlowFrom();
        state.hungryFlowTime = tile.getHungryFlowTime();
        arcaneresearch.api.aspects.Aspect[] flowAspects = tile.getAspects().getAspects();
        state.flowAspects = flowAspects != null ? flowAspects : new arcaneresearch.api.aspects.Aspect[0];
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();
        Vector3f center = new Vector3f(0.5F, 0.5F, 0.5F);
        AuraNodeRenderHelper.renderNode(poseStack, collector, state.node, state.nodePos, state.viewer,
                center, 1.0F,
                AuraNodeRenderHelper.resolve(state.player, false), state.partialTicks);

        if (state.hasLevel) {
            AuraNodeRenderHelper.drawDrainBeam(poseStack, collector, state.nodePos, center,
                    state.drainTarget, state.drainAspect,
                    state.gameTime, state.lastTapGameTime, state.partialTicks);

            if (state.hungryFlowFrom != null && state.gameTime - state.hungryFlowTime < 12L) {
                Vector3f from = new Vector3f(
                        state.hungryFlowFrom.getX() - state.nodePos.getX() + 0.5F,
                        state.hungryFlowFrom.getY() - state.nodePos.getY() + 0.5F,
                        state.hungryFlowFrom.getZ() - state.nodePos.getZ() + 0.5F);
                int flowColor = 0xFFFFFF;
                if (state.flowAspects.length > 0 && state.flowAspects[0] != null) {
                    flowColor = state.flowAspects[0].getColor();
                }
                AuraNodeRenderHelper.drawFloatyLine(poseStack, collector, from, center,
                        flowColor, -0.05F, 1.0F, 0.12F);
            }
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}