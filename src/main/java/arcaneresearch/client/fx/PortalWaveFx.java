package arcaneresearch.client.fx;

/**
 * Full-screen portal-wave effect. The original implementation used the
 * legacy shader pipeline ({@code ShaderInstance}/{@code GlStateManager})
 * which was removed in NeoForge 26.x.  Kept as a no-op until it can be
 * reimplemented with {@code RenderPipeline}s / post-processing.
 */
public final class PortalWaveFx {

    private static long startNanos;

    private PortalWaveFx() {
    }

    public static boolean isAvailable() {
        return false;
    }

    public static void trigger() {
        startNanos = System.nanoTime();
    }

    public static boolean isActive() {
        return startNanos != 0L && System.nanoTime() - startNanos < 1_500_000_000L;
    }

    public static long elapsedNanos() {
        return startNanos == 0L ? 0L : System.nanoTime() - startNanos;
    }
}
