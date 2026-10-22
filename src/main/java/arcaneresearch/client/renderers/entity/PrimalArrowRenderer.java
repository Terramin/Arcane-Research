package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.projectile.EntityPrimalArrow;
import arcaneresearch.common.items.tools.ItemPrimalArrow;

 








 
@OnlyIn(Dist.CLIENT)
public class PrimalArrowRenderer extends EntityRenderer<EntityPrimalArrow, PrimalArrowRenderer.State> {

    public static class State extends EntityRenderState {
        public float yaw;
        public float pitch;
        public float wobble;
        public float fade;
        public int primalType;
        public float r;
        public float g;
        public float b;
        public int frame;
    }

    private static final Identifier ARROW_TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/projectiles/arrow.png");
    private static final Identifier WISP_TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/wispy.png");

    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityPrimalArrow arrow, State state, float partialTicks) {
        super.extractRenderState(arrow, state, partialTicks);
        state.yaw = Mth.lerp(partialTicks, arrow.yRotO, arrow.getYRot()) - 90.0F;
        state.pitch = Mth.lerp(partialTicks, arrow.xRotO, arrow.getXRot());
        float shake = arrow.shakeTime - partialTicks;
        state.wobble = shake > 0.0F ? -Mth.sin(shake * 3.0F) * shake : 0.0F;
        state.fade = Math.max(0.0F, Math.min(1.0F, (100.0F - arrow.getTicksInGround()) / 100.0F));
        int type = arrow.getPrimalType();
        state.primalType = type;
        Aspect aspect = ItemPrimalArrow.aspectFor(type);
        int color = aspect == null ? 0xFFFFFF : aspect.getColor();
        state.r = ((color >> 16) & 0xFF) / 255.0F;
        state.g = ((color >> 8) & 0xFF) / 255.0F;
        state.b = (color & 0xFF) / 255.0F;
        state.frame = arrow.tickCount % 13;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        
        
        
        RenderType body = TCRenderTypes.additiveTextured(ARROW_TEXTURE);
        int light = state.lightCoords;
        float alpha = state.fade;
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = 0.0F;
        float f5 = 5.0F / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = 5.0F / 32.0F;
        float f9 = 10.0F / 32.0F;

        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(state.yaw));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.pitch));
        if (state.wobble != 0.0F) {
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.wobble));
        }
        poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        collector.submitCustomGeometry(poseStack, body, (pose, consumer) -> {
            quad(consumer, pose,
                    -7.0F, -2.0F, -2.0F, f6, f8,
                    -7.0F, -2.0F, 2.0F, f7, f8,
                    -7.0F, 2.0F, 2.0F, f7, f9,
                    -7.0F, 2.0F, -2.0F, f6, f9,
                    1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha, light);
            quad(consumer, pose,
                    -7.0F, 2.0F, -2.0F, f6, f8,
                    -7.0F, 2.0F, 2.0F, f7, f8,
                    -7.0F, -2.0F, 2.0F, f7, f9,
                    -7.0F, -2.0F, -2.0F, f6, f9,
                    -1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, alpha, light);
        });
        
        
        for (int i = 0; i < 4; ++i) {
            poseStack.pushPose();
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(90.0F * i));
            collector.submitCustomGeometry(poseStack, body, (pose, consumer) -> quad(consumer, pose,
                    -8.0F, -2.0F, 0.0F, f2, f4,
                    8.0F, -2.0F, 0.0F, f3, f4,
                    8.0F, 2.0F, 0.0F, f3, f5,
                    -8.0F, 2.0F, 0.0F, f2, f5,
                    0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, alpha, light));
            poseStack.popPose();
        }
        poseStack.popPose();

        
        if (alpha > 0.0F) {
            RenderType trail = state.primalType == ItemPrimalArrow.ENTROPY
                    ? TCRenderTypes.translucentPortal(WISP_TEXTURE)
                    : TCRenderTypes.additiveTextured(WISP_TEXTURE);
            float u0 = state.frame / 64.0F;
            float u1 = u0 + 1.0F / 64.0F;
            float v0 = 3.0F / 64.0F;
            float v1 = 4.0F / 64.0F;
            float size = 0.5F;
            poseStack.pushPose();
            poseStack.rotate(camera.orientation);
            collector.submitCustomGeometry(poseStack, trail, (pose, consumer) -> quad(consumer, pose,
                    -size, -size, 0.0F, u1, v1,
                    -size, size, 0.0F, u1, v0,
                    size, size, 0.0F, u0, v0,
                    size, -size, 0.0F, u0, v1,
                    0.0F, 1.0F, 0.0F, state.r, state.g, state.b, alpha, 0xF000F0));
            poseStack.popPose();
        }

        super.submit(state, poseStack, collector, camera);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             float nx, float ny, float nz,
                             float r, float g, float b, float a, int packedLight) {
        v(consumer, pose, x1, y1, z1, u1, v1, nx, ny, nz, r, g, b, a, packedLight);
        v(consumer, pose, x2, y2, z2, u2, v2, nx, ny, nz, r, g, b, a, packedLight);
        v(consumer, pose, x3, y3, z3, u3, v3, nx, ny, nz, r, g, b, a, packedLight);
        v(consumer, pose, x4, y4, z4, u4, v4, nx, ny, nz, r, g, b, a, packedLight);
    }

    private static void v(VertexConsumer consumer, PoseStack.Pose pose,
                          float x, float y, float z, float u, float v,
                          float nx, float ny, float nz,
                          float r, float g, float b, float a, int packedLight) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, nx, ny, nz);
    }
}
