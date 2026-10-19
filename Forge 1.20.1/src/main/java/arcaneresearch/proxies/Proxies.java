package arcaneresearch.proxies;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import arcaneresearch.Arcaneresearch;
public final class Proxies {
    private static IProxy proxy;
    private Proxies() {
    }
    public static synchronized IProxy get() {
        if (proxy == null) {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                try {
                    proxy = (IProxy) Class.forName("arcaneresearch.proxies.ClientProxy")
                            .getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    Arcaneresearch.LOGGER.error("Failed to create ClientProxy, falling back to ServerProxy", e);
                    proxy = new ServerProxy();
                }
            } else {
                proxy = new ServerProxy();
            }
        }
        return proxy;
    }
}
