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
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class ArcaneresearchProjectileRenderer<T extends Entity> extends EntityRenderer<T> {
    private final ResourceLocation texture;
    private final float size;
    private final int color;
    private final boolean emissive;
    public ArcaneresearchProjectileRenderer(EntityRendererProvider.Context context,
                                        ResourceLocation texture, float size, int color, boolean emissive) {
        super(context);
        this.texture = texture;
        this.size = size;
        this.color = color;
        this.emissive = emissive;
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
    @Override
    public void render(T entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float spin = (entity.tickCount + partialTicks) * 10.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(spin));
        RenderType renderType = emissive ?
                RenderType.entityTranslucentEmissive(texture) :
                RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = 255;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int light = emissive ? 0xF000F0 : packedLight;
        vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                .color(r, g, b, a)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, size, -size, 0.0F)
                .color(r, g, b, a)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, size, size, 0.0F)
                .color(r, g, b, a)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, -size, size, 0.0F)
                .color(r, g, b, a)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    public static class Factory {
        private static final ResourceLocation ORB_TEXTURE =
                new ResourceLocation(Arcaneresearch.MODID, "textures/entity/orb.png");
        private static final ResourceLocation DART_TEXTURE =
                new ResourceLocation(Arcaneresearch.MODID, "textures/entity/dart.png");
        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> orb(
                EntityRendererProvider.Context context, int color) {
            return new ArcaneresearchProjectileRenderer<>(context, ORB_TEXTURE, 0.25F, color, true);
        }
        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> dart(
                EntityRendererProvider.Context context) {
            return new ArcaneresearchProjectileRenderer<>(context, DART_TEXTURE, 0.15F, 0xFFFFFF, false);
        }
        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> magic(
                EntityRendererProvider.Context context, int color) {
            return new ArcaneresearchProjectileRenderer<>(context, ORB_TEXTURE, 0.2F, color, true);
        }
    }
}
