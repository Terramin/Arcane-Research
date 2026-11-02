package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.BannerModel;
import arcaneresearch.common.tiles.misc.TileBanner;

@OnlyIn(Dist.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<TileBanner> {
    private static final ResourceLocation TEX_CULT =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/banner_cultist.png");
    private static final ResourceLocation TEX_BLANK =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/banner_blank.png");

    private static final int ICON_SIZE = 16;
    private static final float FULL_WHITE = 1.0f;
    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    private final BannerModel model;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BannerModel(context.bakeLayer(BannerModel.LAYER_LOCATION));
    }

    @Override
    public void render(TileBanner banner, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Aspect bannerAspect = banner.getAspect();
        int bannerColor = banner.getColor();
        Level level = banner.getLevel();

        ResourceLocation texture = (bannerAspect == null && bannerColor == -1) ? TEX_CULT : TEX_BLANK;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));

        if (level != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

            float facingAngle = banner.getBannerFacing() * 360.0f / 16.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle));
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        if (!banner.getWall()) {
            model.renderPole(poseStack, vertexConsumer, packedLight, packedOverlay,
                    FULL_WHITE, FULL_WHITE, FULL_WHITE, FULL_WHITE);
        } else {
            poseStack.translate(0.0, 1.0, -0.4125);
        }

        model.renderBeam(poseStack, vertexConsumer, packedLight, packedOverlay,
                FULL_WHITE, FULL_WHITE, FULL_WHITE, FULL_WHITE);

        float red = FULL_WHITE;
        float green = FULL_WHITE;
        float blue = FULL_WHITE;

        if (bannerColor != -1) {
            red = ((bannerColor >> 16) & 0xFF) / 255.0f;
            green = ((bannerColor >> 8) & 0xFF) / 255.0f;
            blue = (bannerColor & 0xFF) / 255.0f;
        }

        model.renderTabs(poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, FULL_WHITE);

        Player player = Minecraft.getInstance().player;

        if (player != null && level != null) {
            BlockPos pos = banner.getBlockPos();

            float time = pos.getX() * 7 +
                    pos.getY() * 9 +
                    pos.getZ() * 13 +
                    player.tickCount + partialTicks;

            float rx = 0.02f - Mth.sin(time / 11.0f) * 0.02f;

            model.setBannerRotation(rx);
            model.renderBanner(poseStack, vertexConsumer, packedLight, packedOverlay,
                    red, green, blue, FULL_WHITE);

            if (bannerAspect != null) {
                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, 0.05001);
                poseStack.scale(0.0375f, 0.0375f, 0.0375f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(-rx * RAD_TO_DEG * 2.0f));

                renderAspectIcon(poseStack, buffer, bannerAspect, -8, 0, packedLight, 0.75f);

                poseStack.popPose();
            }
        } else {
            model.renderBanner(poseStack, vertexConsumer, packedLight, packedOverlay,
                    red, green, blue, FULL_WHITE);
        }

        poseStack.popPose();
    }

    private void renderAspectIcon(PoseStack poseStack, MultiBufferSource buffer, Aspect aspect,
                                  int x, int y, int packedLight, float alpha) {
        if (aspect == null) return;

        ResourceLocation image = aspect.getImage();
        if (image == null) return;

        ResourceLocation texture = UtilsFX.strippedAspectTexture(image);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Matrix4f matrix = poseStack.last().pose();

        float x2 = x + ICON_SIZE;
        float y2 = y + ICON_SIZE;

        vertexConsumer.vertex(matrix, x, y2, 0).color(r, g, b, alpha)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x2, y2, 0).color(r, g, b, alpha)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x2, y, 0).color(r, g, b, alpha)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x, y, 0).color(r, g, b, alpha)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
}