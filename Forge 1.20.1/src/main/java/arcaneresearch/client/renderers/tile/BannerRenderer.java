package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.BannerModel;
import arcaneresearch.common.tiles.misc.TileBanner;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class BannerRenderer implements BlockEntityRenderer<TileBanner> {
    private static final ResourceLocation TEX_CULT =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/banner_cultist.png");
    private static final ResourceLocation TEX_BLANK =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/banner_blank.png");
    private static final int ICON_SIZE = 16;
    private final BannerModel model;
    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BannerModel(context.bakeLayer(BannerModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileBanner banner, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture;
        if (banner.getAspect() == null && banner.getColor() == -1) {
            texture = TEX_CULT;
        } else {
            texture = TEX_BLANK;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        if (banner.getLevel() != null) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            float facingAngle = banner.getBannerFacing() * 360.0f / 16.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle));
        }
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        if (!banner.getWall()) {
            model.renderPole(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            poseStack.translate(0.0, 1.0, -0.4125);
        }
        model.renderBeam(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        int color = banner.getColor();
        if (color != -1) {
            Color c = new Color(color);
            red = c.getRed() / 255.0f;
            green = c.getGreen() / 255.0f;
            blue = c.getBlue() / 255.0f;
        }
        model.renderTabs(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 1.0f);
        Player player = Minecraft.getInstance().player;
        if (player != null && banner.getLevel() != null) {
            float time = banner.getBlockPos().getX() * 7 +
                    banner.getBlockPos().getY() * 9 +
                    banner.getBlockPos().getZ() * 13 +
                    player.tickCount + partialTicks;
            float rx = 0.02f - Mth.sin(time / 11.0f) * 0.02f;
            model.setBannerRotation(rx);
            model.renderBanner(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 1.0f);
            Aspect aspect = banner.getAspect();
            if (aspect != null) {
                poseStack.pushPose();
                poseStack.translate(0.0, 0.0, 0.05001);
                poseStack.scale(0.0375f, 0.0375f, 0.0375f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                poseStack.mulPose(Axis.XP.rotationDegrees(-rx * 57.295776f * 2.0f));
                renderAspectIcon(poseStack, buffer, aspect, -8, 0, packedLight, packedOverlay, 0.75f);
                poseStack.popPose();
            }
        } else {
            model.renderBanner(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, 1.0f);
        }
        poseStack.popPose();
    }
    private void renderAspectIcon(PoseStack poseStack, MultiBufferSource buffer, Aspect aspect,
                                  int x, int y, int packedLight, int packedOverlay, float alpha) {
        if (aspect == null) return;
        ResourceLocation texture = UtilsFX.strippedAspectTexture(aspect.getImage());
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        Matrix4f matrix = poseStack.last().pose();
        float x1 = x;
        float y1 = y;
        float x2 = x + ICON_SIZE;
        float y2 = y + ICON_SIZE;
        vertexConsumer.vertex(matrix, x1, y2, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x2, y2, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x2, y1, 0).color(r, g, b, alpha).uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, x1, y1, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
}
