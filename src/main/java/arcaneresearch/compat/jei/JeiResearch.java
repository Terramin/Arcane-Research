package arcaneresearch.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.network.chat.Component;

import java.util.List;

public final class JeiResearch {

    private JeiResearch() {
    }

    public static String label(String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) {
            return "";
        }
        try {
            arcaneresearch.api.research.ResearchEntry entry =
                    arcaneresearch.api.research.ResearchCategories.getResearch(researchKey);
            if (entry != null) {
                net.minecraft.network.chat.Component name = entry.getLocalizedName();
                if (name != null) {
                    String s = name.getString();
                    if (!s.isEmpty()) {
                        return s;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return researchKey;
    }

    public static String fittedText(Font font, String researchKey, int width) {
        return fittedText(font, researchKey, width, 4);
    }

    public static String fittedText(Font font, String researchKey, int width, int xMin) {
        if (font == null) {
            return "";
        }
        String text = label(researchKey);
        if (text.isEmpty()) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(10, width - 4 - xMin));
    }

    public static void drawTopRight(GuiGraphicsExtractor guiGraphics, String researchKey, int width) {
        drawTopRight(guiGraphics, researchKey, width, 4);
    }

    public static void drawTopRight(GuiGraphicsExtractor guiGraphics, String researchKey, int width, int xMin) {
        if (guiGraphics == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        String text = fittedText(font, researchKey, width, xMin);
        if (text.isEmpty()) {
            return;
        }
        guiGraphics.text(font, text, width - 4 - font.width(text), 4, 0xFFFFFFA0, false);
    }

    public static List<Component> tooltip(String researchKey, double mouseX, double mouseY, int width) {
        return tooltip(researchKey, mouseX, mouseY, width, 4);
    }

    public static List<Component> tooltip(String researchKey, double mouseX, double mouseY, int width, int xMin) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        String full = label(researchKey);
        if (full.isEmpty()) {
            return List.of();
        }
        String text = font.plainSubstrByWidth(full, Math.max(10, width - 4 - xMin));
        if (text.isEmpty()) {
            return List.of();
        }
        int textWidth = font.width(text);
        if (mouseX >= width - 4 - textWidth && mouseX <= width && mouseY >= 3 && mouseY <= 13) {
            return List.of(Component.translatable("jei.arcaneresearch.requires_research", full));
        }
        return List.of();
    }
}
