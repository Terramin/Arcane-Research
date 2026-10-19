package arcaneresearch.discovered.client;

import arcaneresearch.helper.GuiBlitHelper;
import arcaneresearch.helper.IDsHelper;
import java.util.ArrayList;
import java.util.List;

import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.discovered.table.HexResearchGame;
import arcaneresearch.discovered.table.HexResearchNoteData;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
import arcaneresearch.discovered.table.HexUtils;

/**
 * Отрисовка hex-стола: фон, пул, соединение, лист, ховеры.
 * Вынесено из {@link HexResearchTableScreen} без смены поведения:
 * состояние читается из экрана (package-доступ), константы геометрии
 * и текстур живут здесь.
 *
 * NEOFORGE_PORT (26.3 render-state): GuiGraphics -&gt; GuiGraphicsExtractor;
 * ResourceLocation -&gt; Identifier; RenderSystem-бленд/шейдер-цвет удалены
 * (пайплайны владеют состоянием; полупрозрачные подложки — per-blit tint);
 * pose — pushMatrix/translate/scale 2D + rotate(Axis...rotationDegrees);
 * drawString -&gt; text; renderTooltip -&gt; deferred
 * setComponentTooltipForNextFrame; ручной рендер слотов/переносимого
 * удалён (их рисует фреймворк — иначе двойной рендер); level.random -&gt;
 * getRandom().
 */
@OnlyIn(Dist.CLIENT)
public class HexTableDraw {

    static final Identifier PARCHMENT =
            IDsHelper.mod("textures/misc/parchment3.png");
    static final Identifier TABLE_BG =
            IDsHelper.mod("textures/gui/guiresearchtable2.png");
    static final Identifier HEX1 =
            IDsHelper.mod("textures/gui/hex1.png");
    static final Identifier HEX2 =
            IDsHelper.mod("textures/gui/hex2.png");
    static final Identifier SCRIPT =
            IDsHelper.mod("textures/misc/script.png");
    static final Identifier UNKNOWN =
            IDsHelper.mod("textures/aspects/_unknown.png");
    static final Identifier ORB =
            IDsHelper.mod("textures/misc/particles_1710.png");

    static final int SHEET_X = 94;
    static final int SHEET_Y = 8;
    static final int SHEET = 150;
    static final int CX = 169;
    static final int CY = 83;
    static final int HEX = 9;
    static final int POOL_X = 10;
    static final int POOL_Y = 40;

    /** Формат количества — паритет UtilsFX.myFormatter ("#######.##"). */
    private static final java.text.DecimalFormat POOL_AMOUNT_FORMAT =
            new java.text.DecimalFormat("#######.##");

    private static class Rune {
        int q;
        int r;
        long start;
        long decay;
        int idx;
    }

    private final HexResearchTableScreen s;

    private final java.util.HashMap<String, HexUtils.Hex[]> lines = new java.util.HashMap<>();
    private final ArrayList<String> highlight = new ArrayList<>();
    private final java.util.HashMap<String, Rune> runes = new java.util.HashMap<>();
    private long lastRuneCheck;

    public HexTableDraw(HexResearchTableScreen screen) {
        this.s = screen;
    }

    /**
     * Фон стола 255x167 + нижняя панель 184x88 — тот же
     * {@code guiresearchtable2.png} (скопирован из 1.7.10), что и в оригинале.
     */
    void drawTableBackground(GuiGraphicsExtractor graphics) {
        GuiBlitHelper.blit(graphics, TABLE_BG, s.guiLeft(), s.guiTop(), 0, 0, 255, 167, 256, 256);
        GuiBlitHelper.blit(graphics, TABLE_BG, s.guiLeft() + 40, s.guiTop() + 167, 0, 166, 184, 88, 256, 256);
    }

    /** Кнопка копирования (регион 232,200), только при complete. */
    void drawDupe(GuiGraphicsExtractor graphics) {
        if (s.note != null && s.note.complete) {
            GuiBlitHelper.blit(graphics, TABLE_BG, s.guiLeft() + 37, s.guiTop() + 5, 232, 200, 24, 24, 256, 256);
        }
    }

    void drawPool(GuiGraphicsExtractor graphics, int mx, int my) {
        // Без инструментов пул скрыт целиком.
        if (!s.hasTools()) {
            return;
        }
        // Паритет GuiResearchTable.drawAspects (строка 685): сетка 5 колонок
        // (xx=drawn/5*16, yy=drawn%5*16), страница 25 шт со сдвигом page*5,
        // faded — ЦВЕТНОЙ с alpha 0.33 (bw=false!), бонус — с тайла.
        List<Aspect> order = s.poolView();
        int drawn = 0;
        int shown = 0;
        for (Aspect aspect : order) {
            if (drawn < s.page * 5) {
                drawn++;
                continue;
            }
            if (shown >= 25) {
                break;
            }
            int x = s.guiLeft() + POOL_X + shown / 5 * 16;
            int y = s.guiTop() + POOL_Y + shown % 5 * 16;
            int amount = s.shownAmount(aspect);
            int bonus = s.shownBonus(aspect);
            boolean faded = amount <= 0 && bonus <= 0;
            drawPoolTag(graphics, x, y, aspect, amount, bonus, faded ? 0.33F : 1.0F);
            drawn++;
            shown++;
        }
    }

    /**
     * Порт {@code UtilsFX.drawTag(double...)} для пула: цветная иконка 16x16
     * с alpha + количество мелким шрифтом (scale 0.5, прижато вправо-вниз,
     * тёмная обводка крестом + белый текст — ветвь {@code blend > 1}).
     */
    private void drawPoolTag(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect,
            int amount, int bonus, float alpha) {
        AspectRenderer.drawAspect(graphics, x, y, aspect, 0, 0, alpha, false);
        if (amount > 0) {
            String text = POOL_AMOUNT_FORMAT.format(amount);
            int sw = s.font().width(text);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) x, (float) y);
            graphics.pose().scale(0.5F, 0.5F);
            int tx = 32 - sw;
            int ty = 32 - s.font().lineHeight;
            graphics.text(s.font(), text, tx - 1, ty, 0x000000, false);
            graphics.text(s.font(), text, tx + 1, ty, 0x000000, false);
            graphics.text(s.font(), text, tx, ty - 1, 0x000000, false);
            graphics.text(s.font(), text, tx, ty + 1, 0x000000, false);
            graphics.text(s.font(), text, tx, ty, 0xFFFFFF, false);
            graphics.pose().popMatrix();
        }
        if (bonus > 0) {
            // Бонус тайла: мелкий "+N" слева вверху (паритет счётчика
            // PacketAspectPool-ветви; искра-партикл здесь — заливка текстом).
            String text = "+" + bonus;
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) x, (float) y);
            graphics.pose().scale(0.5F, 0.5F);
            graphics.text(s.font(), text, 1, 1, 0x000000, false);
            graphics.text(s.font(), text, 0, 0, 0xFFFF00, false);
            graphics.pose().popMatrix();
        }
    }

    void drawCombineUi(GuiGraphicsExtractor graphics) {
        // Кнопка соединения и стрелки — регионы guiresearchtable2.png.
        int bx = s.guiLeft() + 35;
        int by = s.guiTop() + 139;
        boolean armed = s.select1 != null && s.select2 != null;
        GuiBlitHelper.blit(graphics, TABLE_BG, bx, by, 184, 184, 32, 16, 256, 256);
        if (armed && System.nanoTime() - s.lastCombineClick < 200000000L) {
            GuiBlitHelper.blit(graphics, TABLE_BG, bx, by, 184, 168, 32, 16, 256, 256);
        }
        // Орб на кнопке — тинтом, как светящийся спрайт оригинала
        // (аддитивный бленд пайплайна недостижим — обычный GUI_TEXTURED).
        drawOrbSprite(graphics, bx + 8, by, rainbowOrb(orbTick(), bx, by));
        // Стрелки страниц.
        if (s.page < s.lastPage(s.poolView().size())) {
            GuiBlitHelper.blit(graphics, TABLE_BG, s.guiLeft() + 51, s.guiTop() + 121, 208, 208, 24, 8, 256, 256);
        }
        if (s.page > 0) {
            GuiBlitHelper.blit(graphics, TABLE_BG, s.guiLeft() + 27, s.guiTop() + 121, 184, 208, 24, 8, 256, 256);
        }
        // Слоты соединения: только иконки, как в оригинале.
        drawSelectSlot(graphics, s.guiLeft() + 13, s.guiTop() + 139, s.select1);
        drawSelectSlot(graphics, s.guiLeft() + 71, s.guiTop() + 139, s.select2);
    }

    private void drawSelectSlot(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect) {
        if (aspect != null) {
            AspectRenderer.drawAspect(graphics, x, y, aspect);
        }
    }

    /**
     * Орб drawOrb 1-в-1: кадр {@code part=ticks%8} атласа частиц
     * ({@code u=0.5+part/8, v=0.5, 1/16}), тинт цветом, 16x16.
     */
    private void drawOrbSprite(GuiGraphicsExtractor graphics, int x, int y, int rgb) {
        int part = (int) (orbTick() % 8);
        int u = 128 + part * 32;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ORB, x, y, u, 128, 16, 16, 256, 256, rgb);
    }

    private static int rainbowOrb(long tick, float x, float y) {
        float r = 0.7F + (float) Math.sin((tick + x) / 10.0F) * 0.15F + 0.15F;
        float g = 0.7F + (float) Math.sin((tick + x + y) / 11.0F) * 0.15F + 0.15F;
        float b = 0.7F + (float) Math.sin((tick + y) / 12.0F) * 0.15F + 0.15F;
        return (0xFF << 24) | ((int) (r * 255.0F) << 16) | ((int) (g * 255.0F) << 8) | (int) (b * 255.0F);
    }

    private long orbTick() {
        Player player = s.player();
        if (player != null) {
            return player.tickCount;
        }
        return gameTick();
    }

    private long gameTick() {
        return s.minecraft() != null && s.minecraft().level != null
                ? s.minecraft().level.getGameTime() : 0;
    }

    void drawSheet(GuiGraphicsExtractor graphics, int mx, int my) {
        HexResearchNoteData note = s.note;
        int sx = s.guiLeft() + SHEET_X;
        int sy = s.guiTop() + SHEET_Y;
        GuiBlitHelper.blit(graphics, PARCHMENT, sx, sy, 0, 0, SHEET, SHEET, 256, 256);
        // Без записки — пустой пергамент (пул и соединение живут отдельно).
        if (note == null) {
            return;
        }

        tickRunes();
        HexResearchGame.visualize(note, s::known, lines, highlight);

        // Линии связей.
        long tick = s.minecraft() != null && s.minecraft().level != null
                ? s.minecraft().level.getGameTime() : 0;
        for (HexUtils.Hex[] pair : lines.values()) {
            HexUtils.Pixel p1 = pair[0].toPixel(HEX);
            HexUtils.Pixel p2 = pair[1].toPixel(HEX);
            drawLine(graphics, (float) (s.guiLeft() + CX + p1.x), (float) (s.guiTop() + CY + p1.y),
                    (float) (s.guiLeft() + CX + p2.x), (float) (s.guiTop() + CY + p2.y), tick);
        }

        HexUtils.Hex hover = s.sheetHex(mx, my);
        if (!note.complete) {
            // Паритет drawHex/drawHexHighlight: подложка всегда (белым
            // с alpha 0.25 — тинт), аддитивная подсветка HEX2 при ховере.
            for (String k : note.hexes.keySet()) {
                HexUtils.Hex hex = note.hexes.get(k);
                HexEntry entry = note.hexEntries.get(k);
                if (hex == null || entry == null || entry.type == 1) {
                    continue;
                }
                HexUtils.Pixel pix = hex.toPixel(HEX);
                int hx = (int) (s.guiLeft() + CX + pix.x);
                int hy = (int) (s.guiTop() + CY + pix.y);
                graphics.blit(RenderPipelines.GUI_TEXTURED, HEX1, hx - 8, hy - 8,
                        0, 0, 16, 16, 16, 16, 0x40FFFFFF);
                if (hover != null && hover.equals(hex)) {
                    GuiBlitHelper.blit(graphics, HEX2, hx - 8, hy - 8, 0, 0, 16, 16, 16, 16);
                }
            }
        }

        // Якоря и руны.
        for (String k : note.hexEntries.keySet()) {
            HexEntry entry = note.hexEntries.get(k);
            HexUtils.Hex hex = note.hexes.get(k);
            if (entry == null || hex == null || entry.aspect == null) {
                continue;
            }
            HexUtils.Pixel pix = hex.toPixel(HEX);
            int ix = (int) (s.guiLeft() + CX + pix.x) - 8;
            int iy = (int) (s.guiTop() + CY + pix.y) - 8;
            boolean bright = entry.type == 1 || highlight.contains(k);
            if (!s.known(entry.aspect)) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN, ix, iy,
                        0, 0, 16, 16, 16, 16, 0x80000000);
            } else {
                // Орбы якорей — только незавершённой (первый цикл
                // drawSheet под if(!complete); теги ниже — всегда).
                if (entry.type == 1 && !note.complete) {
                    // Радужный орб якоря под иконкой, как drawOrb оригинала.
                    drawOrbSprite(graphics, ix, iy, rainbowOrb(orbTick(), ix, iy));
                }
                if (bright) {
                    drawSheetAspect(graphics, ix, iy, entry.aspect, 1.0F, false);
                } else {
                    drawSheetAspect(graphics, ix, iy, entry.aspect, 0.66F, true);
                }
            }
        }

        // Декоративные руны (script.png: 16 кадров в ряд).
        for (Rune rune : runes.values()) {
            HexUtils.Pixel pix = new HexUtils.Hex(rune.q, rune.r).toPixel(HEX);
            long now = System.currentTimeMillis();
            float progress = (float) (now - rune.start) / Math.max(1, rune.decay - rune.start);
            float alpha = progress < 0.25F ? progress * 2.0F
                    : progress > 0.5F ? (1.0F - progress) : 0.5F;
            alpha *= 0.66F;
            int tint = ((int) (Math.max(0.0F, alpha) * 255.0F) << 24);
            graphics.pose().pushMatrix();
            graphics.pose().translate((float) (s.guiLeft() + CX + pix.x), (float) (s.guiTop() + CY + pix.y));
            // NEOFORGE_PORT: 2D pose rotate takes radians float (cf. HudHandler).
            graphics.pose().rotate((float) Math.toRadians(-90.0F));
            graphics.blit(RenderPipelines.GUI_TEXTURED, SCRIPT, -5, -5,
                    rune.idx * 16, 0, 10, 10, 256, 16, tint);
            graphics.pose().popMatrix();
        }
    }

    private void tickRunes() {
        HexResearchNoteData note = s.note;
        long now = System.currentTimeMillis();
        runes.values().removeIf(rune -> rune.decay < now);
        if (now - lastRuneCheck < 250 || s.minecraft() == null || note == null) {
            return;
        }
        lastRuneCheck = now;
        double rx = (s.minecraft().level != null
                ? s.minecraft().level.getRandom().nextDouble() : Math.random()) * 120.0 - 60.0;
        double ry = (s.minecraft().level != null
                ? s.minecraft().level.getRandom().nextDouble() : Math.random()) * 120.0 - 60.0;
        HexUtils.Hex hex = new HexUtils.Pixel(rx, ry).toHex(HEX);
        String k = hex.toString();
        if (!note.hexes.containsKey(k) || runes.containsKey(k)) {
            return;
        }
        // Паритет drawSheet: руна не кладётся только на занятый гекс
        // (!hexes) и поверх существующей; aspect в hexEntries не проверяется.
        Rune rune = new Rune();
        rune.q = hex.q;
        rune.r = hex.r;
        rune.start = now;
        rune.decay = now + 15000 + (long) (Math.random() * 10000.0);
        rune.idx = (int) (Math.random() * 16.0);
        runes.put(k, rune);
    }

    /**
     * Иконка аспекта на листе: на ~15% меньше клетки (scale 0.85 от центра),
     * чтобы не сливаться с подложкой гекса.
     */
    private void drawSheetAspect(GuiGraphicsExtractor graphics, int x, int y, Aspect aspect,
            float alpha, boolean grayscale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8.0F, y + 8.0F);
        graphics.pose().scale(0.85F, 0.85F);
        graphics.pose().translate(-8.0F, -8.0F);
        AspectRenderer.drawAspect(graphics, 0, 0, aspect, 0, 0, alpha, grayscale);
        graphics.pose().popMatrix();
    }

    /** Линия связи повёрнутой заливкой (без GL-линий). */
    private void drawLine(GuiGraphicsExtractor graphics, float x1, float y1, float x2, float y2, long tick) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0F) {
            return;
        }
        float pulse = 0.3F + (float) Math.sin(tick + x1) * 0.3F + 0.3F;
        int alpha = Math.min(255, (int) (pulse * 255.0F));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x1, y1);
        // NEOFORGE_PORT: 2D pose rotate takes radians float (cf. HudHandler).
        graphics.pose().rotate((float) Math.atan2(dy, dx));
        graphics.fill(0, -1, (int) len, 2, (alpha << 24) | 0x0099CC);
        graphics.pose().popMatrix();
    }

    /** Перетаскиваемый орб в цвете аспекта (паритет drawOrb, без иконки). */
    void renderDragOrb(GuiGraphicsExtractor graphics) {
        if (s.dragging && s.dragged != null) {
            drawOrbSprite(graphics, (int) s.dragX - 8, (int) s.dragY - 8, s.dragged.getColor());
        }
    }

    void drawHover(GuiGraphicsExtractor graphics, int mx, int my) {
        // Тултипы пула.
        List<Aspect> order = s.poolView();
        int drawn = 0;
        int shown = 0;
        for (Aspect aspect : order) {
            if (drawn < s.page * 5) {
                drawn++;
                continue;
            }
            if (shown >= 25) {
                break;
            }
            int x = s.guiLeft() + POOL_X + shown / 5 * 16;
            int y = s.guiTop() + POOL_Y + shown % 5 * 16;
            if (s.inZone(mx, my, x, y, 16, 16)) {
                ArrayList<Component> tip = new ArrayList<>();
                tip.add(Component.literal(aspect.getName()));
                tip.add(Component.literal(aspect.getLocalizedDescription()));
                // Откуда бонус: игроку не видно, что +N — это окружение стола.
                if (s.shownBonus(aspect) > 0) {
                    tip.add(Component.translatable("tooltip.arcaneresearch.hexbonus",
                            s.shownBonus(aspect)));
                }
                // Паритет drawAspectText: тултип в (mx, my-8).
                graphics.setComponentTooltipForNextFrame(s.font(), tip, mx, my - 8);
                // Рецепт компонентов, как drawAspectText, но без _back-подложек.
                if (!aspect.isPrimal() && aspect.getComponents() != null
                        && aspect.getComponents().length >= 2) {
                    // Высота бокса тултипа (формула ваниллы: 8 + (строк-1)*10,
                    // строки с переносом по 170): иконки — под боксом, иначе
                    // тултип их перекрывает.
                    int textLines = 0;
                    for (net.minecraft.network.chat.Component line : tip) {
                        textLines += Math.max(1, s.font().split(line, 170).size());
                    }
                    int boxH = 8 + Math.max(0, textLines - 1) * 10;
                    int iy = Math.max(my + 8, my - 20 + boxH + 4);
                    Aspect[] comp = aspect.getComponents();
                    if (s.known(comp[0])) {
                        AspectRenderer.drawAspect(graphics, mx + 8, iy, comp[0]);
                    } else {
                        GuiBlitHelper.blit(graphics, UNKNOWN, mx + 8, iy, 0, 0, 16, 16, 16, 16);
                    }
                    if (s.known(comp[1])) {
                        AspectRenderer.drawAspect(graphics, mx + 26, iy, comp[1]);
                    } else {
                        GuiBlitHelper.blit(graphics, UNKNOWN, mx + 26, iy, 0, 0, 16, 16, 16, 16);
                    }
                }
                return;
            }
            drawn++;
            shown++;
        }
        // Тултипы слотов соединения (паритет drawAspectText: select1/select2).
        if (s.select1 != null && s.inZone(mx, my, s.guiLeft() + 13, s.guiTop() + 139, 16, 16)) {
            graphics.setComponentTooltipForNextFrame(s.font(), aspectTip(s.select1), mx, my - 8);
            return;
        }
        if (s.select2 != null && s.inZone(mx, my, s.guiLeft() + 71, s.guiTop() + 139, 16, 16)) {
            graphics.setComponentTooltipForNextFrame(s.font(), aspectTip(s.select2), mx, my - 8);
            return;
        }
        // Тултипы рун на листе.
        HexUtils.Hex hex = s.sheetHex(mx, my);
        if (hex != null && s.note != null) {
            HexEntry entry = s.note.hexEntries.get(hex.toString());
            if (entry != null && entry.aspect != null) {
                graphics.setTooltipForNextFrame(s.font(),
                        Component.literal(entry.aspect.getName()), mx, my);
            }
        }
    }

    /** Строки тултипа аспекта (имя + описание). */
    private static ArrayList<Component> aspectTip(Aspect aspect) {
        ArrayList<Component> tip = new ArrayList<>();
        tip.add(Component.literal(aspect.getName()));
        tip.add(Component.literal(aspect.getLocalizedDescription()));
        return tip;
    }

    /**
     * Диагностика инверсии: с зажатым Shift показывает гекс, который
     * вернул хит-тест под курсором.
     *
     * @deprecated помечено на удаление: отладочный оверлей, никем не вызывается.
     */
    @Deprecated(forRemoval = true)
    void drawPickDebug(GuiGraphicsExtractor graphics, int mx, int my) {
        if (!Minecraft.getInstance().hasShiftDown() || s.note == null) {
            return;
        }
        HexUtils.Hex hex = s.sheetHex(mx, my);
        int dx = mx - (s.guiLeft() + CX);
        int dy = my - (s.guiTop() + CY);
        String text = "pick=" + (hex == null ? "--" : hex.q + ":" + hex.r)
                + " d=" + dx + "," + dy;
        graphics.text(s.font(), text, mx + 12, my - 12, 0xFFFF55, true);
    }

    /** No-ink предупреждение (порт; чернила — слот 0 меню). */
    void drawInkWarning(GuiGraphicsExtractor graphics) {
        ItemStack tools;
        try {
            tools = s.menu.getSlot(0).getItem();
        } catch (Exception e) {
            return;
        }
        if (!tools.isEmpty() && tools.getDamageValue() < tools.getMaxDamage()) {
            return;
        }
        java.util.List<Component> tip = new java.util.ArrayList<>();
        tip.add(Component.literal("No scribing tools"));
        tip.add(Component.literal("Place tools in the top-left slot"));
        int w = 0;
        for (Component line : tip) {
            w = Math.max(w, s.font().width(line));
        }
        graphics.setComponentTooltipForNextFrame(s.font(), tip,
                s.guiLeft() + 157 - w, s.guiTop() + 84);
    }
}
