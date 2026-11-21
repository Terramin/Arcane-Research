package arcaneresearch.discovered.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public final class DiscoveredNoteText {
    private static final Pattern IMG_PATTERN = Pattern.compile("(?is)<img\\b[^>]*>.*?</img>");
    private static final Pattern BR_PATTERN = Pattern.compile("(?i)<br\\s*/?>");
    private static final Pattern WS_PATTERN = Pattern.compile("\\s+");

    private static final int CACHE_LIMIT = 16;
    private static final Map<LinesKey, List<FormattedCharSequence>> LINES_CACHE = new LinkedHashMap<>();
    private static final Map<FitKey, Fit> FIT_CACHE = new LinkedHashMap<>();

    private record LinesKey(Font font, String text, boolean scrambled, int wrapWidth, int maxLines) {
    }

    private record FitKey(Font font, String text, boolean scrambled, int baseWrap,
                          float baseScale, float availH, float minScale) {
    }

    private DiscoveredNoteText() {
    }

    public record Fit(List<FormattedCharSequence> lines, float scale) {
    }

    public static List<FormattedCharSequence> parchmentLines(Font font, Component desc,
                                                              boolean scrambled, int wrapWidth, int maxLines) {
        if (font == null || desc == null) {
            return new ArrayList<>();
        }
        String raw = desc.getString();
        LinesKey key = new LinesKey(font, raw, scrambled, wrapWidth, maxLines);
        List<FormattedCharSequence> cached = LINES_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<FormattedCharSequence> lines = splitParas(font, raw, scrambled, wrapWidth);
        if (maxLines >= 0 && lines.size() > maxLines) {
            lines = new ArrayList<>(lines.subList(0, maxLines));
        }
        putCached(LINES_CACHE, key, lines);
        return lines;
    }

    public static Fit parchmentFit(Font font, Component desc, boolean scrambled,
                                   int baseWrap, float baseScale, float availH, float minScale) {
        if (font == null || desc == null || availH <= 0.0F) {
            return new Fit(List.of(), minScale);
        }
        String raw = desc.getString();
        FitKey key = new FitKey(font, raw, scrambled, baseWrap, baseScale, availH, minScale);
        Fit cached = FIT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Fit fit = computeFit(font, raw, scrambled, baseWrap, baseScale, availH, minScale);
        putCached(FIT_CACHE, key, fit);
        return fit;
    }

    private static <K, V> void putCached(Map<K, V> cache, K key, V value) {
        if (cache.size() >= CACHE_LIMIT) {
            Iterator<K> it = cache.keySet().iterator();
            if (it.hasNext()) {
                it.remove();
            }
        }
        cache.put(key, value);
    }

    private static Fit computeFit(Font font, String raw, boolean scrambled,
                                  int baseWrap, float baseScale, float availH, float minScale) {
        int lh = font.lineHeight;
        List<FormattedCharSequence> lines = splitParas(font, raw, scrambled, baseWrap);
        if (lines.size() * lh * baseScale <= availH) {
            return new Fit(lines, baseScale);
        }
        float lo = minScale;
        float hi = baseScale;
        List<FormattedCharSequence> best = splitParas(font, raw, scrambled, wrapFor(baseWrap, baseScale, lo));
        for (int i = 0; i < 10; i++) {
            float mid = (lo + hi) / 2.0F;
            List<FormattedCharSequence> midLines =
                    splitParas(font, raw, scrambled, wrapFor(baseWrap, baseScale, mid));
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

    private static List<FormattedCharSequence> splitParas(Font font, String raw,
                                                          boolean scrambled, int wrapWidth) {
        List<FormattedCharSequence> out = new ArrayList<>();
        if (font == null || raw == null || raw.isEmpty()) {
            return out;
        }
        String stripped = IMG_PATTERN.matcher(raw).replaceAll(" ");
        String[] paras = BR_PATTERN.split(stripped);
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