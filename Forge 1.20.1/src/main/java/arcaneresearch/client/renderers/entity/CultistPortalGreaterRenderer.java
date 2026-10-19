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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.monster.boss.EntityCultistPortalGreater;
@OnlyIn(Dist.CLIENT)
public class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/cultist_portal.png");
    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.1F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityCultistPortalGreater entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityCultistPortalGreater entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        long time = System.nanoTime() / 50000000L;
        float scaley = 1.5F;
        int activeFrames = (int) Math.min(50.0F, entity.tickCount + partialTicks);
        if (entity.hurtTime > 0) {
            double hurtWobble = Math.sin(entity.hurtTime * 72 * Math.PI / 180.0);
            scaley -= (float) (hurtWobble / 4.0);
            activeFrames += (int) (6.0 * hurtWobble);
        }
        if (entity.pulse > 0) {
            double pulseWobble = Math.sin(entity.pulse * 36 * Math.PI / 180.0);
            scaley += (float) (pulseWobble / 4.0);
            activeFrames += (int) (12.0 * pulseWobble);
        }
        float scale = activeFrames / 50.0F * 1.3F;
        float healthPercent = 1.0F - entity.getHealth() / entity.getMaxHealth();
        float m = healthPercent / 3.0F;
        float bob = Mth.sin(entity.tickCount / (5.0F - 12.0F * m)) * m + m;
        float bob2 = Mth.sin(entity.tickCount / (6.0F - 15.0F * m)) * m + m;
        float alpha = 1.0F - bob;
        scaley -= bob / 4.0F;
        scale -= bob2 / 3.0F;
        poseStack.translate(0.0, entity.getBbHeight() / 2.0F, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        int frame = 15 - (int) (time % 16);
        float minU = frame / 16.0F;
        float maxU = minU + 0.0625F;
        float minV = 0.0F;
        float maxV = 1.0F;
        RenderType renderType = TCRenderTypes.translucentPortal(TEXTURE);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int light = net.minecraft.client.renderer.LightTexture.pack(14, 0);
        vertex(vertexConsumer, matrix, normal, -scale, -scaley, 0, maxU, minV, alpha, light);
        vertex(vertexConsumer, matrix, normal, -scale, scaley, 0, maxU, maxV, alpha, light);
        vertex(vertexConsumer, matrix, normal, scale, scaley, 0, minU, maxV, alpha, light);
        vertex(vertexConsumer, matrix, normal, scale, -scaley, 0, minU, minV, alpha, light);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                        float x, float y, float z, float u, float v, float alpha, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, -1.0F)
                .endVertex();
    }
}
