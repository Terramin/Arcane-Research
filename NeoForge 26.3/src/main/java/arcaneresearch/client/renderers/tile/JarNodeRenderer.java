package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.JarNodeModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.aura.TileJarNode;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (node visuals + animated jar lid/core).
public class JarNodeRenderer implements BlockEntityRenderer<TileJarNode, JarNodeRenderer.State> {
    private static final Identifier JAR_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jar.png");

    public static class State extends BlockEntityRenderState {
        public arcaneresearch.common.lib.aura.INode node;
        public net.minecraft.core.BlockPos nodePos;
        public LivingEntity viewer;
        public Player player;
        public float partialTicks;
        public net.minecraft.world.entity.Entity drainTarget;
        public arcaneresearch.api.aspects.Aspect drainAspect;
        public long gameTime;
        public long lastTapGameTime;
        public boolean hasLevel;
        public float animateSize = 1.0F;
    }

    private final JarNodeModel model;

    public JarNodeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new JarNodeModel(context.bakeLayer(JarNodeModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileJarNode tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        state.viewer = camEntity instanceof LivingEntity living ? living : null;
        state.player = state.viewer instanceof Player player ? player : null;
        state.node = tile;
        state.nodePos = tile.getBlockPos();
        state.partialTicks = partialTicks;
        state.drainTarget = tile.getDrainTarget();
        state.drainAspect = tile.getDrainAspect();
        state.gameTime = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0L;
        state.hasLevel = tile.getLevel() != null;
        state.lastTapGameTime = tile.getLastTapGameTime();
        long animate = tile.getAnimate();
        if (animate > 0L) {
            long now = System.currentTimeMillis();
            if (animate > now) {
                state.animateSize = 1.0F + 2.0F * (animate - now) / 1000.0F;
            } else {
                tile.setAnimate(0L);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();
        Vector3f center = new Vector3f(0.5F, 0.4F, 0.5F);
        AuraNodeRenderHelper.renderNode(poseStack, collector, state.node, state.nodePos, state.viewer,
                center, 0.7F,
                AuraNodeRenderHelper.resolve(state.player, true), state.partialTicks);

        if (state.hasLevel) {
            AuraNodeRenderHelper.drawDrainBeam(poseStack, collector, state.nodePos, center,
                    state.drainTarget, state.drainAspect,
                    state.gameTime, state.lastTapGameTime, state.partialTicks);
        }
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.01D, 0.5D);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
        float size = state.animateSize;
        poseStack.scale(size, size, size);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, TCRenderTypes.objSolid(JAR_TEX), (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderLid(work, consumer, light, OverlayTexture.NO_OVERLAY);
        });
        RenderType glassType = TCRenderTypes.translucentPortal(JAR_TEX);
        collector.submitCustomGeometry(poseStack, glassType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderCore(work, consumer, light, OverlayTexture.NO_OVERLAY, 0.55F);
        });
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}