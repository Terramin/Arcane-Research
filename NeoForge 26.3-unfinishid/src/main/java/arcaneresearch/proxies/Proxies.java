package arcaneresearch.proxies;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge (Dist-based proxy selection; only imports changed).
public final class Proxies {
    private static IProxy proxy;

    private Proxies() {
    }

    public static synchronized IProxy get() {
        if (proxy == null) {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
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

    // Kept for compat (client setup may override explicitly).
    public static void set(IProxy p) {
        proxy = p;
    }
}
