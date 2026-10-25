package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import arcaneresearch.common.entities.projectile.EntityGolemOrb;
@OnlyIn(Dist.CLIENT)
public class GolemOrbRenderer extends EntityRenderer<EntityGolemOrb> {
    private static final ResourceLocation PARTICLES =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    public GolemOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityGolemOrb entity) {
        return PARTICLES;
    }
    @Override
    public void render(EntityGolemOrb entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float bob = Mth.sin(entity.tickCount / 5.0F) * 0.2F + 0.2F;
        float scale = 1.0F + bob;
        poseStack.scale(scale, scale, scale);
        float f2 = (1 + entity.tickCount % 6) / 32.0F;
        float f3 = f2 + 0.03125F;
        float f4 = entity.isRed() ? 0.1875F : 0.21875F;
        float f5 = f4 + 0.03125F;
        float half = 0.5F;
        float height = 1.0F;
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(PARTICLES));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int light = 0xF000F0;
        float a = 0.8F;
        builder.vertex(matrix, -half, -half, 0.0F).color(1.0F, 1.0F, 1.0F, a)
                .uv(f2, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, height - half, -half, 0.0F).color(1.0F, 1.0F, 1.0F, a)
                .uv(f3, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, height - half, height - half, 0.0F).color(1.0F, 1.0F, 1.0F, a)
                .uv(f3, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, -half, height - half, 0.0F).color(1.0F, 1.0F, 1.0F, a)
                .uv(f2, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
