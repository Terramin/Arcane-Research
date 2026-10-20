package arcaneresearch.client.fx.other;

import net.neoforged.api.distmarker.Dist;

@Deprecated(forRemoval = true)
public class MistEffect {

    public static boolean active = false;
    public static int duration = 0;

    public static void start(int dur) {
        active = true;
        duration = dur;
    }

    public static void reset() {
        active = false;
        duration = 0;
    }

    public static void tick() {
        if (active && duration > 0) {
            duration--;
            if (duration <= 0) {
                active = false;
            }
        }
    }
}