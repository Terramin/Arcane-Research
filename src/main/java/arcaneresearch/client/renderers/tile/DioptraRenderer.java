package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.devices.TileDioptra;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class DioptraRenderer implements BlockEntityRenderer<TileDioptra> {
    private static final ResourceLocation GRID_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/gridblock.png");
    private static final ResourceLocation SIDE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/dioptra_side.png");
    public DioptraRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileDioptra tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        Minecraft mc = Minecraft.getInstance();
        float ticks = (mc.player != null ? mc.player.tickCount : 0) + partialTicks;
        boolean showingVis = tile.isDisplayingVis();
        float rc, gc, bc;
        if (showingVis) {
            rc = Mth.sin(ticks / 12.0f) * 0.05f + 0.85f;
            gc = Mth.sin(ticks / 11.0f) * 0.05f + 0.9f;
            bc = Mth.sin(ticks / 10.0f) * 0.05f + 0.95f;
        } else {
            rc = Mth.sin(ticks / 12.0f) * 0.05f + 0.85f;
            gc = Mth.sin(ticks / 11.0f) * 0.05f + 0.45f;
            bc = Mth.sin(ticks / 10.0f) * 0.05f + 0.95f;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        renderGrid(tile, poseStack, buffer, rc, gc, bc, ticks, packedLight);
        renderSidePanels(poseStack, buffer, rc, gc, bc, packedLight);
        poseStack.popPose();
    }
    private void renderGrid(TileDioptra tile, PoseStack poseStack, MultiBufferSource buffer,
                            float r, float g, float b, float ticks, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.495, 0.501, -0.495);
        poseStack.scale(0.99f, 1.0f, 0.99f);
        VertexConsumer vertexConsumer = buffer.getBuffer(
                arcaneresearch.client.renderers.TCRenderTypes.additiveTextured(GRID_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        for (int a = 0; a < 12; a++) {
            for (int bb = 0; bb < 12; bb++) {
                float h00 = tile.getGridValue(a, bb) / 96.0f;
                float h10 = tile.getGridValue(a + 1, bb) / 96.0f;
                float h11 = tile.getGridValue(a + 1, bb + 1) / 96.0f;
                float h01 = tile.getGridValue(a, bb + 1) / 96.0f;
                float x0 = a / 12.0f;
                float x1 = (a + 1) / 12.0f;
                float z0 = bb / 12.0f;
                float z1 = (bb + 1) / 12.0f;
                double d3 = a - 6;
                double d4 = bb - 6;
                double dis = Math.sqrt(d3 * d3 + d4 * d4);
                float wave = Mth.sin((float) ((tile.counter - dis * 10.0) / 8.0));
                int brightness = (int) (200.0f + wave * 15.0f);
                int light = net.minecraft.client.renderer.LightTexture.pack(
                        Math.round(brightness / 16.0f), 0);
                Color c = new Color(r * 0.8f, g, b);
                float cr = c.getRed() / 255.0f;
                float cg = c.getGreen() / 255.0f;
                float cb = c.getBlue() / 255.0f;
                float alpha = 0.9f;
                vertexConsumer.vertex(matrix, x0, h01, z1).color(cr, cg, cb, alpha)
                        .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x1, h11, z1).color(cr, cg, cb, alpha)
                        .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x1, h10, z0).color(cr, cg, cb, alpha)
                        .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x0, h00, z0).color(cr, cg, cb, alpha)
                        .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                if (a == 0) {
                    vertexConsumer.vertex(matrix, 0, 0, z1).color(cr, cg, cb, 0f)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, h01, z1).color(cr, cg, cb, alpha)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, h00, z0).color(cr, cg, cb, alpha)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, 0, z0).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }
                if (a == 11) {
                    vertexConsumer.vertex(matrix, 1, 0, z0).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, h10, z0).color(cr, cg, cb, alpha)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, h11, z1).color(cr, cg, cb, alpha)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, 0, z1).color(cr, cg, cb, 0f)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }
                if (bb == 0) {
                    vertexConsumer.vertex(matrix, x0, h00, 0).color(cr, cg, cb, alpha)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x1, h10, 0).color(cr, cg, cb, alpha)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x1, 0, 0).color(cr, cg, cb, 0f)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x0, 0, 0).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }
                if (bb == 11) {
                    vertexConsumer.vertex(matrix, x0, 0, 1).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x1, 0, 1).color(cr, cg, cb, 0f)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x1, h11, 1).color(cr, cg, cb, alpha)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x0, h01, 1).color(cr, cg, cb, alpha)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }
            }
        }
        poseStack.popPose();
    }
    private void renderSidePanels(PoseStack poseStack, MultiBufferSource buffer,
                                  float r, float g, float b, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 1.0, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(270));
        VertexConsumer vertexConsumer = buffer.getBuffer(
                arcaneresearch.client.renderers.TCRenderTypes.additiveTexturedNoCull(SIDE_TEXTURE));
        int fullLight = net.minecraft.client.renderer.LightTexture.pack(14, 0);
        for (int q = 0; q < 4; q++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f * q));
            poseStack.translate(0, 0, -0.5);
            renderCenteredQuad(poseStack, vertexConsumer, 1.0f, r, g, b, 0.8f, fullLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private void renderCenteredQuad(PoseStack poseStack, VertexConsumer consumer,
                                    float size, float r, float g, float b, float a, int light) {
        Matrix4f matrix = poseStack.last().pose();
        float half = size / 2.0f;
        consumer.vertex(matrix, -half, -half, 0).color(r, g, b, a)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, half, -half, 0).color(r, g, b, a)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, half, half, 0).color(r, g, b, a)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -half, half, 0).color(r, g, b, a)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
    }
    @Override
    public boolean shouldRenderOffScreen(TileDioptra tile) {
        return true;
    }
}
