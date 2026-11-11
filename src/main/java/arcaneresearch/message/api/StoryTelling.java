package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StoryTelling {

    private static final ArrayDeque<Queued> QUEUE = new ArrayDeque<>();
    private static final int MAX_QUEUE = 16;
    private static final int FADE_IN = 8;
    private static final int FADE_OUT = 12;
    private static final int GAP_TICKS = 4;
    private static final int MAX_WIDTH = 240;
    private static final int PADDING = 4;
    private static final int ICON_SIZE = 16;
    private static final int ICON_TEXT_SPACING = 6;
    private static final int TITLE_TEXT_GAP = 2;
    private static final int BG_COLOR = 0x80000000;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int HUD_ABOVE_HOTBAR_HEIGHT = 28;
    private static final int HUD_GAP = 8;
    private static final int MIN_TOP_MARGIN = 10;
    private static final float SHAKE_AMPLITUDE_X = 2.0f;
    private static final float SHAKE_FREQ_X = 0.7f;
    private static final float TYPEWRITER_DURATION_FRACTION = 0.7f;

    private static StoryMessage current;
    private static UUID currentId;
    private static int ageTicks;
    private static int gapTicks;
    private static long clientTicks;

    private static AckSender ackSender;

    private static final Map<UUID, BiConsumer<UUID, DeliveryStatus>> LOCAL_PENDING = new HashMap<>();

    private static List<FormattedCharSequence> cachedTitleLines;
    private static List<FormattedCharSequence> cachedTextLines;
    private static int[] cachedLineLengths;
    private static int cachedTotalChars;
    private static int cachedCharsPerTick;
    private static boolean cachedHasIcon;
    private static int cachedIconBlock;
    private static int cachedBoxWidth;
    private static int cachedBoxHeight;

    private record Queued(StoryMessage msg, UUID id, long enqueuedAt) { }

    public interface AckSender {
        void sendAck(UUID messageId, DeliveryStatus status);
    }

    private StoryTelling() {
    }

    public static void setAckSender(AckSender sender) {
        ackSender = sender;
    }

    public static void receiveFromServer(StoryMessage msg, UUID id) {
        enqueue(msg, id);
    }

    public static void say(StoryMessage msg) {
        say(msg, null);
    }

    public static void say(StoryMessage msg, BiConsumer<UUID, DeliveryStatus> callback) {
        if (msg == null) return;
        UUID id = callback != null ? UUID.randomUUID() : null;
        if (id != null) LOCAL_PENDING.put(id, callback);
        enqueue(msg, id);
    }

    private static void enqueue(StoryMessage msg, UUID id) {
        if (id != null) {
            if (id.equals(currentId)) return;
            for (Queued q : QUEUE) if (id.equals(q.id())) return;
        } else {
            if (current != null && sameContent(current, msg)) return;
            for (Queued q : QUEUE) if (sameContent(q.msg(), msg)) return;
        }
        if (QUEUE.size() >= MAX_QUEUE) {
            Queued evicted = QUEUE.pollFirst();
            notifyDelivery(evicted.id(), DeliveryStatus.DROPPED);
        }
        QUEUE.addLast(new Queued(msg, id, clientTicks));
        pump();
    }

    public static void clear() {
        if (currentId != null) notifyDelivery(currentId, DeliveryStatus.DROPPED);
        for (Queued q : QUEUE) notifyDelivery(q.id(), DeliveryStatus.DROPPED);
        QUEUE.clear();
        current = null;
        currentId = null;
        ageTicks = 0;
        gapTicks = 0;
        clearCache();
        dropAllLocalPending();
    }

    public static void skip() {
        if (current == null) return;
        finishCurrent(DeliveryStatus.SKIPPED, false);
    }

    private static boolean sameContent(StoryMessage a, StoryMessage b) {
        return a.title().getString().equals(b.title().getString())
                && a.text().getString().equals(b.text().getString());
    }

    private static void pump() {
        if (current != null || gapTicks > 0) return;
        while (!QUEUE.isEmpty()) {
            Queued q = QUEUE.pollFirst();
            if (q.msg().maxWaitTicks() >= 0
                    && clientTicks - q.enqueuedAt() > q.msg().maxWaitTicks()) {
                notifyDelivery(q.id(), DeliveryStatus.DROPPED);
                continue;
            }
            current = q.msg();
            currentId = q.id();
            ageTicks = 0;
            cacheMessage(current);
            return;
        }
    }

    private static void finishCurrent(DeliveryStatus status, boolean withGap) {
        UUID id = currentId;
        current = null;
        currentId = null;
        ageTicks = 0;
        gapTicks = withGap ? GAP_TICKS : 0;
        clearCache();
        notifyDelivery(id, status);
        pump();
    }

    private static void notifyDelivery(UUID id, DeliveryStatus status) {
        if (id == null) return;
        var localCb = LOCAL_PENDING.remove(id);
        if (localCb != null) {
            try {
                localCb.accept(id, status);
            } catch (Throwable t) {
                Arcaneresearch.LOGGER.error("Local story callback failed for id={}", id, t);
            }
            return;
        }
        if (ackSender != null) {
            try {
                ackSender.sendAck(id, status);
            } catch (Throwable t) {
                Arcaneresearch.LOGGER.error("Failed to send story ack for id={}", id, t);
            }
        }
    }

    private static void dropAllLocalPending() {
        if (LOCAL_PENDING.isEmpty()) return;
        var snapshot = new ArrayList<>(LOCAL_PENDING.entrySet());
        LOCAL_PENDING.clear();
        for (var e : snapshot) {
            try {
                e.getValue().accept(e.getKey(), DeliveryStatus.DROPPED);
            } catch (Throwable t) {
                Arcaneresearch.LOGGER.error("Local story callback failed on clear id={}", e.getKey(), t);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (Minecraft.getInstance().isPaused()) return;
        clientTicks++;

        if (current == null) {
            if (gapTicks > 0) gapTicks--;
            pump();
            return;
        }
        ageTicks++;
        if (ageTicks >= FADE_IN + current.durationTicks() + FADE_OUT) {
            finishCurrent(DeliveryStatus.SHOWN, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private static void cacheMessage(StoryMessage msg) {
        Minecraft mc = Minecraft.getInstance();
        var font = mc.font;

        cachedHasIcon = !StoryMessage.NONE.equals(msg.icon());
        cachedTitleLines = font.split(msg.title(), MAX_WIDTH);
        cachedTextLines = font.split(msg.text(), MAX_WIDTH);

        int textWidth = 0;
        for (FormattedCharSequence line : cachedTitleLines) textWidth = Math.max(textWidth, font.width(line));
        for (FormattedCharSequence line : cachedTextLines) textWidth = Math.max(textWidth, font.width(line));
        textWidth = Math.min(textWidth, MAX_WIDTH);

        int textHeight = (cachedTitleLines.size() + cachedTextLines.size()) * font.lineHeight;
        if (!cachedTitleLines.isEmpty() && !cachedTextLines.isEmpty()) textHeight += TITLE_TEXT_GAP;

        cachedIconBlock = cachedHasIcon ? ICON_SIZE + ICON_TEXT_SPACING : 0;
        cachedBoxWidth = PADDING * 2 + cachedIconBlock + textWidth;
        cachedBoxHeight = PADDING * 2 + Math.max(ICON_SIZE, textHeight);

        cachedLineLengths = new int[cachedTextLines.size()];
        int total = 0;
        for (int i = 0; i < cachedTextLines.size(); i++) {
            cachedLineLengths[i] = charCount(cachedTextLines.get(i));
            total += cachedLineLengths[i];
        }
        cachedTotalChars = total;

        if (msg.typewriter()) {
            int budgetTicks = Math.max(1, (int) (msg.durationTicks() * TYPEWRITER_DURATION_FRACTION));
            cachedCharsPerTick = Math.max(1, (total + budgetTicks - 1) / budgetTicks);
        } else {
            cachedCharsPerTick = 0;
        }
    }

    private static void clearCache() {
        cachedTitleLines = null;
        cachedTextLines = null;
        cachedLineLengths = null;
        cachedTotalChars = 0;
        cachedCharsPerTick = 0;
        cachedHasIcon = false;
        cachedIconBlock = 0;
        cachedBoxWidth = 0;
        cachedBoxHeight = 0;
    }

    public static void render(GuiGraphics g, float partialTick, int screenWidth, int screenHeight) {
        if (current == null) return;
        float alpha = computeAlpha(partialTick);
        if (alpha <= 0.01f) return;
        float bgAlpha = (float) Math.sqrt(alpha);

        Minecraft mc = Minecraft.getInstance();
        var font = mc.font;

        float t = ageTicks + partialTick;
        int shakeX = current.shake()
                ? Math.round(Mth.sin(t * SHAKE_FREQ_X) * SHAKE_AMPLITUDE_X) : 0;

        int x = (screenWidth - cachedBoxWidth) / 2 + shakeX;
        int y = screenHeight - HOTBAR_HEIGHT - HUD_ABOVE_HOTBAR_HEIGHT - HUD_GAP - cachedBoxHeight;
        y = Math.max(MIN_TOP_MARGIN, y);

        g.pose().pushPose();
        g.pose().translate(0.0F, 0.0F, 200.0F);

        g.fill(x, y, x + cachedBoxWidth, y + cachedBoxHeight, withAlpha(BG_COLOR, bgAlpha));

        if (cachedHasIcon) {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            g.blit(current.icon(), x + PADDING, y + PADDING, 0, 0,
                    ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        int textX = x + PADDING + cachedIconBlock;
        int textY = y + PADDING;
        int titleColor = withAlpha(current.titleColor(), alpha);
        int textColor = withAlpha(current.textColor(), alpha);

        for (FormattedCharSequence line : cachedTitleLines) {
            g.drawString(font, line, textX, textY, titleColor, false);
            textY += font.lineHeight;
        }
        if (!cachedTitleLines.isEmpty() && !cachedTextLines.isEmpty()) textY += TITLE_TEXT_GAP;

        int budget;
        if (cachedCharsPerTick > 0) {
            budget = Math.min(cachedTotalChars, (int) (cachedCharsPerTick * t));
        } else {
            budget = Integer.MAX_VALUE;
        }

        for (int i = 0; i < cachedTextLines.size(); i++) {
            if (budget <= 0) break;
            FormattedCharSequence line = cachedTextLines.get(i);
            int len = cachedLineLengths[i];
            FormattedCharSequence visible = budget >= len ? line : takePrefix(line, budget);
            g.drawString(font, visible, textX, textY, textColor, false);
            textY += font.lineHeight;
            budget -= len;
        }

        g.pose().popPose();
    }

    private static int charCount(FormattedCharSequence seq) {
        int[] count = {0};
        seq.accept((index, style, cp) -> { count[0]++; return true; });
        return count[0];
    }

    private static FormattedCharSequence takePrefix(FormattedCharSequence source, int maxChars) {
        List<Style> styles = new ArrayList<>();
        List<Integer> codepoints = new ArrayList<>();
        int[] count = {0};
        boolean[] truncated = {false};
        source.accept((index, style, cp) -> {
            if (count[0] >= maxChars) { truncated[0] = true; return false; }
            styles.add(style);
            codepoints.add(cp);
            count[0]++;
            return true;
        });
        if (!truncated[0]) return source;
        return sink -> {
            for (int i = 0; i < codepoints.size(); i++) {
                if (!sink.accept(i, styles.get(i), codepoints.get(i))) return false;
            }
            return true;
        };
    }

    private static float computeAlpha(float partialTick) {
        if (current == null) return 0f;
        float t = ageTicks + partialTick;
        if (t < FADE_IN) return Mth.clamp(t / FADE_IN, 0f, 1f);
        int fullEnd = FADE_IN + current.durationTicks();
        if (t > fullEnd) return Mth.clamp(1f - (t - fullEnd) / FADE_OUT, 0f, 1f);
        return 1f;
    }

    private static int withAlpha(int color, float alpha) {
        int a = Mth.clamp((int) (((color >>> 24) & 0xFF) * alpha), 0, 255);
        return (a << 24) | (color & 0xFFFFFF);
    }
}