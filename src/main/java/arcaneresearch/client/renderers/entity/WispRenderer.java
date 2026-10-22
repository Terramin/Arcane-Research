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
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.entities.monster.EntityWisp;






public class WispRenderer extends EntityRenderer<EntityWisp, WispRenderer.State> {

    public static class State extends EntityRenderState {
        public int color;
        public int frame;
        public boolean dead;
    }

    private static final Identifier PARTICLES_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");
    private static final Identifier NODE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/auranodes.png");

    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityWisp entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        int color = 0;
        Aspect aspect = entity.getAspect();
        if (aspect != null) {
            color = aspect.getColor();
        }
        state.color = color;
        state.frame = entity.tickCount % 16;
        state.dead = entity.isDeadOrDying();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        if (state.dead) {
            return;
        }
        float red = ((state.color >> 16) & 0xFF) / 255.0F;
        float green = ((state.color >> 8) & 0xFF) / 255.0F;
        float blue = (state.color & 0xFF) / 255.0F;

        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        RenderType particles = RenderTypes.entityTranslucentEmissive(PARTICLES_TEXTURE);
        collector.submitCustomGeometry(poseStack, particles,
                (pose, consumer) -> renderQuad(pose, consumer, 0.4F, 1.0F, 1.0F, 1.0F, 1.0F, 64, 512 + state.frame));
        collector.submitCustomGeometry(poseStack, particles,
                (pose, consumer) -> renderQuad(pose, consumer, 0.75F, 1.0F, 1.0F, 1.0F, 0.25F, 64, 320 + state.frame));
        RenderType node = RenderTypes.entityTranslucentEmissive(NODE_TEXTURE);
        collector.submitCustomGeometry(poseStack, node,
                (pose, consumer) -> renderQuad(pose, consumer, 0.75F, red, green, blue, 0.5F, 32, 800 + state.frame));

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                                   float size, float red, float green, float blue, float alpha,
                                   int grid, int frame) {
        int xm = frame % grid;
        int ym = frame / grid;
        float u0 = xm / (float) grid;
        float u1 = u0 + 1.0F / grid;
        float v0 = ym / (float) grid;
        float v1 = v0 + 1.0F / grid;

        int light = 0xF000F0;

        vertexConsumer.addVertex(pose, -size, -size, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        vertexConsumer.addVertex(pose, size, -size, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        vertexConsumer.addVertex(pose, size, size, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);

        vertexConsumer.addVertex(pose, -size, size, 0.0F)
                .setColor(red, green, blue, alpha)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}