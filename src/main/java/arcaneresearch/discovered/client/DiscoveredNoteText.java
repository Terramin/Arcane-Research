package arcaneresearch.discovered.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

/**
 * Текст на пергаменте: stage-тексты TC6-формата содержат {@code <BR>} как перенос
 * (иначе буквы BR видны на пергаменте) и {@code <IMG>...</IMG>} как картинку
 * для книги (на пергаменте ей не место — в оригиналах TC3/reforged longText
 * вообще без тегов, поэтому вырезаем). Низ не режется: масштаб и ширина
 * переноса подбираются совместно (shrink-to-fit) чтобы текст влез в пергамент.
 */
public final class DiscoveredNoteText {

    private DiscoveredNoteText() {
    }

    /** Строки + масштаб, подобранные совместно. */
    public record Fit(List<FormattedCharSequence> lines, float scale) {
    }

    /**
     * Разбить описание на строки для пергамента.
     *
     * @param font      шрифт
     * @param desc      описание (переведённый текст с {@code <BR>})
     * @param scrambled шифровать (obfuscated) как нерасшифрованное
     * @param wrapWidth ширина переноса
     * @param maxLines  максимум строк (влезть в пергамент)
     */
    public static List<FormattedCharSequence> parchmentLines(Font font, Component desc,
            boolean scrambled, int wrapWidth, int maxLines) {
        List<FormattedCharSequence> out = splitParas(font, desc, scrambled, wrapWidth);
        if (maxLines >= 0 && out.size() > maxLines) {
            return new ArrayList<>(out.subList(0, maxLines));
        }
        return out;
    }

    /**
     * Подобрать максимальный масштаб (от базы вниз до минимума), при котором
     * текст влезает в высоту. Ширина переноса растёт с усадкой
     * ({@code baseWrap * baseScale / scale}), чтобы строки занимали всю ширину
     * пергамента, а не переносились раньше времени.
     *
     * @param baseWrap  ширина переноса при базовом масштабе (рука 180, попап 130)
     * @param baseScale базовый масштаб (рука 0.63, попап 1.0)
     * @param availH    доступная высота в единицах пергамента
     * @param minScale  минимальный масштаб (дальше — обрезка низа)
     */
    public static Fit parchmentFit(Font font, Component desc, boolean scrambled,
            int baseWrap, float baseScale, float availH, float minScale) {
        if (desc == null || availH <= 0.0F) {
            return new Fit(List.of(), minScale);
        }
        int lh = font.lineHeight;
        List<FormattedCharSequence> lines = splitParas(font, desc, scrambled, baseWrap);
        if (lines.size() * lh * baseScale <= availH) {
            return new Fit(lines, baseScale);
        }
        // Бинарный поиск максимального влезающего масштаба.
        // fits(scale) монотонен: меньше масштаб -> шире перенос -> не больше строк.
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
        int maxFit = (int) (availH / (lh * lo));
        if (best.size() > maxFit) {
            best = new ArrayList<>(best.subList(0, Math.max(0, maxFit)));
        }
        return new Fit(best, lo);
    }

    private static int wrapFor(int baseWrap, float baseScale, float scale) {
        return Math.min(512, Math.max(24, Math.round(baseWrap * baseScale / scale)));
    }

    private static List<FormattedCharSequence> splitParas(Font font, Component desc,
            boolean scrambled, int wrapWidth) {
        List<FormattedCharSequence> out = new ArrayList<>();
        if (desc == null) {
            return out;
        }
        String raw = desc.getString();
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        // Картинки книги на пергаменте не рисуются — вырезать целиком с содержимым.
        raw = raw.replaceAll("(?is)<img\\b[^>]*>.*?</img>", " ");
        for (String para : raw.split("(?i)<br\\s*/?>")) {
            String text = para.strip().replaceAll("\\s+", " ");
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
