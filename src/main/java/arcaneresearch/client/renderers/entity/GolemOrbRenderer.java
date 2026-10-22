package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.projectile.EntityGolemOrb;



public class GolemOrbRenderer extends EntityRenderer<EntityGolemOrb, GolemOrbRenderer.State> {

    public static class State extends EntityRenderState {
        public float bob;
        public float frameU;
        public boolean red;
    }

    private static final Identifier PARTICLES =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGolemOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bob = Mth.sin(entity.tickCount / 5.0F) * 0.2F + 0.2F;
        state.frameU = (1 + entity.tickCount % 6) / 32.0F;
        state.red = entity.isRed();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        float scale = 1.0F + state.bob;
        poseStack.scale(scale, scale, scale);

        float f2 = state.frameU;
        float f3 = f2 + 0.03125F;
        float f4 = state.red ? 0.1875F : 0.21875F;
        float f5 = f4 + 0.03125F;
        float half = 0.5F;

        RenderType type = RenderTypes.entityTranslucent(PARTICLES);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, -half, -half, f2, f5);
            quad(consumer, pose, half, -half, f3, f5);
            quad(consumer, pose, half, half, f3, f4);
            quad(consumer, pose, -half, half, f2, f4);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(1.0F, 1.0F, 1.0F, 0.8F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}