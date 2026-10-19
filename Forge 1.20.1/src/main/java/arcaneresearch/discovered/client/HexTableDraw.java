package arcaneresearch.discovered.client;
import arcaneresearch.helper.GuiBlitHelper;
import arcaneresearch.helper.IDsHelper;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.discovered.table.HexResearchGame;
import arcaneresearch.discovered.table.HexResearchNoteData;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
import arcaneresearch.discovered.table.HexUtils;
@OnlyIn(Dist.CLIENT)
public class HexTableDraw {
    static final ResourceLocation PARCHMENT =
            IDsHelper.mod("textures/misc/parchment3.png");
    static final ResourceLocation TABLE_BG =
            IDsHelper.mod("textures/gui/guiresearchtable2.png");
    static final ResourceLocation HEX1 =
            IDsHelper.mod("textures/gui/hex1.png");
    static final ResourceLocation HEX2 =
            IDsHelper.mod("textures/gui/hex2.png");
    static final ResourceLocation SCRIPT =
            IDsHelper.mod("textures/misc/script.png");
    static final ResourceLocation UNKNOWN =
            IDsHelper.mod("textures/aspects/_unknown.png");
    static final ResourceLocation ORB =
            IDsHelper.mod("textures/misc/particles_1710.png");
    static final int SHEET_X = 94;
    static final int SHEET_Y = 8;
    static final int SHEET = 150;
    static final int CX = 169;
    static final int CY = 83;
    static final int HEX = 9;
    static final int POOL_X = 10;
    static final int POOL_Y = 40;
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
    void drawTableBackground(GuiGraphics graphics) {
        GuiBlitHelper.blit(graphics, TABLE_BG, s.left, s.top, 0, 0, 255, 167, 256, 256);
        GuiBlitHelper.blit(graphics, TABLE_BG, s.left + 40, s.top + 167, 0, 166, 184, 88, 256, 256);
    }
    void renderMenuSlots(GuiGraphics graphics, int mx, int my) {
        Slot hovered = null;
        for (Slot slot : s.slots()) {
            int x = s.left + slot.x;
            int y = s.top + slot.y;
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, x, y);
                graphics.renderItemDecorations(s.font(), stack, x, y);
            }
            if (s.inZone(mx, my, x, y, 16, 16)) {
                hovered = slot;
            }
        }
        if (hovered != null) {
            int x = s.left + hovered.x;
            int y = s.top + hovered.y;
            graphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
            if (!hovered.getItem().isEmpty()) {
                graphics.renderTooltip(s.font(), hovered.getItem(), mx, my);
            }
        }
    }
    void renderCarried(GuiGraphics graphics, int mx, int my) {
        ItemStack menuCarried = s.menu.getCarried();
        if (!menuCarried.isEmpty()) {
            graphics.renderItem(menuCarried, mx - 8, my - 8);
            graphics.renderItemDecorations(s.font(), menuCarried, mx - 8, my - 8);
        }
    }
    void drawDupe(GuiGraphics graphics) {
        if (s.note != null && s.note.complete) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            GuiBlitHelper.blit(graphics, TABLE_BG, s.left + 37, s.top + 5, 232, 200, 24, 24, 256, 256);
        }
    }
    void drawPool(GuiGraphics graphics, int mx, int my) {
        if (!s.hasTools()) {
            return;
        }
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
            int x = s.left + POOL_X + shown / 5 * 16;
            int y = s.top + POOL_Y + shown % 5 * 16;
            int amount = s.shownAmount(aspect);
            int bonus = s.shownBonus(aspect);
            boolean faded = amount <= 0 && bonus <= 0;
            drawPoolTag(graphics, x, y, aspect, amount, bonus, faded ? 0.33F : 1.0F);
            drawn++;
            shown++;
        }
    }
    private void drawPoolTag(GuiGraphics graphics, int x, int y, Aspect aspect,
            int amount, int bonus, float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        AspectRenderer.drawAspect(graphics, x, y, aspect, 0, 0, alpha, false);
        if (amount > 0) {
            String text = POOL_AMOUNT_FORMAT.format(amount);
            int sw = s.font().width(text);
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            int tx = 32 - sw;
            int ty = 32 - s.font().lineHeight;
            graphics.drawString(s.font(), text, tx - 1, ty, 0x000000, false);
            graphics.drawString(s.font(), text, tx + 1, ty, 0x000000, false);
            graphics.drawString(s.font(), text, tx, ty - 1, 0x000000, false);
            graphics.drawString(s.font(), text, tx, ty + 1, 0x000000, false);
            graphics.drawString(s.font(), text, tx, ty, 0xFFFFFF, false);
            graphics.pose().popPose();
        }
        if (bonus > 0) {
            String text = "+" + bonus;
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.drawString(s.font(), text, 1, 1, 0x000000, false);
            graphics.drawString(s.font(), text, 0, 0, 0xFFFF00, false);
            graphics.pose().popPose();
        }
    }
    void drawCombineUi(GuiGraphics graphics) {
        int bx = s.left + 35;
        int by = s.top + 139;
        boolean armed = s.select1 != null && s.select2 != null;
        GuiBlitHelper.blit(graphics, TABLE_BG, bx, by, 184, 184, 32, 16, 256, 256);
        if (armed && System.nanoTime() - s.lastCombineClick < 200000000L) {
            GuiBlitHelper.blit(graphics, TABLE_BG, bx, by, 184, 168, 32, 16, 256, 256);
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        drawOrbSprite(graphics, bx + 8, by, rainbowOrb(orbTick(), bx, by));
        RenderSystem.defaultBlendFunc();
        if (s.page < s.lastPage(s.poolView().size())) {
            GuiBlitHelper.blit(graphics, TABLE_BG, s.left + 51, s.top + 121, 208, 208, 24, 8, 256, 256);
        }
        if (s.page > 0) {
            GuiBlitHelper.blit(graphics, TABLE_BG, s.left + 27, s.top + 121, 184, 208, 24, 8, 256, 256);
        }
        drawSelectSlot(graphics, s.left + 13, s.top + 139, s.select1);
        drawSelectSlot(graphics, s.left + 71, s.top + 139, s.select2);
    }
    private void drawSelectSlot(GuiGraphics graphics, int x, int y, Aspect aspect) {
        if (aspect != null) {
            AspectRenderer.drawAspect(graphics, x, y, aspect);
        }
    }
    private void drawOrbSprite(GuiGraphics graphics, int x, int y, int rgb) {
        int part = (int) (orbTick() % 8);
        int u = 128 + part * 32;
        RenderSystem.setShaderColor(((rgb >> 16) & 0xFF) / 255.0F,
                ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F);
        GuiBlitHelper.blit(graphics, ORB, x, y, u, 128, 16, 16, 256, 256);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
    void drawSheet(GuiGraphics graphics, int mx, int my) {
        HexResearchNoteData note = s.note;
        int sx = s.left + SHEET_X;
        int sy = s.top + SHEET_Y;
        GuiBlitHelper.blit(graphics, PARCHMENT, sx, sy, 0, 0, SHEET, SHEET, 256, 256);
        if (note == null) {
            return;
        }
        tickRunes();
        HexResearchGame.visualize(note, s::known, lines, highlight);
        long tick = s.minecraft() != null && s.minecraft().level != null
                ? s.minecraft().level.getGameTime() : 0;
        for (HexUtils.Hex[] pair : lines.values()) {
            HexUtils.Pixel p1 = pair[0].toPixel(HEX);
            HexUtils.Pixel p2 = pair[1].toPixel(HEX);
            drawLine(graphics, (float) (s.left + CX + p1.x), (float) (s.top + CY + p1.y),
                    (float) (s.left + CX + p2.x), (float) (s.top + CY + p2.y), tick);
        }
        HexUtils.Hex hover = s.sheetHex(mx, my);
        if (!note.complete) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (String k : note.hexes.keySet()) {
                HexUtils.Hex hex = note.hexes.get(k);
                HexEntry entry = note.hexEntries.get(k);
                if (hex == null || entry == null || entry.type == 1) {
                    continue;
                }
                HexUtils.Pixel pix = hex.toPixel(HEX);
                int hx = (int) (s.left + CX + pix.x);
                int hy = (int) (s.top + CY + pix.y);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.25F);
                GuiBlitHelper.blit(graphics, HEX1, hx - 8, hy - 8, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (hover != null && hover.equals(hex)) {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(770, 1);
                    GuiBlitHelper.blit(graphics, HEX2, hx - 8, hy - 8, 0, 0, 16, 16, 16, 16);
                    RenderSystem.defaultBlendFunc();
                }
            }
        }
        for (String k : note.hexEntries.keySet()) {
            HexEntry entry = note.hexEntries.get(k);
            HexUtils.Hex hex = note.hexes.get(k);
            if (entry == null || hex == null || entry.aspect == null) {
                continue;
            }
            HexUtils.Pixel pix = hex.toPixel(HEX);
            int ix = (int) (s.left + CX + pix.x) - 8;
            int iy = (int) (s.top + CY + pix.y) - 8;
            boolean bright = entry.type == 1 || highlight.contains(k);
            if (!s.known(entry.aspect)) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 0.5F);
                GuiBlitHelper.blit(graphics, UNKNOWN, ix, iy, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else {
                if (entry.type == 1 && !note.complete) {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(770, 1);
                    drawOrbSprite(graphics, ix, iy, rainbowOrb(orbTick(), ix, iy));
                    RenderSystem.defaultBlendFunc();
                }
                if (bright) {
                    drawSheetAspect(graphics, ix, iy, entry.aspect, 1.0F, false);
                } else {
                    drawSheetAspect(graphics, ix, iy, entry.aspect, 0.66F, true);
                }
            }
        }
        for (Rune rune : runes.values()) {
            HexUtils.Pixel pix = new HexUtils.Hex(rune.q, rune.r).toPixel(HEX);
            long now = System.currentTimeMillis();
            float progress = (float) (now - rune.start) / Math.max(1, rune.decay - rune.start);
            float alpha = progress < 0.25F ? progress * 2.0F
                    : progress > 0.5F ? (1.0F - progress) : 0.5F;
            alpha *= 0.66F;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, Math.max(0.0F, alpha));
            graphics.pose().pushPose();
            graphics.pose().translate(s.left + CX + pix.x, s.top + CY + pix.y, 0.0F);
            graphics.pose().mulPose(Axis.ZP.rotation(-(float) Math.PI / 2.0F));
            GuiBlitHelper.blit(graphics, SCRIPT, -5, -5, rune.idx * 16, 0, 10, 10, 256, 16);
            graphics.pose().popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
                ? s.minecraft().level.random.nextDouble() : Math.random()) * 120.0 - 60.0;
        double ry = (s.minecraft().level != null
                ? s.minecraft().level.random.nextDouble() : Math.random()) * 120.0 - 60.0;
        HexUtils.Hex hex = new HexUtils.Pixel(rx, ry).toHex(HEX);
        String k = hex.toString();
        if (!note.hexes.containsKey(k) || runes.containsKey(k)) {
            return;
        }
        Rune rune = new Rune();
        rune.q = hex.q;
        rune.r = hex.r;
        rune.start = now;
        rune.decay = now + 15000 + (long) (Math.random() * 10000.0);
        rune.idx = (int) (Math.random() * 16.0);
        runes.put(k, rune);
    }
    private void drawSheetAspect(GuiGraphics graphics, int x, int y, Aspect aspect,
            float alpha, boolean grayscale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8.0F, y + 8.0F, 0.0F);
        graphics.pose().scale(0.85F, 0.85F, 1.0F);
        graphics.pose().translate(-8.0F, -8.0F, 0.0F);
        AspectRenderer.drawAspect(graphics, 0, 0, aspect, 0, 0, alpha, grayscale);
        graphics.pose().popPose();
    }
    private void drawLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, long tick) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0F) {
            return;
        }
        float pulse = 0.3F + (float) Math.sin(tick + x1) * 0.3F + 0.3F;
        int alpha = Math.min(255, (int) (pulse * 255.0F));
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        graphics.pose().pushPose();
        graphics.pose().translate(x1, y1, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(dy, dx)));
        graphics.fill(0, -1, (int) len, 2, (alpha << 24) | 0x0099CC);
        graphics.pose().popPose();
        RenderSystem.defaultBlendFunc();
    }
    void renderDragOrb(GuiGraphics graphics) {
        if (s.dragging && s.dragged != null) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 1);
            drawOrbSprite(graphics, (int) s.dragX - 8, (int) s.dragY - 8, s.dragged.getColor());
            RenderSystem.defaultBlendFunc();
        }
    }
    void drawHover(GuiGraphics graphics, int mx, int my) {
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
            int x = s.left + POOL_X + shown / 5 * 16;
            int y = s.top + POOL_Y + shown % 5 * 16;
            if (s.inZone(mx, my, x, y, 16, 16)) {
                ArrayList<Component> tip = new ArrayList<>();
                tip.add(Component.literal(aspect.getName()));
                tip.add(Component.literal(aspect.getLocalizedDescription()));
                if (s.shownBonus(aspect) > 0) {
                    tip.add(Component.translatable("tooltip.arcaneresearch.hexbonus",
                            s.shownBonus(aspect)));
                }
                graphics.renderTooltip(s.font(), tip, java.util.Optional.empty(), mx, my - 8);
                if (!aspect.isPrimal() && aspect.getComponents() != null
                        && aspect.getComponents().length >= 2) {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
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
        if (s.select1 != null && s.inZone(mx, my, s.left + 13, s.top + 139, 16, 16)) {
            graphics.renderTooltip(s.font(), aspectTip(s.select1),
                    java.util.Optional.empty(), mx, my - 8);
            return;
        }
        if (s.select2 != null && s.inZone(mx, my, s.left + 71, s.top + 139, 16, 16)) {
            graphics.renderTooltip(s.font(), aspectTip(s.select2),
                    java.util.Optional.empty(), mx, my - 8);
            return;
        }
        HexUtils.Hex hex = s.sheetHex(mx, my);
        if (hex != null && s.note != null) {
            HexEntry entry = s.note.hexEntries.get(hex.toString());
            if (entry != null && entry.aspect != null) {
                graphics.renderTooltip(s.font(), Component.literal(entry.aspect.getName()), mx, my);
            }
        }
    }
    private static ArrayList<Component> aspectTip(Aspect aspect) {
        ArrayList<Component> tip = new ArrayList<>();
        tip.add(Component.literal(aspect.getName()));
        tip.add(Component.literal(aspect.getLocalizedDescription()));
        return tip;
    }
    @Deprecated(forRemoval = true)
    void drawPickDebug(GuiGraphics graphics, int mx, int my) {
        if (!Screen.hasShiftDown() || s.note == null) {
            return;
        }
        HexUtils.Hex hex = s.sheetHex(mx, my);
        int dx = mx - (s.left + CX);
        int dy = my - (s.top + CY);
        String text = "pick=" + (hex == null ? "--" : hex.q + ":" + hex.r)
                + " d=" + dx + "," + dy;
        graphics.drawString(s.font(), text, mx + 12, my - 12, 0xFFFF55, true);
    }
    void drawInkWarning(GuiGraphics graphics) {
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
        graphics.renderTooltip(s.font(), tip, java.util.Optional.empty(),
                s.left + 157 - w, s.top + 84);
    }
}
