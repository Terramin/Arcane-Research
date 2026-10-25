package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.projectile.EntityPrimalArrow;
import arcaneresearch.common.items.tools.ItemPrimalArrow;
@OnlyIn(Dist.CLIENT)
public class PrimalArrowRenderer extends EntityRenderer<EntityPrimalArrow> {
    private static final ResourceLocation ARROW_TEXTURE =
            new ResourceLocation("textures/entity/projectiles/arrow.png");
    private static final ResourceLocation WISP_TEXTURE =
            new ResourceLocation("arcaneresearch", "textures/misc/wispy.png");
    public PrimalArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityPrimalArrow entity) {
        return ARROW_TEXTURE;
    }
    private static void quad(VertexConsumer builder, Matrix4f matrix, Matrix3f normal,
                             float x1, float y1, float z1, float u1, float v1,
                             float x2, float y2, float z2, float u2, float v2,
                             float x3, float y3, float z3, float u3, float v3,
                             float x4, float y4, float z4, float u4, float v4,
                             float nx, float ny, float nz,
                             float r, float g, float b, float a, int packedLight) {
        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x3, y3, z3).color(r, g, b, a).uv(u3, v3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
        builder.vertex(matrix, x4, y4, z4).color(r, g, b, a).uv(u4, v4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(normal, nx, ny, nz).endVertex();
    }
    @Override
    public void render(EntityPrimalArrow arrow, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTicks, arrow.yRotO, arrow.getYRot()) - 90.0F;
        float pitch = Mth.lerp(partialTicks, arrow.xRotO, arrow.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));
        float shake = (float) arrow.shakeTime - partialTicks;
        if (shake > 0.0F) {
            float wobble = -Mth.sin(shake * 3.0F) * shake;
            poseStack.mulPose(Axis.ZP.rotationDegrees(wobble));
        }
        float fade = Math.max(0.0F, Math.min(1.0F, (100.0F - arrow.getTicksInGround()) / 100.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer builder = buffer.getBuffer(TCRenderTypes.additiveTextured(ARROW_TEXTURE));
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
                1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, fade, packedLight);
        quad(builder, matrix, normal,
                -7.0F, 2.0F, -2.0F, f6, f8,
                -7.0F, 2.0F, 2.0F, f7, f8,
                -7.0F, -2.0F, 2.0F, f7, f9,
                -7.0F, -2.0F, -2.0F, f6, f9,
                -1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, fade, packedLight);
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
                    0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, fade, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
        renderTrail(arrow, partialTicks, buffer, fade);
        super.render(arrow, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderTrail(EntityPrimalArrow arrow, float partialTicks,
                             MultiBufferSource buffer, float fade) {
        if (fade <= 0.0F) {
            return;
        }
        int type = arrow.getPrimalType();
        Aspect aspect = ItemPrimalArrow.aspectFor(type);
        int color = aspect == null ? 0xFFFFFF : aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        VertexConsumer builder = type == ItemPrimalArrow.ENTROPY
                ? buffer.getBuffer(TCRenderTypes.translucentPortal(WISP_TEXTURE))
                : buffer.getBuffer(TCRenderTypes.additiveTextured(WISP_TEXTURE));
        Camera camera = this.entityRenderDispatcher.camera;
        double camX = Mth.lerp(partialTicks, arrow.xo, arrow.getX());
        double camY = Mth.lerp(partialTicks, arrow.yo, arrow.getY());
        double camZ = Mth.lerp(partialTicks, arrow.zo, arrow.getZ());
        net.minecraft.world.phys.Vec3 camPos = camera.getPosition();
        float x = (float) (camX - camPos.x());
        float y = (float) (camY - camPos.y());
        float z = (float) (camZ - camPos.z());
        int frame = arrow.tickCount % 13;
        float u0 = frame / 64.0F;
        float u1 = u0 + 1.0F / 64.0F;
        float v0 = 3.0F / 64.0F;
        float v1 = 4.0F / 64.0F;
        float size = 0.5F;
        Quaternionf q = camera.rotation();
        Vector3f[] v = {
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        PoseStack tmp = new PoseStack();
        for (Vector3f p : v) {
            p.rotate(q);
            p.mul(size);
            p.add(x, y, z);
        }
        Matrix4f m = tmp.last().pose();
        Matrix3f n = tmp.last().normal();
        int light = 0xF000F0;
        quad(builder, m, n,
                v[0].x(), v[0].y(), v[0].z(), u1, v1,
                v[1].x(), v[1].y(), v[1].z(), u1, v0,
                v[2].x(), v[2].y(), v[2].z(), u0, v0,
                v[3].x(), v[3].y(), v[3].z(), u0, v1,
                0.0F, 1.0F, 0.0F, r, g, b, fade, light);
    }
}
