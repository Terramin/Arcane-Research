package arcaneresearch.client.fx;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

/**
 * Holders for the wave-effect render pipelines. NeoForge 26.x removed
 * {@code ShaderInstance}/{@code RenderStateShard}; the pipelines are built and
 * registered by {@link ArcaneFxPipelines} instead.
 */
public final class FxWaveShaders {

    public static final RenderPipeline PORTAL_WAVE = ArcaneFxPipelines.PORTAL_WAVE;
    public static final RenderPipeline SCAN_RIPPLE = ArcaneFxPipelines.SCAN_RIPPLE;
    public static final RenderPipeline PLAYER_GLITCH = ArcaneFxPipelines.PLAYER_GLITCH;

    private FxWaveShaders() {
    }
}
