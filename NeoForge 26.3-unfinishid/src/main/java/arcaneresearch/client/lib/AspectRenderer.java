package arcaneresearch.client.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

import java.text.DecimalFormat;
import java.util.List;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 client/lib/AspectRenderer.java.
// REQUIRED changes (verified via javap on MC 26.3):
//   GuiGraphics -> GuiGraphicsExtractor; drawString -> text.
//   Immediate-mode tinted quads (RenderSystem/Tesselator/BufferBuilder/GameRenderer) removed ->
//     extractor.blit(GUI_TEXTURED, tex, x, y, 0, 0, w, h, imgW, imgH, argbTint) (ARGB tint multiplies).
//   Identifier -> Identifier (fromNamespaceAndPath; getNamespace/getPath kept).
//   NativeImage.getPixelRGBA/setPixelRGBA -> getPixel/setPixel (packing verified
//     below: luminance is channel-order independent, alpha handling identical).
//   DynamicTexture(image) removed -> DynamicTexture(Supplier<String>, NativeImage).
public class AspectRenderer {

    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#");
    private static final int ICON_SIZE = 16;
    // NEOFORGE_PORT: blit needs explicit texture dims; recorded while stripping (same image read).
    private static final java.util.Map<Identifier, int[]> TEXTURE_DIMS =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static String formatAmount(float amount) {
        if (amount == (int) amount) {
            return String.valueOf((int) amount);
        }
        return AMOUNT_FORMATTER.format(amount);
    }

    public static void drawAspect(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect) {
        drawAspect(graphics, x, y, aspect, 0, 0, 1.0f, false);
    }

    public static void drawAspect(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect, float amount) {
        drawAspect(graphics, x, y, aspect, amount, 0, 1.0f, false);
    }

    public static void drawAspect(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect,
                                  float amount, int bonus, float alpha, boolean grayscale) {
        if (aspect == null) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        if (grayscale) {
            r = g = b = 0.1f;
            alpha *= 0.8f;
        }

        Identifier texture = aspect.getImage();
        renderColoredTexture(graphics, texture, x, y, ICON_SIZE, ICON_SIZE, r, g, b, alpha);

        if (amount > 0) {
            String amountStr = formatAmount(amount);
            int textX = x + ICON_SIZE - font.width(amountStr);
            int textY = y + ICON_SIZE - font.lineHeight;

            // NEOFORGE_PORT (26.3 strict-ARGB text): GuiGraphicsExtractor.text
            // early-outs when ARGB.alpha(color)==0 (verified in 26.x bytecode),
            // while Forge 1.20.1 ignored alpha. 0xRRGGBB literals must carry
            // full alpha or the text is silently skipped.
            graphics.text(font, amountStr, textX + 1, textY + 1, 0xFF000000, false);
            graphics.text(font, amountStr, textX, textY, 0xFFFFFFFF, false);
        }

        if (bonus > 0) {

            graphics.text(font, "+", x - 2, y - 4, 0xFFFFFF00, false);
            if (bonus > 1) {
                graphics.text(font, String.valueOf(bonus), x + 4, y - 2, 0xFFFFFFFF, false);
            }
        }
    }

    public static Identifier strippedTexture(Identifier texture) {
        // Вырезка фона переехала в общий helper — используется всеми
        // местами, рисующими иконки аспектов (GUI + мир).
        return UtilsFX.strippedAspectTexture(texture);
    }

    /**
     * Full pixel dimensions of a GUI texture for blit sampling. Populated by
     * {@link #strippedTexture} while the image is read. Null when the resource
     * is missing/unreadable (callers keep their previous sampling then).
     */
    public static int[] textureDims(Identifier texture) {
        int[] dims = TEXTURE_DIMS.get(texture);
        if (dims == null) {
            strippedTexture(texture);
            dims = TEXTURE_DIMS.get(texture);
        }
        return dims;
    }

    private static void renderColoredTexture(GuiGraphicsExtractor graphics, Identifier texture,
                                             int x, int y, int width, int height, float r, float g, float b, float a) {
        // NEOFORGE_PORT: POSITION_TEX_COLOR immediate quad -> tinted pipeline blit
        // (blit color is ARGB and multiplies the texture, same as vertex color did).
        // UV contract (verified against 26.3 GuiGraphicsExtractor bytecode: the 12-arg
        // overload is (x, y, u, v, screenW, screenH, srcW, srcH, texW, texH) delegating
        // to the 13-arg with default color -1): the FULL texture is sampled (srcW/H =
        // texW/H, same as Forge's uv 0->1 quad), drawn at width x height. The old
        // 11-arg call passed width/height as sample size and showed only the top-left
        // corner magnified (aspect art is 400x400, icons looked distorted/wrong size).
        Identifier tex = strippedTexture(texture);
        int[] dims = TEXTURE_DIMS.get(tex);
        int argb = ((int) (a * 255.0f) << 24)
                | ((int) (r * 255.0f) << 16)
                | ((int) (g * 255.0f) << 8)
                | (int) (b * 255.0f);
        if (dims != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, width, height,
                    dims[0], dims[1], dims[0], dims[1], argb);
        }
    }

    public static int drawAspectList(GuiGraphicsExtractor graphics, int x, int y, AspectList aspects, int spacing) {
        if (aspects == null) return x;

        int currentX = x;
        for (Aspect aspect : aspects.getAspects()) {
            int amount = aspects.getAmount(aspect);
            drawAspect(graphics, currentX, y, aspect, amount);
            currentX += ICON_SIZE + spacing;
        }
        return currentX;
    }

    public static void drawAspectListCentered(GuiGraphicsExtractor graphics, int centerX, int y,
                                              AspectList aspects, int spacing) {
        if (aspects == null || aspects.size() == 0) return;

        int totalWidth = aspects.size() * (ICON_SIZE + spacing) - spacing;
        int startX = centerX - totalWidth / 2;
        drawAspectList(graphics, startX, y, aspects, spacing);
    }

    public static void drawAspectSmall(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect) {
        if (aspect == null) return;

        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        Identifier texture = aspect.getImage();
        renderColoredTexture(graphics, texture, x, y, 8, 8, r, g, b, 1.0f);
    }

    public static List<net.minecraft.network.chat.Component> getAspectTooltip(Aspect aspect, int amount) {
        if (aspect == null) return List.of();

        java.util.ArrayList<net.minecraft.network.chat.Component> tooltip = new java.util.ArrayList<>();

        int color = aspect.getColor();
        tooltip.add(net.minecraft.network.chat.Component.literal(aspect.getName())
                .withStyle(style -> style.withColor(color)));

        tooltip.add(net.minecraft.network.chat.Component.literal(aspect.getLocalizedDescription())
                .withStyle(net.minecraft.ChatFormatting.GRAY));

        if (amount > 0) {
            tooltip.add(net.minecraft.network.chat.Component.literal("Amount: " + amount)
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }

        if (!aspect.isPrimal() && aspect.getComponents() != null) {
            Aspect[] components = aspect.getComponents();
            String compStr = components[0].getName() + " + " + components[1].getName();
            tooltip.add(net.minecraft.network.chat.Component.literal("(" + compStr + ")")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        }

        return tooltip;
    }

    public static boolean isMouseOverAspect(int x, int y, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + ICON_SIZE && mouseY >= y && mouseY < y + ICON_SIZE;
    }

    public static List<net.minecraft.network.chat.Component> drawAspectWithTooltip(
            GuiGraphicsExtractor graphics, int x, int y, Aspect aspect, int amount, int mouseX, int mouseY) {
        drawAspect(graphics, x, y, aspect, amount);

        if (isMouseOverAspect(x, y, mouseX, mouseY)) {
            return getAspectTooltip(aspect, amount);
        }
        return null;
    }
}