package arcaneresearch.helper;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

 







 
public final class ClientRunHelper {

    private ClientRunHelper() {
    }

      
    public static void runOnClient(Runnable action) {
        if (action == null) {
            return;
        }
        try {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                action.run();
            }
        } catch (Exception ignored) {
        }
    }
}
