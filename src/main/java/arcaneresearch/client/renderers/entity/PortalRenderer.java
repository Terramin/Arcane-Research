package arcaneresearch.client.renderers.entity;

import arcaneresearch.client.models.entity.PortalModel;
import arcaneresearch.common.entities.EntityPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class PortalRenderer extends MobRenderer<EntityPortal, PortalModel<EntityPortal>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/portal.png");
    private static final ResourceLocation GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/entity/glowportal.png");
    private static final int SECONDS_PER_TICK_GROUP = 20;
    private float lastFadeAlpha = 1f;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context, new PortalModel<>(context.bakeLayer(PortalModel.LAYER_LOCATION)), 0f);

        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight,
                               @NotNull EntityPortal entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                VertexConsumer consumer = bufferSource.getBuffer(RenderType.eyes(GLOW_TEXTURE));
                this.getParentModel().renderToBuffer(
                        poseStack, consumer, 0xF000F0,
                        LivingEntityRenderer.getOverlayCoords(entity, 0),
                        1f, 1f, 1f, 1f);
            }
        });
    }

    protected void setupRotations(EntityPortal entity, @NotNull PoseStack poseStack, float bob, float yBodyRot, float partialTicks) {
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        super.setupRotations(entity, poseStack, bob, yaw, partialTicks);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityPortal entity) {
        return TEXTURE;
    }

    @Override
    public void render(EntityPortal entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        float alpha = entity.getFadeAlpha(partialTicks);
        this.lastFadeAlpha = alpha;
        if (alpha <= 0.001f) {
            return;
        }
        if (alpha < 0.999f) {
            bufferSource = new FadingBufferSource(bufferSource, alpha);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        renderLifetimeTimer(entity, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    protected RenderType getRenderType(@NotNull EntityPortal entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (this.lastFadeAlpha < 0.999f) {
            return RenderType.entityTranslucentCull(TEXTURE);
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    private void renderLifetimeTimer(EntityPortal entity, float partialTicks,
                                     PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.isClosing() || entity.getOpenFraction(partialTicks) < 0.999f) {
            return;
        }
        int lifetime = entity.getDisplayedLifetime();
        if (lifetime <= 0) {
            return;
        }

        String text = Integer.toString((lifetime + SECONDS_PER_TICK_GROUP - 1) / SECONDS_PER_TICK_GROUP);
        float scale = 0.025F;

        poseStack.pushPose();
        poseStack.translate(0.0D, entity.getBbHeight() + 0.3D, 0.0D);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(-scale, -scale, scale);
        Font font = this.getFont();
        float halfWidth = -font.width(text) / 2.0F;
        font.drawInBatch(text, halfWidth, 0.0F, 0xFFFFFFFF, false,
                poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0x40000000, packedLight);
        poseStack.popPose();
    }

    private record FadingBufferSource(MultiBufferSource parent, float alpha) implements MultiBufferSource {

        @Override
            public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
                return new FadingVertexConsumer(this.parent.getBuffer(renderType), this.alpha);
            }
        }

    private record FadingVertexConsumer(VertexConsumer inner, float alpha) implements VertexConsumer {

        @Override
            public @NotNull VertexConsumer vertex(double x, double y, double z) {
                this.inner.vertex(x, y, z);
                return this;
            }

            @Override
            public @NotNull VertexConsumer color(int red, int green, int blue, int alphaInt) {
                this.inner.color(red, green, blue, (int) (alphaInt * this.alpha));
                return this;
            }

            @Override
            public @NotNull VertexConsumer uv(float u, float v) {
                this.inner.uv(u, v);
                return this;
            }

            @Override
            public @NotNull VertexConsumer overlayCoords(int u, int v) {
                this.inner.overlayCoords(u, v);
                return this;
            }

            @Override
            public @NotNull VertexConsumer uv2(int u, int v) {
                this.inner.uv2(u, v);
                return this;
            }

            @Override
            public @NotNull VertexConsumer normal(float x, float y, float z) {
                this.inner.normal(x, y, z);
                return this;
            }

            @Override
            public void endVertex() {
                this.inner.endVertex();
            }

            @Override
            public void defaultColor(int red, int green, int blue, int alphaInt) {
                this.inner.defaultColor(red, green, blue, (int) (alphaInt * this.alpha));
            }

            @Override
            public void unsetDefaultColor() {
                this.inner.unsetDefaultColor();
            }

            @Override
            public void vertex(float x, float y, float z, float red, float green, float blue, float a,
                               float texU, float texV, int overlayUV, int lightUV,
                               float normalX, float normalY, float normalZ) {
                this.inner.vertex(x, y, z, red, green, blue, a * this.alpha,
                        texU, texV, overlayUV, lightUV, normalX, normalY, normalZ);
            }
        }
}
