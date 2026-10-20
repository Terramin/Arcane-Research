package arcaneresearch.proxies;

import net.neoforged.api.distmarker.Dist;

public class ProxyBlock {

    public static final String[] CRYSTALS = {
            "crystal_air", "crystal_fire", "crystal_water", "crystal_earth",
            "crystal_order", "crystal_entropy", "crystal_flux"
    };

    public static void registerDomain() {
    }

    public static void setupBlocksClient() {
    }

    public static String[] crystalModelPaths() {
        return CRYSTALS.clone();
    }
}