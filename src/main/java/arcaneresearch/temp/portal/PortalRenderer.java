package arcaneresearch.temp.portal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PortalRenderer extends MobRenderer<PortalEntity, ModelPortalJava<PortalEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/portal.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/glowportal.png");

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelPortalJava<>(context.bakeLayer(ModelPortalJava.LAYER_LOCATION)), 0f);

        this.addLayer(new RenderLayer<PortalEntity, ModelPortalJava<PortalEntity>>(this) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                               PortalEntity entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(
                        poseStack, consumer, 0xF000F0,
                        LivingEntityRenderer.getOverlayCoords(entity, 0),
                        1f, 1f, 1f, 1f);
            }
        });
    }

    protected void setupRotations(PortalEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTicks) {
        // модель поворачивается по фактическому yaw сущности (yBodyRot у "noAi" сущности не обновляется)
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        super.setupRotations(entity, poseStack, bob, yaw, partialTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(PortalEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        renderLifetimeTimer(entity, partialTicks, poseStack, bufferSource, packedLight);
    }

    /** Таймер оставшегося времени существования над порталом (показывается только у полностью открытого портала). */
    private void renderLifetimeTimer(PortalEntity entity, float partialTicks,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isClosing() || entity.getOpenFraction(partialTicks) < 0.999f) {
            return; // портал закрыт/открывается/закрывается — таймер скрыт
        }
        int lifetime = entity.getDisplayedLifetime();
        if (lifetime <= 0) {
            return;
        }

        Component text = Component.literal(Integer.toString(Mth.ceil(lifetime / 20f)));
        float scale = 0.025F;

        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.3D, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        Font font = this.getFont();
        float halfWidth = -font.width(text) / 2.0F;
        font.drawInBatch(text, halfWidth, 0.0F, 0xFFFFD700, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        poseStack.popPose();
    }
}
