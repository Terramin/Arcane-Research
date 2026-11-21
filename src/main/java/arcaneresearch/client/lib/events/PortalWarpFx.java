package arcaneresearch.client.lib.events;

import net.neoforged.api.distmarker.Dist;
/**
 * Teleport warp post-effect. NeoForge 26.x removed the legacy
 * {@code PostChain}/{@code ShaderInstance} API this effect used, so it is
 * currently a no-op pending a post-processing pipeline port.
 */
public final class PortalWarpFx {

    private PortalWarpFx() {
    }

    public static void start() {
        // Requires a 26.x post-processing pipeline implementation.
    }

    public static boolean isActive() {
        return false;
    }
}
