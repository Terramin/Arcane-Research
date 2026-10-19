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
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.projectile.EntityEldritchOrb;
@OnlyIn(Dist.CLIENT)
public class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    private final RandomSource random = RandomSource.create();
    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityEldritchOrb entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityEldritchOrb entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        random.setSeed(187L);
        poseStack.pushPose();
        float age = entity.tickCount + partialTicks;
        float scale = Math.min(entity.tickCount, 10) / 10.0f;
        renderTendrils(poseStack, buffer, age, scale);
        renderOrbSprite(entity, poseStack, buffer, age);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderTendrils(PoseStack poseStack, MultiBufferSource buffer,
                                float age, float scale) {
        VertexConsumer vertexConsumer = buffer.getBuffer(
                arcaneresearch.client.renderers.TCRenderTypes.orbTendrils());
        float spin = age / 80.0F * 360.0F;
        for (int i = 0; i < 12; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + spin));
            float length = (random.nextFloat() * 20.0f + 5.0f) / 30.0f * scale;
            float width = (random.nextFloat() * 2.0f + 1.0f) / 30.0f * scale;
            Matrix4f matrix = poseStack.last().pose();
            vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).endVertex();
            vertexConsumer.vertex(matrix, (float) (-0.866 * width), length, -0.5f * width)
                    .color(64, 64, 64, 255).endVertex();
            vertexConsumer.vertex(matrix, (float) (0.866 * width), length, -0.5f * width)
                    .color(64, 64, 64, 255).endVertex();
            vertexConsumer.vertex(matrix, 0, length, 1.0f * width)
                    .color(64, 64, 64, 255).endVertex();
            vertexConsumer.vertex(matrix, (float) (-0.866 * width), length, -0.5f * width)
                    .color(64, 64, 64, 255).endVertex();
            poseStack.popPose();
        }
    }
    private void renderOrbSprite(EntityEldritchOrb entity, PoseStack poseStack,
                                 MultiBufferSource buffer, float age) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.75f, 0.75f, 0.75f);
        VertexConsumer vertexConsumer = buffer.getBuffer(
                RenderType.entityTranslucentEmissive(TEXTURE));
        int frame = entity.tickCount % 13;
        float u0 = frame / 64.0f;
        float u1 = u0 + 1.0f / 64.0f;
        float v0 = 3.0f / 64.0f;
        float v1 = v0 + 1.0f / 64.0f;
        float size = 0.5f;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertexConsumer.vertex(matrix, -size, -size, 0)
                .color(255, 255, 255, 255)
                .uv(u0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vertexConsumer.vertex(matrix, size, -size, 0)
                .color(255, 255, 255, 255)
                .uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vertexConsumer.vertex(matrix, size, size, 0)
                .color(255, 255, 255, 255)
                .uv(u1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, 0, 1, 0)
                .endVertex();
        vertexConsumer.vertex(matrix, -size, size, 0)
                .color(255, 255, 255, 255)
                .uv(u0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0)
                .normal(normal, 0, 1, 0)
                .endVertex();
        poseStack.popPose();
    }
}
