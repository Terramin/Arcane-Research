package arcaneresearch.discovered.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public final class DiscoveredNoteText {
    private static final Pattern IMG_PATTERN = Pattern.compile("(?is)<img\\b[^>]*>.*?</img>");
    private static final Pattern BR_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern WS_PATTERN = Pattern.compile("\\s+");

    private DiscoveredNoteText() {
    }

    public record Fit(List<FormattedCharSequence> lines, float scale) {
    }

    public static List<FormattedCharSequence> parchmentLines(Font font, Component desc,
                                                             boolean scrambled, int wrapWidth, int maxLines) {
        if (font == null) {
            return new ArrayList<>();
        }
        List<FormattedCharSequence> out = splitParas(font, desc, scrambled, wrapWidth);
        if (maxLines >= 0 && out.size() > maxLines) {
            return new ArrayList<>(out.subList(0, maxLines));
        }
        return out;
    }

    public static Fit parchmentFit(Font font, Component desc, boolean scrambled,
                                   int baseWrap, float baseScale, float availH, float minScale) {
        if (font == null || desc == null || availH <= 0.0F) {
            return new Fit(List.of(), minScale);
        }
        int lh = font.lineHeight;
        List<FormattedCharSequence> lines = splitParas(font, desc, scrambled, baseWrap);
        if (lines.size() * lh * baseScale <= availH) {
            return new Fit(lines, baseScale);
        }
        float lo = minScale;
        float hi = baseScale;
        List<FormattedCharSequence> best = splitParas(font, desc, scrambled, wrapFor(baseWrap, baseScale, lo));
        for (int i = 0; i < 10; i++) {
            float mid = (lo + hi) / 2.0F;
            List<FormattedCharSequence> midLines = splitParas(font, desc, scrambled, wrapFor(baseWrap, baseScale, mid));
            if (midLines.size() * lh * mid <= availH) {
                lo = mid;
                best = midLines;
            } else {
                hi = mid;
            }
        }
        float denom = lh * lo;
        int maxFit = denom > 0.0F ? (int) (availH / denom) : Integer.MAX_VALUE;
        if (maxFit < 0) {
            maxFit = 0;
        }
        if (best.size() > maxFit) {
            best = new ArrayList<>(best.subList(0, maxFit));
        }
        return new Fit(best, lo);
    }

    private static int wrapFor(int baseWrap, float baseScale, float scale) {
        if (!(scale > 0.0F)) {
            return 512;
        }
        float w = baseWrap * baseScale / scale;
        if (!(w > 0.0F)) {
            return 24;
        }
        int rounded = Math.round(w);
        return Math.min(512, Math.max(24, rounded));
    }

    private static List<FormattedCharSequence> splitParas(Font font, Component desc,
                                                          boolean scrambled, int wrapWidth) {
        List<FormattedCharSequence> out = new ArrayList<>();
        if (font == null || desc == null) {
            return out;
        }
        String raw = desc.getString();
        if (raw.isEmpty()) {
            return out;
        }
        raw = IMG_PATTERN.matcher(raw).replaceAll(" ");
        String[] paras = BR_PATTERN.split(raw);
        for (String para : paras) {
            if (para == null) {
                continue;
            }
            String text = WS_PATTERN.matcher(para.strip()).replaceAll(" ");
            if (text.isEmpty()) {
                continue;
            }
            MutableComponent line = Component.literal(text);
            if (scrambled) {
                line.withStyle(s -> s.withObfuscated(true));
            }
            out.addAll(font.split(line, wrapWidth));
        }
        return out;
    }
}