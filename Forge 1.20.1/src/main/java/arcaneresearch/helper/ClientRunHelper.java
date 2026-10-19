package arcaneresearch.helper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
public final class ClientRunHelper {
    private ClientRunHelper() {
    }
    public static void runOnClient(Runnable action) {
        if (action == null) {
            return;
        }
        try {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> action);
        } catch (Exception ignored) {
        }
    }
}
