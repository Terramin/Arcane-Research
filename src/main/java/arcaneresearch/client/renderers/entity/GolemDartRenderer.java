package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.common.entities.projectile.EntityGolemDart;
@OnlyIn(Dist.CLIENT)
public class GolemDartRenderer extends EntityRenderer<EntityGolemDart> {
    private static final ResourceLocation ARROW_TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");
    public GolemDartRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    private static void quad(VertexConsumer builder, Matrix4f matrix, Matrix3f normal,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             float nx, float ny, float nz, int packedLight) {
        builder.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255).uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x3, y3, z3).color(255, 255, 255, 255).uv(u3, v3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x4, y4, z4).color(255, 255, 255, 255).uv(u4, v4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
    }
    @Override
    public ResourceLocation getTextureLocation(EntityGolemDart entity) {
        return ARROW_TEXTURE;
    }
    @Override
    public void render(EntityGolemDart entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F;
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        float shake = (float) ((AbstractArrow) entity).shakeTime - partialTicks;
        if (shake > 0.0F) {
            float wobble = -Mth.sin(shake * 3.0F) * shake;
            poseStack.mulPose(Axis.ZP.rotationDegrees(wobble));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.033F, 0.033F, 0.033F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer builder = buffer.getBuffer(RenderType.entityCutout(ARROW_TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float f2 = 0.0F;
        float f3 = 0.5F;
        float f4 = 0.0F;
        float f5 = 5.0F / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = 5.0F / 32.0F;
        float f9 = 10.0F / 32.0F;
        quad(builder, matrix, normal,
                -7.0F, -2.0F, -2.0F, f6, f8,
                -7.0F, -2.0F, 2.0F, f7, f8,
                -7.0F, 2.0F, 2.0F, f7, f9,
                -7.0F, 2.0F, -2.0F, f6, f9,
                1.0F, 0.0F, 0.0F, packedLight);
        quad(builder, matrix, normal,
                -7.0F, 2.0F, -2.0F, f6, f8,
                -7.0F, 2.0F, 2.0F, f7, f8,
                -7.0F, -2.0F, 2.0F, f7, f9,
                -7.0F, -2.0F, -2.0F, f6, f9,
                -1.0F, 0.0F, 0.0F, packedLight);
        for (int i = 0; i < 4; ++i) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * i));
            PoseStack.Pose bodyPose = poseStack.last();
            Matrix4f bodyMatrix = bodyPose.pose();
            Matrix3f bodyNormal = bodyPose.normal();
            quad(builder, bodyMatrix, bodyNormal,
                    -8.0F, -2.0F, 0.0F, f2, f4,
                    8.0F, -2.0F, 0.0F, f3, f4,
                    8.0F, 2.0F, 0.0F, f3, f5,
                    -8.0F, 2.0F, 0.0F, f2, f5,
                    0.0F, 0.0F, 1.0F, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
