package arcaneresearch.client.renderers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
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
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
@OnlyIn(Dist.CLIENT)
public class WorldAspectRenderer {
    private static final int ROW_SIZE = 5;
    private static volatile float tagscale = 0f;
    public static void tick() {
        if (tagscale > 0f) {
            tagscale -= 0.005f;
        }
    }
    public static void drawTagsOnContainer(PoseStack poseStack, double x, double y, double z, AspectList tags,
                                           Direction face, float partialTick) {
        if (tags == null || tags.size() == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (tagscale < 0.5f) {
            tagscale += 0.031f - tagscale / 10f;
        }
        double bx = x + 0.5;
        double by = y + 0.5;
        double bz = z + 0.5;
        if (face != null) {
            bx += tagscale * 2.0 * face.getStepX();
            by += tagscale * 2.0 * face.getStepY();
            bz += tagscale * 2.0 * face.getStepZ();
        }
        drawTagRows(poseStack, bx, by, bz, tags);
    }
    public static void drawTagsAt(PoseStack poseStack, double x, double y, double z, AspectList tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }
        if (tagscale < 0.5f) {
            tagscale += 0.031f - tagscale / 10f;
        }
        drawTagRows(poseStack, x, y, z, tags);
    }
    private static void drawTagRows(PoseStack poseStack, double x, double y, double z, AspectList tags) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        java.util.List<Aspect> valid = new java.util.ArrayList<>();
        for (Aspect tag : tags.getAspects()) {
            if (tag != null && tag.getImage() != null) {
                valid.add(tag);
            }
        }
        if (valid.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        float scale = Math.max(0.05f, tagscale);
        int total = valid.size();
        for (int i = 0; i < total; i++) {
            Aspect tag = valid.get(i);
            int row = i / ROW_SIZE;
            int col = i % ROW_SIZE;
            int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);
            float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
            float slotY = row * 1.15f;
            poseStack.pushPose();
            try {
                poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
                poseStack.scale(scale, scale, scale);
                poseStack.translate(slotX, slotY, 0);
                drawAspectQuad(poseStack.last().pose(), tag.getImage(), tag.getColor());
            } catch (Exception ignored) {
            } finally {
                poseStack.popPose();
            }
        }
        for (int i = 0; i < total; i++) {
            Aspect tag = valid.get(i);
            int row = i / ROW_SIZE;
            int col = i % ROW_SIZE;
            int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);
            float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
            float slotY = row * 1.15f;
            poseStack.pushPose();
            try {
                poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
                poseStack.scale(scale, scale, scale);
                poseStack.translate(slotX, slotY, 0);
                drawAmountText(poseStack, String.valueOf(tags.getAmount(tag)));
            } catch (Exception ignored) {
            } finally {
                poseStack.popPose();
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    private static void drawAspectQuad(Matrix4f matrix, ResourceLocation texture, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0,
                arcaneresearch.client.lib.UtilsFX.strippedAspectTexture(texture));
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, -0.5f, -0.5f, 0).uv(1, 1).color(r, g, b, 0.85f).endVertex();
        buffer.vertex(matrix, 0.5f, -0.5f, 0).uv(0, 1).color(r, g, b, 0.85f).endVertex();
        buffer.vertex(matrix, 0.5f, 0.5f, 0).uv(0, 0).color(r, g, b, 0.85f).endVertex();
        buffer.vertex(matrix, -0.5f, 0.5f, 0).uv(1, 0).color(r, g, b, 0.85f).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
    private static void drawAmountText(PoseStack poseStack, String text) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        float textWidth = font.width(text) * 0.04f;
        poseStack.pushPose();
        poseStack.translate(textWidth - 0.48f, -0.5f, 0.01f);
        poseStack.scale(-0.04f, -0.04f, 0.04f);
        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(text, 1, 1, 0x111111, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(text, 0, 0, 0xFFFFFF, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, 15728880);
        buffer.endBatch();
        poseStack.popPose();
    }
    public static void drawTextInAir(PoseStack poseStack, double x, double y, double z,
                                     float partialTick, String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || text == null) {
            return;
        }
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(x - cameraPos.x + 0.5, y - cameraPos.y + 0.5, z - cameraPos.z + 0.5);
        poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
        poseStack.scale(-0.0125f, -0.0125f, 0.0125f);
        Font font = mc.font;
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Matrix4f matrix = poseStack.last().pose();
        float w = font.width(text) / 2f;
        font.drawInBatch(text, -w, 0, 0xFFFFFF, true, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, 15728880);
        buffer.endBatch();
        poseStack.popPose();
    }
}
