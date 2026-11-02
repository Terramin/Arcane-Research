package arcaneresearch.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class WorldAspectRenderer {
    private static final int ROW_SIZE = 5;
    private static final int TAGSCALE_MILLI_MAX = 500;
    private static final AtomicInteger TAGSCALE_MILLI = new AtomicInteger(0);

    public static void tick() {
        TAGSCALE_MILLI.updateAndGet(v -> v > 0 ? Math.max(0, v - 5) : 0);
    }

    private static float bumpAndGetTagscale() {
        return TAGSCALE_MILLI.updateAndGet(v -> {
            if (v < TAGSCALE_MILLI_MAX) {
                int next = v + 31 - v / 10;
                if (next > TAGSCALE_MILLI_MAX) next = TAGSCALE_MILLI_MAX;
                return next;
            }
            return v;
        }) / 1000f;
    }

    public static void drawTagsOnContainer(PoseStack poseStack, double x, double y, double z, AspectList tags,
                                           Direction face, float partialTick) {
        if (poseStack == null || tags == null || tags.size() == 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        float tagscale = bumpAndGetTagscale();
        double bx = x + 0.5;
        double by = y + 0.5;
        double bz = z + 0.5;
        if (face != null) {
            bx += tagscale * 2.0 * face.getStepX();
            by += tagscale * 2.0 * face.getStepY();
            bz += tagscale * 2.0 * face.getStepZ();
        }
        drawTagRows(poseStack, bx, by, bz, tags, tagscale);
    }

    public static void drawTagsAt(PoseStack poseStack, double x, double y, double z, AspectList tags) {
        if (poseStack == null || tags == null || tags.size() == 0) return;
        float tagscale = bumpAndGetTagscale();
        drawTagRows(poseStack, x, y, z, tags, tagscale);
    }

    private static void drawTagRows(PoseStack poseStack, double x, double y, double z, AspectList tags, float tagscale) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        Aspect[] allAspects = tags.getAspects();
        if (allAspects == null || allAspects.length == 0) return;

        int validCount = 0;
        for (Aspect tag : allAspects) {
            if (tag != null && tag.getImage() != null) validCount++;
        }
        if (validCount == 0) return;

        Aspect[] valid = new Aspect[validCount];
        int idx = 0;
        for (Aspect tag : allAspects) {
            if (tag != null && tag.getImage() != null) valid[idx++] = tag;
        }

        float scale = Math.max(0.05f, tagscale);
        int total = valid.length;
        Quaternionf rotation = camera.rotation();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        double dx = x - cameraPos.x;
        double dy = y - cameraPos.y;
        double dz = z - cameraPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        try {
            for (int i = 0; i < total; i++) {
                Aspect tag = valid[i];
                int row = i / ROW_SIZE;
                int col = i % ROW_SIZE;
                int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);
                float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
                float slotY = row * 1.15f;

                poseStack.pushPose();
                try {
                    poseStack.translate(dx, dy, dz);
                    poseStack.mulPose(rotation);
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(slotX, slotY, 0);
                    drawAspectQuad(poseStack.last().pose(), tag.getImage(), tag.getColor());
                } catch (Exception ignored) {
                } finally {
                    poseStack.popPose();
                }
            }
            for (int i = 0; i < total; i++) {
                Aspect tag = valid[i];
                int row = i / ROW_SIZE;
                int col = i % ROW_SIZE;
                int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);
                float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
                float slotY = row * 1.15f;

                poseStack.pushPose();
                try {
                    poseStack.translate(dx, dy, dz);
                    poseStack.mulPose(rotation);
                    poseStack.scale(scale, scale, scale);
                    poseStack.translate(slotX, slotY, 0);
                    drawAmountText(poseStack, font, buffer, String.valueOf(tags.getAmount(tag)));
                } catch (Exception ignored) {
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    private static void drawAspectQuad(Matrix4f matrix, ResourceLocation texture, int color) {
        if (matrix == null || texture == null) return;
        ResourceLocation stripped = arcaneresearch.client.lib.UtilsFX.strippedAspectTexture(texture);
        if (stripped == null) return;
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, stripped);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, -0.5f, -0.5f, 0).uv(1, 1).color(r, g, b, 0.85f).endVertex();
        builder.vertex(matrix, 0.5f, -0.5f, 0).uv(0, 1).color(r, g, b, 0.85f).endVertex();
        builder.vertex(matrix, 0.5f, 0.5f, 0).uv(0, 0).color(r, g, b, 0.85f).endVertex();
        builder.vertex(matrix, -0.5f, 0.5f, 0).uv(1, 0).color(r, g, b, 0.85f).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }

    private static void drawAmountText(PoseStack poseStack, Font font, MultiBufferSource.BufferSource buffer, String text) {
        if (poseStack == null || font == null || buffer == null || text == null || text.isEmpty()) return;
        float textWidth = font.width(text) * 0.04f;
        poseStack.pushPose();
        try {
            poseStack.translate(textWidth - 0.48f, -0.5f, 0.01f);
            poseStack.scale(-0.04f, -0.04f, 0.04f);
            Matrix4f matrix = poseStack.last().pose();
            font.drawInBatch(text, 1, 1, 0x111111, false, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, 15728880);
            font.drawInBatch(text, 0, 0, 0xFFFFFF, false, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, 15728880);
            buffer.endBatch();
        } finally {
            poseStack.popPose();
        }
    }

    public static void drawTextInAir(PoseStack poseStack, double x, double y, double z,
                                     float partialTick, String text) {
        if (poseStack == null || text == null || text.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        Quaternionf rotation = camera.rotation();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        poseStack.pushPose();
        try {
            poseStack.translate(x - cameraPos.x + 0.5, y - cameraPos.y + 0.5, z - cameraPos.z + 0.5);
            poseStack.mulPose(rotation);
            poseStack.scale(-0.0125f, -0.0125f, 0.0125f);
            Matrix4f matrix = poseStack.last().pose();
            float w = font.width(text) / 2f;
            font.drawInBatch(text, -w, 0, 0xFFFFFF, true, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, 15728880);
            buffer.endBatch();
        } finally {
            poseStack.popPose();
        }
    }
}