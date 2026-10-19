package arcaneresearch.client.lib.events.handler;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParticleHandler {

    private static final List<Delayed> QUEUE = new ArrayList<>();
    private static int tick;

    public static void render(float partialTick) {

    }

    public static void schedule(Runnable task, int delay) {
        if (delay <= 0) {
            task.run();
            return;
        }
        synchronized (QUEUE) {
            QUEUE.add(new Delayed(task, tick + delay));
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            synchronized (QUEUE) {
                QUEUE.clear();
            }
            return;
        }
        tick++;
        synchronized (QUEUE) {
            Iterator<Delayed> it = QUEUE.iterator();
            while (it.hasNext()) {
                Delayed delayed = it.next();
                if (delayed.dueTick() <= tick) {
                    it.remove();
                    try {
                        delayed.task().run();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private record Delayed(Runnable task, int dueTick) {
    }
}