package arcaneresearch.invscan.client;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.discovered.scan.ScanTargets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ScanFlightSystem {

    private static final float FLIGHT_DURATION_SEC = 1.10F;
    private static final float STAGGER_SEC        = 0.08F;
    private static final float ARC_HEIGHT         = 22.0F;
    private static final float FADE_START         = 0.65F;

    private static final float END_BASE_X = 26.0F;
    private static final float END_BASE_Y = 26.0F;
    private static final float STEP       = 18.0F;
    private static final int   COLS       = 3;

    private static final int MAX_ACTIVE = 128;

    private static final List<Flight> ACTIVE = new ArrayList<>();

    private ScanFlightSystem() {}

    public static void launchFromSlot(AbstractContainerScreen<?> screen,
                                      Slot slot,
                                      AspectList aspects,
                                      Player player) {
        if (screen == null || slot == null) return;
        launch(screen,
                screen.getLeftPos() + slot.x + 8.0F,
                screen.getTopPos()  + slot.y + 8.0F,
                aspects, player);
    }

    public static void launchFromPoint(AbstractContainerScreen<?> screen,
                                       float originX, float originY,
                                       AspectList aspects,
                                       Player player) {
        launch(screen, originX, originY, aspects, player);
    }

    private static void launch(AbstractContainerScreen<?> screen,
                               float originX, float originY,
                               AspectList aspects, Player player) {
        if (screen == null || aspects == null || aspects.size() <= 0 || player == null) return;

        long now = System.nanoTime();
        Minecraft minecraft = Minecraft.getInstance();
        float endBaseX = minecraft.getWindow().getGuiScaledWidth() - END_BASE_X;
        float endBaseY = END_BASE_Y;

        int idx = 0;
        for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
            if (aspect == null) continue;

            int amount = aspects.getAmount(aspect);
            boolean known = ScanTargets.known(player, aspect);

            long startsAt = now + (long) (idx * STAGGER_SEC * 1_000_000_000L);
            float endX = endBaseX - (idx % COLS) * STEP;
            float endY = endBaseY + (idx / COLS) * STEP;

            ACTIVE.add(new Flight(aspect, amount, known,
                    originX, originY, endX, endY, startsAt));
            idx++;
        }

        while (ACTIVE.size() > MAX_ACTIVE) ACTIVE.remove(0);
    }

    public static void tick() {
        if (ACTIVE.isEmpty()) return;
        long now = System.nanoTime();
        Iterator<Flight> it = ACTIVE.iterator();
        while (it.hasNext()) if (it.next().isDone(now)) it.remove();
    }

    public static void render(GuiGraphicsExtractor g, float partialTick) {
        if (ACTIVE.isEmpty()) return;
        long now = System.nanoTime();
        for (int i = 0; i < ACTIVE.size(); i++) ACTIVE.get(i).render(g, now);
    }

    public static void clear() { ACTIVE.clear(); }

    private static final class Flight {
        final Aspect aspect;
        final int amount;
        final boolean known;
        final float startX, startY, endX, endY;
        final long startsAt;
        final long endsAt;

        Flight(Aspect aspect, int amount, boolean known,
               float startX, float startY, float endX, float endY,
               long startsAt) {
            this.aspect = aspect;
            this.amount = amount;
            this.known  = known;
            this.startX = startX;
            this.startY = startY;
            this.endX   = endX;
            this.endY   = endY;
            this.startsAt = startsAt;
            this.endsAt   = startsAt + (long) (FLIGHT_DURATION_SEC * 1_000_000_000L);
        }

        boolean isDone(long now) { return now >= endsAt; }

        void render(GuiGraphicsExtractor g, long now) {
            if (now < startsAt) return;

            float t = (now - startsAt) / (float) (endsAt - startsAt);
            if (t < 0.0F) t = 0.0F;
            if (t > 1.0F) t = 1.0F;

            float eased = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
            float x = startX + (endX - startX) * eased;
            float y = startY + (endY - startY) * eased;
            y -= ARC_HEIGHT * (float) Math.sin(t * Math.PI);

            float alpha = t < FADE_START
                    ? 1.0F
                    : 1.0F - (t - FADE_START) / (1.0F - FADE_START);
            if (alpha < 0.0F) alpha = 0.0F;
            if (alpha > 1.0F) alpha = 1.0F;

            float drawAlpha = known ? alpha * 0.55F : alpha;

            AspectRenderer.drawAspect(g,
                    Math.round(x), Math.round(y),
                    aspect, amount, 0,
                    drawAlpha, known);
        }
    }
}
