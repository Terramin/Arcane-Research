package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.entity.ArcaneBoreModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
@OnlyIn(Dist.CLIENT)
public class ArcaneBoreRenderer extends MobRenderer<EntityArcaneBore, ArcaneBoreModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/arcanebore.png");
    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/beam1.png");
    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(ArcaneBoreModel.LAYER_LOCATION)), 0.5F);
    }
    private static Vec3 rotateAroundY(Vec3 vec, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vec.x * cos + vec.z * sin;
        double z = -vec.x * sin + vec.z * cos;
        return new Vec3(x, vec.y, z);
    }
    private static Vec3 rotateAroundZ(Vec3 vec, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vec.x * cos - vec.y * sin;
        double y = vec.x * sin + vec.y * cos;
        return new Vec3(x, y, vec.z);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityArcaneBore entity) {
        return TEXTURE;
    }
    @Override
    protected RenderType getRenderType(EntityArcaneBore entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
    @Override
    public void render(EntityArcaneBore entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        if (entity.isClientDigging() && entity.isActive() && entity.hasValidInventory()) {
            renderMiningBeam(entity, partialTicks, poseStack, buffer, packedLight);
        }
    }
    private void renderMiningBeam(EntityArcaneBore entity, float partialTicks,
                                  PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        Vec3 offset = new Vec3(0.5, 0.075, 0.0);
        offset = rotateAroundZ(offset, pitch * Mth.DEG_TO_RAD);
        offset = rotateAroundY(offset, -((yaw + 90.0F) * Mth.DEG_TO_RAD));
        poseStack.translate(offset.x, entity.getEyeHeight() + offset.y, offset.z);
        renderGlow(poseStack, buffer, entity.tickCount, partialTicks);
        renderBeam(poseStack, buffer, entity, partialTicks, packedLight);
        poseStack.popPose();
    }
    private void renderGlow(PoseStack poseStack, MultiBufferSource buffer, int tickCount, float partialTicks) {
        VertexConsumer builder = buffer.getBuffer(TCRenderTypes.additiveTextured(UtilsFX.NODE_TEXTURE));
        int frame = 96 + tickCount % 32;
        UtilsFX.renderBillboardQuad(poseStack, builder, 0.5F, 32, 32, frame,
                0.0F, 1.0F, 0.4F, 0.8F, LightTexture.pack(13, 0));
    }
    private void renderBeam(PoseStack poseStack, MultiBufferSource buffer,
                            EntityArcaneBore entity, float partialTicks, int packedLight) {
        float beamLength = 5.0F;
        float beamWidth = 0.15F;
        float opacity = 0.4F;
        float rotation = (entity.level().getGameTime() % 72L) * 5L + 5.0F * partialTicks;
        float time = entity.tickCount + partialTicks;
        float scroll = time * 0.2F - Mth.floor(time * 0.1F);
        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.pushPose();
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(180.0F + yaw));
        poseStack.mulPose(Axis.XN.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        VertexConsumer vertexConsumer = buffer.getBuffer(TCRenderTypes.additiveTextured(BEAM_TEXTURE));
        for (int i = 0; i < 3; i++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(60.0F));
            PoseStack.Pose pose = poseStack.last();
            float u0 = 0.0F;
            float u1 = 1.0F;
            float v0 = -1.0F + scroll + i / 3.0F;
            float v1 = beamLength + v0;
            int beamLight = LightTexture.pack(12, 0);
            int r = (int) (0.0F * 255);
            int g = (int) (1.0F * 255);
            int b = (int) (0.4F * 255);
            int a = (int) (opacity * 255);
            vertexConsumer.vertex(pose.pose(), 0.0F, beamLength, 0.0F)
                    .color(r, g, b, a)
                    .uv(u1, v1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(beamLight)
                    .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
            vertexConsumer.vertex(pose.pose(), -beamWidth, 0.0F, 0.0F)
                    .color(r, g, b, a)
                    .uv(u1, v0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(beamLight)
                    .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
            vertexConsumer.vertex(pose.pose(), beamWidth, 0.0F, 0.0F)
                    .color(r, g, b, a)
                    .uv(u0, v0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(beamLight)
                    .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
            vertexConsumer.vertex(pose.pose(), 0.0F, beamLength, 0.0F)
                    .color(r, g, b, a)
                    .uv(u0, v1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(beamLight)
                    .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
        poseStack.popPose();
    }
    @Override
    protected boolean shouldShowName(EntityArcaneBore entity) {
        return false;
    }
}
