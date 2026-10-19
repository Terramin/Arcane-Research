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
import arcaneresearch.common.tiles.aura.TileNodeEnergized;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1 via a custom State.
public class EnergizedNodeRenderer implements BlockEntityRenderer<TileNodeEnergized, EnergizedNodeRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public arcaneresearch.api.aspects.AspectList auraBase;
        public arcaneresearch.common.lib.aura.NodeType nodeType;
        public arcaneresearch.common.lib.aura.NodeModifier nodeModifier;
        public net.minecraft.core.BlockPos nodePos;
        public LivingEntity viewer;
        public Player player;
        public float partialTicks;
    }

    public EnergizedNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileNodeEnergized tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        state.viewer = camEntity instanceof LivingEntity living ? living : null;
        state.player = state.viewer instanceof Player player ? player : null;
        state.auraBase = tile.getAuraBase();
        state.nodeType = tile.getNodeType();
        state.nodeModifier = tile.getNodeModifier();
        state.nodePos = tile.getBlockPos();
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();
        AuraNodeRenderHelper.renderNode(poseStack, collector, state.auraBase, state.nodeType,
                state.nodeModifier, state.nodePos, state.viewer,
                new Vector3f(0.5F, 0.5F, 0.5F), 1.0F,
                AuraNodeRenderHelper.resolve(state.player, true), state.partialTicks);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}