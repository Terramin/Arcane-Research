package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.devices.TileDioptra;

@OnlyIn(Dist.CLIENT)
public class DioptraRenderer implements BlockEntityRenderer<TileDioptra> {
    private static final ResourceLocation GRID_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/gridblock.png");
    private static final ResourceLocation SIDE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/dioptra_side.png");

    private static final float GRID_DIVISOR = 96.0f;
    private static final float GRID_ALPHA = 0.9f;
    private static final float SIDE_ALPHA = 0.8f;
    private static final int SIDE_LIGHT = LightTexture.pack(14, 0);

    public DioptraRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileDioptra tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;

        Minecraft mc = Minecraft.getInstance();
        float ticks = (mc.player != null ? mc.player.tickCount : 0) + partialTicks;

        float sinT12 = Mth.sin(ticks / 12.0f) * 0.05f;
        float sinT11 = Mth.sin(ticks / 11.0f) * 0.05f;
        float sinT10 = Mth.sin(ticks / 10.0f) * 0.05f;

        float rc = sinT12 + 0.85f;
        float gc = tile.isDisplayingVis() ? (sinT11 + 0.9f) : (sinT11 + 0.45f);
        float bc = sinT10 + 0.95f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        renderGrid(tile, poseStack, buffer, rc, gc, bc);
        renderSidePanels(poseStack, buffer, rc, gc, bc);
        poseStack.popPose();
    }

    private void renderGrid(TileDioptra tile, PoseStack poseStack, MultiBufferSource buffer,
                            float r, float g, float b) {
        poseStack.pushPose();
        poseStack.translate(-0.495, 0.501, -0.495);
        poseStack.scale(0.99f, 1.0f, 0.99f);

        VertexConsumer vertexConsumer = buffer.getBuffer(TCRenderTypes.additiveTextured(GRID_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        float cr = Mth.clamp(r * 0.8f, 0.0f, 1.0f);
        float cg = Mth.clamp(g, 0.0f, 1.0f);
        float cb = Mth.clamp(b, 0.0f, 1.0f);

        for (int a = 0; a < 12; a++) {
            float x0 = a / 12.0f;
            float x1 = (a + 1) / 12.0f;

            boolean leftEdge = a == 0;
            boolean rightEdge = a == 11;

            double d3 = a - 6;

            for (int bb = 0; bb < 12; bb++) {
                float h00 = tile.getGridValue(a, bb) / GRID_DIVISOR;
                float h10 = tile.getGridValue(a + 1, bb) / GRID_DIVISOR;
                float h11 = tile.getGridValue(a + 1, bb + 1) / GRID_DIVISOR;
                float h01 = tile.getGridValue(a, bb + 1) / GRID_DIVISOR;

                float z0 = bb / 12.0f;
                float z1 = (bb + 1) / 12.0f;

                double d4 = bb - 6;
                double dis = Math.sqrt(d3 * d3 + d4 * d4);

                float wave = Mth.sin((float) ((tile.counter - dis * 10.0) / 8.0));
                int brightness = (int) (200.0f + wave * 15.0f);
                int light = LightTexture.pack(Math.round(brightness / 16.0f), 0);

                vertexConsumer.vertex(matrix, x0, h01, z1).color(cr, cg, cb, GRID_ALPHA)
                        .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x1, h11, z1).color(cr, cg, cb, GRID_ALPHA)
                        .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x1, h10, z0).color(cr, cg, cb, GRID_ALPHA)
                        .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                vertexConsumer.vertex(matrix, x0, h00, z0).color(cr, cg, cb, GRID_ALPHA)
                        .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();

                if (leftEdge) {
                    vertexConsumer.vertex(matrix, 0, 0, z1).color(cr, cg, cb, 0f)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, h01, z1).color(cr, cg, cb, GRID_ALPHA)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, h00, z0).color(cr, cg, cb, GRID_ALPHA)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 0, 0, z0).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }

                if (rightEdge) {
                    vertexConsumer.vertex(matrix, 1, 0, z0).color(cr, cg, cb, 0f)
                            .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, h10, z0).color(cr, cg, cb, GRID_ALPHA)
                            .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, h11, z1).color(cr, cg, cb, GRID_ALPHA)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, 1, 0, z1).color(cr, cg, cb, 0f)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }

                if (bb == 0) {
                    vertexConsumer.vertex(matrix, x0, h00, 0).color(cr, cg, cb, GRID_ALPHA)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x1, h10, 0).color(cr, cg, cb, GRID_ALPHA)
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
                    vertexConsumer.vertex(matrix, x1, h11, 1).color(cr, cg, cb, GRID_ALPHA)
                            .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                    vertexConsumer.vertex(matrix, x0, h01, 1).color(cr, cg, cb, GRID_ALPHA)
                            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
                }
            }
        }

        poseStack.popPose();
    }

    private void renderSidePanels(PoseStack poseStack, MultiBufferSource buffer,
                                  float r, float g, float b) {
        poseStack.pushPose();
        poseStack.translate(0, 1.0, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(270));

        VertexConsumer vertexConsumer = buffer.getBuffer(TCRenderTypes.additiveTexturedNoCull(SIDE_TEXTURE));

        float cr = Mth.clamp(r, 0.0f, 1.0f);
        float cg = Mth.clamp(g, 0.0f, 1.0f);
        float cb = Mth.clamp(b, 0.0f, 1.0f);

        for (int q = 0; q < 4; q++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f * q));
            poseStack.translate(0, 0, -0.5);
            renderCenteredQuad(poseStack, vertexConsumer, 1.0f, cr, cg, cb, SIDE_ALPHA, SIDE_LIGHT);
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
    public boolean shouldRenderOffScreen(@NotNull TileDioptra tile) {
        return true;
    }
}