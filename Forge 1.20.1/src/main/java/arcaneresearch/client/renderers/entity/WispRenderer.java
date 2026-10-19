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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.entities.monster.EntityWisp;
@OnlyIn(Dist.CLIENT)
public class WispRenderer extends EntityRenderer<EntityWisp> {
    private static final ResourceLocation PARTICLES_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    private static final ResourceLocation NODE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/auranodes.png");
    public WispRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityWisp entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
    @Override
    public void render(EntityWisp entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isDeadOrDying()) {
            return;
        }
        int color = 0;
        Aspect aspect = entity.getAspect();
        if (aspect != null) {
            color = aspect.getColor();
        }
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        int frame = entity.tickCount % 16;
        VertexConsumer particles = buffer.getBuffer(RenderType.entityTranslucentEmissive(PARTICLES_TEXTURE));
        renderQuad(poseStack, particles, 0.4F, 1.0F, 1.0F, 1.0F, 1.0F, 64, 512 + frame, packedLight);
        renderQuad(poseStack, particles, 0.75F, 1.0F, 1.0F, 1.0F, 0.25F, 64, 320 + frame, packedLight);
        VertexConsumer node = buffer.getBuffer(RenderType.entityTranslucentEmissive(NODE_TEXTURE));
        renderQuad(poseStack, node, 0.75F, red, green, blue, 0.5F, 32, 800 + frame, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderQuad(PoseStack poseStack, VertexConsumer vertexConsumer,
                            float size, float red, float green, float blue, float alpha,
                            int grid, int frame, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int xm = frame % grid;
        int ym = frame / grid;
        float u0 = xm / (float) grid;
        float u1 = u0 + 1.0F / grid;
        float v0 = ym / (float) grid;
        float v1 = v0 + 1.0F / grid;
        int light = 0xF000F0;
        vertexConsumer.vertex(matrix, -size, -size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, size, -size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, size, size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer.vertex(matrix, -size, size, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
