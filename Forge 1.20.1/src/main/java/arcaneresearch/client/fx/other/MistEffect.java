package arcaneresearch.client.fx.other;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@Deprecated(forRemoval = true)
@OnlyIn(Dist.CLIENT)
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