package arcaneresearch.client.lib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import java.text.DecimalFormat;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class AspectRenderer {
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#");
    private static final int ICON_SIZE = 16;
    public static String formatAmount(float amount) {
        if (amount == (int) amount) {
            return String.valueOf((int) amount);
        }
        return AMOUNT_FORMATTER.format(amount);
    }
    public static void drawAspect(GuiGraphics graphics, int x, int y, Aspect aspect) {
        drawAspect(graphics, x, y, aspect, 0, 0, 1.0f, false);
    }
    public static void drawAspect(GuiGraphics graphics, int x, int y, Aspect aspect, float amount) {
        drawAspect(graphics, x, y, aspect, amount, 0, 1.0f, false);
    }
    public static void drawAspect(GuiGraphics graphics, int x, int y, Aspect aspect,
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
        ResourceLocation texture = aspect.getImage();
        renderColoredTexture(graphics, texture, x, y, ICON_SIZE, ICON_SIZE, r, g, b, alpha);
        if (amount > 0) {
            String amountStr = formatAmount(amount);
            int textX = x + ICON_SIZE - font.width(amountStr);
            int textY = y + ICON_SIZE - font.lineHeight;
            graphics.drawString(font, amountStr, textX + 1, textY + 1, 0x000000, false);
            graphics.drawString(font, amountStr, textX, textY, 0xFFFFFF, false);
        }
        if (bonus > 0) {
            graphics.drawString(font, "+", x - 2, y - 4, 0xFFFF00, false);
            if (bonus > 1) {
                graphics.drawString(font, String.valueOf(bonus), x + 4, y - 2, 0xFFFFFF, false);
            }
        }
    }
    public static ResourceLocation strippedTexture(ResourceLocation texture) {
        return UtilsFX.strippedAspectTexture(texture);
    }
    private static void renderColoredTexture(GuiGraphics graphics, ResourceLocation texture,
                                             int x, int y, int width, int height, float r, float g, float b, float a) {
        RenderSystem.setShaderTexture(0, strippedTexture(texture));
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, x, y + height, 0).uv(0, 1).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv(1, 1).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv(1, 0).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x, y, 0).uv(0, 0).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    public static int drawAspectList(GuiGraphics graphics, int x, int y, AspectList aspects, int spacing) {
        if (aspects == null) return x;
        int currentX = x;
        for (Aspect aspect : aspects.getAspects()) {
            int amount = aspects.getAmount(aspect);
            drawAspect(graphics, currentX, y, aspect, amount);
            currentX += ICON_SIZE + spacing;
        }
        return currentX;
    }
    public static void drawAspectListCentered(GuiGraphics graphics, int centerX, int y,
                                              AspectList aspects, int spacing) {
        if (aspects == null || aspects.size() == 0) return;
        int totalWidth = aspects.size() * (ICON_SIZE + spacing) - spacing;
        int startX = centerX - totalWidth / 2;
        drawAspectList(graphics, startX, y, aspects, spacing);
    }
    public static void drawAspectSmall(GuiGraphics graphics, int x, int y, Aspect aspect) {
        if (aspect == null) return;
        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        ResourceLocation texture = aspect.getImage();
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
            GuiGraphics graphics, int x, int y, Aspect aspect, int amount, int mouseX, int mouseY) {
        drawAspect(graphics, x, y, aspect, amount);
        if (isMouseOverAspect(x, y, mouseX, mouseY)) {
            return getAspectTooltip(aspect, amount);
        }
        return null;
    }
}
