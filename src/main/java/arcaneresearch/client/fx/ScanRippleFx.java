package arcaneresearch.client.fx;

/**
 * Screen-space scan ripple. The original implementation used the legacy
 * shader pipeline ({@code ShaderInstance}/{@code RenderStateShard}) which was
 * removed in NeoForge 26.x.  Kept as a no-op until it is reimplemented with a
 * {@code RenderPipeline}.
 */
public final class ScanRippleFx {

    private static long startNanos;

    private ScanRippleFx() {
    }

    public static void trigger() {
        startNanos = System.nanoTime();
    }

    public static boolean isActive() {
        return startNanos != 0L && System.nanoTime() - startNanos < 1_000_000_000L;
    }

    public static long elapsedNanos() {
        return startNanos == 0L ? 0L : System.nanoTime() - startNanos;
    }
}
