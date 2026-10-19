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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.GrapplerModel;
import arcaneresearch.common.entities.projectile.EntityFocusMine;
@OnlyIn(Dist.CLIENT)
public class FocusMineRenderer extends EntityRenderer<EntityFocusMine> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/mine.png");
    private final GrapplerModel model;
    public FocusMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(GrapplerModel.LAYER_LOCATION));
    }
    @Override
    public ResourceLocation getTextureLocation(EntityFocusMine entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityFocusMine entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float pulse = (entity.tickCount + partialTicks) % 8.0F / 8.0F;
        float red = 1.0F;
        float green = 1.0F - pulse;
        float blue = 1.0F - pulse;
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks));
        RenderType renderType = RenderType.entityCutout(TEXTURE);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        int light = 0xF000F0;
        this.model.renderToBuffer(poseStack, vertexConsumer, light,
                OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
