package arcaneresearch.client.lib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.TCRenderTypes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class UtilsFX {
    public static final ResourceLocation NODE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/auranodes.png");
    private static final java.util.Map<ResourceLocation, ResourceLocation> STRIPPED_ASPECT_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<ResourceLocation, ResourceLocation> CUTOUT_ASPECT_CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();
    public static ResourceLocation strippedAspectTexture(ResourceLocation texture) {
        return processedAspectTexture(texture, STRIPPED_ASPECT_CACHE, "stripped", false);
    }
    public static ResourceLocation cutoutAspectTexture(ResourceLocation texture) {
        return processedAspectTexture(texture, CUTOUT_ASPECT_CACHE, "cutout", true);
    }
    private static ResourceLocation processedAspectTexture(ResourceLocation texture,
                                                           java.util.Map<ResourceLocation, ResourceLocation> cache,
                                                           String prefix, boolean fullCutout) {
        ResourceLocation cached = cache.get(texture);
        if (cached != null) {
            return cached;
        }
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            if (resource.isPresent()) {
                try (var in = resource.get().open()) {
                    com.mojang.blaze3d.platform.NativeImage image =
                            com.mojang.blaze3d.platform.NativeImage.read(in);
                    boolean touched = false;
                    for (int y = 0; y < image.getHeight(); y++) {
                        for (int x = 0; x < image.getWidth(); x++) {
                            int pixel = image.getPixelRGBA(x, y);
                            int lum = ((pixel & 0xFF) + ((pixel >> 8) & 0xFF)
                                    + ((pixel >> 16) & 0xFF)) / 3;
                            if (lum < 16) {
                                int a = fullCutout ? 0 : (pixel >>> 24) / 10;
                                image.setPixelRGBA(x, y, (pixel & 0x00FFFFFF) | (a << 24));
                                touched = true;
                            } else if (lum < 48) {
                                float k = (float) (lum - 16) / 32;
                                float fade = fullCutout ? k : 0.1f + 0.9f * k;
                                int a = (int) ((pixel >>> 24) * fade);
                                image.setPixelRGBA(x, y, (pixel & 0x00FFFFFF) | (a << 24));
                                touched = true;
                            }
                        }
                    }
                    if (touched) {
                        ResourceLocation processed = new ResourceLocation(texture.getNamespace(),
                                prefix + "/" + texture.getPath());
                        Minecraft.getInstance().getTextureManager().register(processed,
                                new net.minecraft.client.renderer.texture.DynamicTexture(image));
                        cache.put(texture, processed);
                        return processed;
                    }
                    image.close();
                }
            }
        } catch (Exception ignored) {
        }
        cache.put(texture, texture);
        return texture;
    }
    public static final String[] colorNames = {
            "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime", "Pink", "Gray",
            "Light Gray", "Cyan", "Purple", "Blue", "Brown", "Green", "Red", "Black"
    };
    public static final String[] colorCodes = {
            "\u00a7f", "\u00a76", "\u00a7d", "\u00a79", "\u00a7e", "\u00a7a", "\u00a7d", "\u00a78",
            "\u00a77", "\u00a7b", "\u00a75", "\u00a79", "\u00a74", "\u00a72", "\u00a7c", "\u00a78"
    };
    public static final int[] colors = {
            15790320, 15435844, 12801229, 6719955, 14602026, 4312372, 14188952, 4408131,
            10526880, 2651799, 8073150, 2437522, 5320730, 3887386, 11743532, 1973019
    };
    public static final DecimalFormat myFormatter = new DecimalFormat("#######.##");
    public static float sysPartialTicks = 0.0F;
    public static boolean hideStackOverlay = false;
    public static void rotateToPlayer(PoseStack poseStack) {
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
    }
    public static void renderBillboardQuad(PoseStack poseStack, VertexConsumer builder,
                                           float scale, float r, float g, float b, float a,
                                           int packedLight) {
        renderBillboardQuad(poseStack, builder, scale, 1, 1, 0, r, g, b, a, packedLight);
    }
    public static void renderBillboardQuad(PoseStack poseStack, VertexConsumer builder,
                                           float scale, int gridX, int gridY, int frame,
                                           float r, float g, float b, float a, int packedLight) {
        poseStack.pushPose();
        rotateToPlayer(poseStack);
        int xm = frame % gridX;
        int ym = frame / gridY;
        float f1 = xm / (float) gridX;
        float f2 = f1 + 1.0f / gridX;
        float f3 = ym / (float) gridY;
        float f4 = f3 + 1.0f / gridY;
        Matrix4f matrix = poseStack.last().pose();
        builder.vertex(matrix, -scale, -scale, 0.0f)
                .color(r, g, b, a).uv(f2, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 1.0f, 0.0f).endVertex();
        builder.vertex(matrix, -scale, scale, 0.0f)
                .color(r, g, b, a).uv(f2, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 1.0f, 0.0f).endVertex();
        builder.vertex(matrix, scale, scale, 0.0f)
                .color(r, g, b, a).uv(f1, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 1.0f, 0.0f).endVertex();
        builder.vertex(matrix, scale, -scale, 0.0f)
                .color(r, g, b, a).uv(f1, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0f, 1.0f, 0.0f).endVertex();
        poseStack.popPose();
    }
    public static void renderItemIn2D(ResourceLocation texture, float thickness,
                                      VertexConsumer buffer, PoseStack poseStack, int packedLight) {
        renderTextureIn3D(poseStack, buffer, 1.0F, 1.0F, 0.0F, 0.0F, 16, 16, thickness, packedLight);
    }
    public static void drawBeam(PoseStack poseStack, VertexConsumer builder,
                                Vector s, Vector e, Vector p, float width,
                                int packedLight, float r, float g, float b, float a) {
        Vector ps = sub(s, p);
        Vector se = sub(e, s);
        Vector normal = cross(ps, se).normalize();
        Vector half = mul(normal, width);
        Vector p1 = add(s, half);
        Vector p2 = sub(s, half);
        Vector p3 = add(e, half);
        Vector p4 = sub(e, half);
        drawQuad(poseStack, builder, p1, p3, p4, p2, packedLight, r, g, b, a);
    }
    public static void drawBeam(PoseStack poseStack, VertexConsumer builder,
                                Vector s, Vector e, Vector p, float width, int packedLight) {
        drawBeam(poseStack, builder, s, e, p, width, packedLight, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static void drawQuad(PoseStack poseStack, VertexConsumer builder,
                                Vector p1, Vector p2, Vector p3, Vector p4,
                                int packedLight, float r, float g, float b, float a) {
        org.joml.Matrix4f matrix = poseStack.last().pose();
        builder.vertex(matrix, p1.getX(), p1.getY(), p1.getZ()).color(r, g, b, a).uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, p2.getX(), p2.getY(), p2.getZ()).color(r, g, b, a).uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, p3.getX(), p3.getY(), p3.getZ()).color(r, g, b, a).uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, p4.getX(), p4.getY(), p4.getZ()).color(r, g, b, a).uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
    }
    public static void renderFacingQuad(PoseStack poseStack, VertexConsumer builder,
                                        double px, double py, double pz,
                                        int gridX, int gridY, int frame, float scale,
                                        int color, float alpha, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(px, py, pz);
        rotateToPlayer(poseStack);
        int xm = frame % gridX;
        int ym = frame / gridY;
        float f1 = xm / (float) gridX;
        float f2 = f1 + 1.0F / gridX;
        float f3 = ym / (float) gridY;
        float f4 = f3 + 1.0F / gridY;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        Matrix4f matrix = poseStack.last().pose();
        builder.vertex(matrix, -scale, -scale, 0.0F)
                .color(r, g, b, alpha).uv(f2, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, -scale, scale, 0.0F)
                .color(r, g, b, alpha).uv(f2, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, scale, scale, 0.0F)
                .color(r, g, b, alpha).uv(f1, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, scale, -scale, 0.0F)
                .color(r, g, b, alpha).uv(f1, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F).endVertex();
        poseStack.popPose();
    }
    public static void renderFacingQuad(PoseStack poseStack, VertexConsumer builder,
                                        double px, double py, double pz,
                                        int gridX, int gridY, int frame, float scale,
                                        int color, float alpha, int blend, float partialTicks,
                                        int packedLight) {
        renderFacingQuad(poseStack, builder, px, py, pz, gridX, gridY, frame, scale,
                color, alpha, packedLight);
    }
    public static void drawTexturedQuad(GuiGraphics graphics, float x, float y,
                                        float u, float v, float w, float h, float z) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float s = 0.00390625F;
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(matrix, x, y + h, z).uv(u * s, (v + h) * s).endVertex();
        buf.vertex(matrix, x + w, y + h, z).uv((u + w) * s, (v + h) * s).endVertex();
        buf.vertex(matrix, x + w, y, z).uv((u + w) * s, v * s).endVertex();
        buf.vertex(matrix, x, y, z).uv(u * s, v * s).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }
    public static void drawTexturedQuad(GuiGraphics graphics, float x, float y,
                                        float u, float v, float w, float h) {
        drawTexturedQuad(graphics, x, y, u, v, w, h, 0.0F);
    }
    public static void drawTexturedQuadF(GuiGraphics graphics, float x, float y,
                                         float u, float v, float w, float h, float z) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float d = 0.0625F;
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(matrix, x, y + 16.0F, z).uv(u * d, (v + h) * d).endVertex();
        buf.vertex(matrix, x + 16.0F, y + 16.0F, z).uv((u + w) * d, (v + h) * d).endVertex();
        buf.vertex(matrix, x + 16.0F, y, z).uv((u + w) * d, v * d).endVertex();
        buf.vertex(matrix, x, y, z).uv(u * d, v * d).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }
    public static void drawTexturedQuadF(GuiGraphics graphics, float x, float y,
                                         float u, float v, float w, float h) {
        drawTexturedQuadF(graphics, x, y, u, v, w, h, 0.0F);
    }
    public static void drawTexturedQuadFull(GuiGraphics graphics, float x, float y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(matrix, x, y + 16.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
        buf.vertex(matrix, x + 16.0F, y + 16.0F, 0.0F).uv(1.0F, 1.0F).endVertex();
        buf.vertex(matrix, x + 16.0F, y, 0.0F).uv(1.0F, 0.0F).endVertex();
        buf.vertex(matrix, x, y, 0.0F).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }
    public static void drawTexturedQuadFull(GuiGraphics graphics, ResourceLocation texture,
                                            float x, float y) {
        RenderSystem.setShaderTexture(0, texture);
        drawTexturedQuadFull(graphics, x, y);
    }
    public static void renderItemInGUI(GuiGraphics graphics, ItemStack stack, int x, int y, int z) {
        if (stack.isEmpty()) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, z);
        graphics.renderItem(stack, x, y);
        pose.popPose();
    }
    public static void renderItemInGUI(GuiGraphics graphics, ItemStack stack, int x, int y) {
        renderItemInGUI(graphics, stack, x, y, 0);
    }
    public static void renderQuadCentered(PoseStack poseStack, VertexConsumer builder,
                                          int gridX, int gridY, int frame, float scale,
                                          float red, float green, float blue,
                                          float alpha, int packedLight) {
        int xm = frame % gridX;
        int ym = frame / gridY;
        float f1 = xm / (float) gridX;
        float f2 = f1 + 1.0F / gridX;
        float f3 = ym / (float) gridY;
        float f4 = f3 + 1.0F / gridY;
        float s = scale * 0.5F;
        Matrix4f matrix = poseStack.last().pose();
        builder.vertex(matrix, -s, s, 0.0F)
                .color(red, green, blue, alpha).uv(f2, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, s, s, 0.0F)
                .color(red, green, blue, alpha).uv(f2, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, s, -s, 0.0F)
                .color(red, green, blue, alpha).uv(f1, f3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, -s, -s, 0.0F)
                .color(red, green, blue, alpha).uv(f1, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
    }
    public static void renderQuadCentered(PoseStack poseStack, VertexConsumer builder) {
        renderQuadCentered(poseStack, builder, 1, 1, 0, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0xF000F0);
    }
    public static void renderQuadCentered(PoseStack poseStack, MultiBufferSource buffers,
                                          ResourceLocation texture, float scale,
                                          float red, float green, float blue,
                                          int packedLight, int blend, float alpha) {
        renderQuadCentered(poseStack, buffers, texture, 1, 1, 0, scale,
                red, green, blue, packedLight, blend, alpha);
    }
    public static void renderQuadCentered(PoseStack poseStack, MultiBufferSource buffers,
                                          ResourceLocation texture,
                                          int gridX, int gridY, int frame, float scale,
                                          float red, float green, float blue,
                                          int packedLight, int blend, float alpha) {
        VertexConsumer builder = buffers.getBuffer(blend == 1
                ? TCRenderTypes.additiveTextured(texture)
                : TCRenderTypes.translucentPortal(texture));
        renderQuadCentered(poseStack, builder, gridX, gridY, frame, scale,
                red, green, blue, alpha, packedLight);
    }
    public static void renderQuadFromIcon(PoseStack poseStack, VertexConsumer builder,
                                          TextureAtlasSprite icon, float scale,
                                          float red, float green, float blue,
                                          float alpha, int packedLight) {
        float f1 = icon.getU1();
        float f2 = icon.getV0();
        float f3 = icon.getU0();
        float f4 = icon.getV1();
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        builder.vertex(matrix, 0.0F, 0.0F, 0.0F)
                .color(red, green, blue, alpha).uv(f1, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, 1.0F, 0.0F, 0.0F)
                .color(red, green, blue, alpha).uv(f3, f4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, 1.0F, 1.0F, 0.0F)
                .color(red, green, blue, alpha).uv(f3, f2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix, 0.0F, 1.0F, 0.0F)
                .color(red, green, blue, alpha).uv(f1, f2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        poseStack.popPose();
    }
    public static void renderQuadFromIcon(PoseStack poseStack, VertexConsumer builder,
                                          TextureAtlasSprite icon, float scale,
                                          float red, float green, float blue,
                                          int brightness, int blend, float opacity) {
        renderQuadFromIcon(poseStack, builder, icon, scale, red, green, blue, opacity, brightness);
    }
    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect,
                               float amount, int bonus, float alpha, boolean grayscale) {
        if (aspect == null) {
            return;
        }
        AspectRenderer.drawAspect(graphics, x, y, aspect, amount, bonus, alpha, grayscale);
    }
    public static void drawTag(GuiGraphics graphics, double x, double y, Aspect aspect,
                               float amount, int bonus, float alpha, boolean grayscale) {
        drawTag(graphics, (int) x, (int) y, aspect, amount, bonus, alpha, grayscale);
    }
    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect,
                               float amount, int bonus) {
        drawTag(graphics, x, y, aspect, amount, bonus, 1.0F, false);
    }
    public static void drawTag(GuiGraphics graphics, int x, int y, Aspect aspect) {
        drawTag(graphics, x, y, aspect, 0.0F, 0, 1.0F, false);
    }
    public static void drawCustomTooltip(GuiGraphics graphics, Font font, List<String> text,
                                         int x, int y, int subTipColor) {
        drawCustomTooltip(graphics, font, text, x, y, subTipColor, false);
    }
    public static void drawCustomTooltip(GuiGraphics graphics, Font font, List<String> text,
                                         int x, int y, int subTipColor, boolean ignoreMouse) {
        if (text == null || text.isEmpty()) {
            return;
        }
        List<Component> lines = new ArrayList<>(text.size());
        for (int i = 0; i < text.size(); i++) {
            String line = text.get(i).replace("@@", "");
            if (subTipColor != -99) {
                line = (i == 0 ? "\u00a7" + Integer.toHexString(subTipColor) : "\u00a77") + line;
            }
            lines.add(Component.literal(line));
        }
        graphics.renderTooltip(font, lines, java.util.Optional.empty(), x, y);
    }
    public static void drawGradientRect(GuiGraphics graphics, int x1, int y1, int x2, int y2,
                                        int colorStart, int colorEnd) {
        graphics.fillGradient(x1, y1, x2, y2, 300, colorStart, colorEnd);
    }
    public static boolean renderItemStack(GuiGraphics graphics, ItemStack stack,
                                          int x, int y, String tooltip) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        graphics.renderItem(stack, x, y);
        if (!hideStackOverlay) {
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y, tooltip);
        }
        return true;
    }
    public static boolean renderItemStackShaded(GuiGraphics graphics, ItemStack stack,
                                                int x, int y, String tooltip, float shade) {
        RenderSystem.setShaderColor(shade, shade, shade, shade);
        try {
            return renderItemStack(graphics, stack, x, y, tooltip);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    public static void renderItemIn2D(PoseStack poseStack, VertexConsumer builder,
                                      TextureAtlasSprite icon, float thickness, int packedLight) {
        renderTextureIn3D(poseStack, builder, icon.getU1(), icon.getV0(),
                icon.getU0(), icon.getV1(), 16, 16, thickness,
                packedLight, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static void renderTextureIn3D(PoseStack poseStack, VertexConsumer builder,
                                         float maxU, float maxV, float minU, float minV,
                                         int width, int height, float thickness,
                                         int packedLight,
                                         float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        quad3D(builder, matrix, packedLight, r, g, b, a,
                0.0F, 0.0F, 0.0F, maxU, minV, 0.0F, 0.0F, 1.0F,
                1.0F, 0.0F, 0.0F, minU, minV,
                1.0F, 1.0F, 0.0F, minU, maxV,
                0.0F, 1.0F, 0.0F, maxU, maxV);
        quad3D(builder, matrix, packedLight, r, g, b, a,
                0.0F, 1.0F, -thickness, maxU, maxV, 0.0F, 0.0F, -1.0F,
                1.0F, 1.0F, -thickness, minU, maxV,
                1.0F, 0.0F, -thickness, minU, minV,
                0.0F, 0.0F, -thickness, maxU, minV);
        float f5 = 0.5F * (maxU - minU) / width;
        float f6 = 0.5F * (minV - maxV) / height;
        for (int k = 0; k < width; k++) {
            float f7 = k / (float) width;
            float f8 = maxU + (minU - maxU) * f7 - f5;
            quad3D(builder, matrix, packedLight, r, g, b, a,
                    f7, 0.0F, -thickness, f8, minV, -1.0F, 0.0F, 0.0F,
                    f7, 0.0F, 0.0F, f8, minV,
                    f7, 1.0F, 0.0F, f8, maxV,
                    f7, 1.0F, -thickness, f8, maxV);
        }
        for (int k = 0; k < width; k++) {
            float f7 = k / (float) width;
            float f8 = maxU + (minU - maxU) * f7 - f5;
            float f9 = f7 + 1.0F / width;
            quad3D(builder, matrix, packedLight, r, g, b, a,
                    f9, 1.0F, -thickness, f8, maxV, 1.0F, 0.0F, 0.0F,
                    f9, 1.0F, 0.0F, f8, maxV,
                    f9, 0.0F, 0.0F, f8, minV,
                    f9, 0.0F, -thickness, f8, minV);
        }
        for (int k = 0; k < height; k++) {
            float f7 = k / (float) height;
            float f8 = minV + (maxV - minV) * f7 - f6;
            float f9 = f7 + 1.0F / height;
            quad3D(builder, matrix, packedLight, r, g, b, a,
                    0.0F, f9, 0.0F, maxU, f8, 0.0F, 1.0F, 0.0F,
                    1.0F, f9, 0.0F, minU, f8,
                    1.0F, f9, -thickness, minU, f8,
                    0.0F, f9, -thickness, maxU, f8);
        }
        for (int k = 0; k < height; k++) {
            float f7 = k / (float) height;
            float f8 = minV + (maxV - minV) * f7 - f6;
            quad3D(builder, matrix, packedLight, r, g, b, a,
                    1.0F, f7, 0.0F, minU, f8, 0.0F, -1.0F, 0.0F,
                    0.0F, f7, 0.0F, maxU, f8,
                    0.0F, f7, -thickness, maxU, f8,
                    1.0F, f7, -thickness, minU, f8);
        }
    }
    public static void renderTextureIn3D(PoseStack poseStack, VertexConsumer builder,
                                         float maxU, float maxV, float minU, float minV,
                                         int width, int height, float thickness,
                                         int packedLight) {
        renderTextureIn3D(poseStack, builder, maxU, maxV, minU, minV, width, height,
                thickness, packedLight, 1.0F, 1.0F, 1.0F, 1.0F);
    }
    private static void quad3D(VertexConsumer builder, Matrix4f matrix, int packedLight,
                               float r, float g, float b, float a,
                               float x1, float y1, float z1, float u1, float v1,
                               float nx, float ny, float nz,
                               float x2, float y2, float z2, float u2, float v2,
                               float x3, float y3, float z3, float u3, float v3,
                               float x4, float y4, float z4, float u4, float v4) {
        builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(nx, ny, nz).endVertex();
        builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(u2, v2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(nx, ny, nz).endVertex();
        builder.vertex(matrix, x3, y3, z3).color(r, g, b, a).uv(u3, v3)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(nx, ny, nz).endVertex();
        builder.vertex(matrix, x4, y4, z4).color(r, g, b, a).uv(u4, v4)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
                .normal(nx, ny, nz).endVertex();
    }
    public static Vector add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y, a.z + b.z);
    }
    public static Vector sub(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
    }
    public static Vector mul(Vector a, float f) {
        return new Vector(a.x * f, a.y * f, a.z * f);
    }
    public static Vector cross(Vector a, Vector b) {
        return new Vector(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
    }
    public static Vector Add(Vector a, Vector b) {
        return add(a, b);
    }
    public static Vector Sub(Vector a, Vector b) {
        return sub(a, b);
    }
    public static Vector Mul(Vector a, float f) {
        return mul(a, f);
    }
    public static Vector Cross(Vector a, Vector b) {
        return cross(a, b);
    }
    public static class Vector {
        public float x;
        public float y;
        public float z;
        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        public float getX() {
            return x;
        }
        public float getY() {
            return y;
        }
        public float getZ() {
            return z;
        }
        public float norm() {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }
        public Vector normalize() {
            float n = norm();
            return n == 0.0F ? new Vector(0.0F, 0.0F, 0.0F) : new Vector(x / n, y / n, z / n);
        }
    }
}
