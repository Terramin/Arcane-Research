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
import arcaneresearch.common.entities.projectile.EntityGolemDart;



public class GolemDartRenderer extends EntityRenderer<EntityGolemDart, GolemDartRenderer.State> {

    public static class State extends EntityRenderState {
        public float yaw;
        public float pitch;
        public float wobble;
    }

    private static final Identifier ARROW_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png");

    public GolemDartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGolemDart entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float shake = entity.shakeTime - partialTicks;
        float wobble = 0.0F;
        if (shake > 0.0F) {
            wobble = -Mth.sin(shake * 3.0F) * shake;
        }
        state.wobble = wobble;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(state.yaw));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.pitch));
        if (state.wobble != 0.0F) {
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.wobble));
        }
        poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.033F, 0.033F, 0.033F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);

        RenderType type = RenderTypes.entityCutout(ARROW_TEXTURE);
        int light = state.lightCoords;
        emitQuad(poseStack, collector, type, light,
                -7.0F, -2.0F, -2.0F, 0.0F, 5.0F / 32.0F,
                -7.0F, -2.0F, 2.0F, 0.15625F, 5.0F / 32.0F,
                -7.0F, 2.0F, 2.0F, 0.15625F, 10.0F / 32.0F,
                -7.0F, 2.0F, -2.0F, 0.0F, 10.0F / 32.0F,
                1.0F, 0.0F, 0.0F);
        emitQuad(poseStack, collector, type, light,
                -7.0F, 2.0F, -2.0F, 0.0F, 5.0F / 32.0F,
                -7.0F, 2.0F, 2.0F, 0.15625F, 5.0F / 32.0F,
                -7.0F, -2.0F, 2.0F, 0.15625F, 10.0F / 32.0F,
                -7.0F, -2.0F, -2.0F, 0.0F, 10.0F / 32.0F,
                -1.0F, 0.0F, 0.0F);
        for (int i = 0; i < 4; ++i) {
            poseStack.pushPose();
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(90.0F * i));
            emitQuad(poseStack, collector, type, light,
                    -8.0F, -2.0F, 0.0F, 0.0F, 0.0F,
                    8.0F, -2.0F, 0.0F, 0.5F, 0.0F,
                    8.0F, 2.0F, 0.0F, 0.5F, 5.0F / 32.0F,
                    -8.0F, 2.0F, 0.0F, 0.0F, 5.0F / 32.0F,
                    0.0F, 0.0F, 1.0F);
            poseStack.popPose();
        }

        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, int light,
                                 float x1, float y1, float z1, float u1, float v1,
                                 float x2, float y2, float z2, float u2, float v2,
                                 float x3, float y3, float z3, float u3, float v3,
                                 float x4, float y4, float z4, float u4, float v4,
                                 float nx, float ny, float nz) {
        collector.submitCustomGeometry(poseStack, type,
                (pose, consumer) -> quad(consumer, pose,
                        x1, y1, z1, u1, v1, x2, y2, z2, u2, v2,
                        x3, y3, z3, u3, v3, x4, y4, z4, u4, v4,
                        nx, ny, nz, light));
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             float nx, float ny, float nz, int packedLight) {
        v(consumer, pose, x1, y1, z1, u1, v1, nx, ny, nz, packedLight);
        v(consumer, pose, x2, y2, z2, u2, v2, nx, ny, nz, packedLight);
        v(consumer, pose, x3, y3, z3, u3, v3, nx, ny, nz, packedLight);
        v(consumer, pose, x4, y4, z4, u4, v4, nx, ny, nz, packedLight);
    }

    private static void v(VertexConsumer consumer, PoseStack.Pose pose,
                          float x, float y, float z, float u, float v,
                          float nx, float ny, float nz, int packedLight) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }
}