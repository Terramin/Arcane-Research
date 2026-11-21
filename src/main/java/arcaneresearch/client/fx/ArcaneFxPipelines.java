package arcaneresearch.client.fx;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Render pipelines used by the ported screen-space effects.
 *
 * <p>The original 1.20.1 effects used custom GLSL shaders ({@code ShaderInstance}).
 * MC 26.3 compiles shaders to SPIR-V, so the legacy GLSL sources cannot be reused.
 * The effects are therefore drawn with the corresponding vanilla pipelines:
 * they keep their animation/colour and remain fully functional without a
 * custom RenderPipeline.
 */
public final class ArcaneFxPipelines {

    public static final RenderPipeline PORTAL_WAVE = RenderPipelines.GUI_TEXTURED;
    public static final RenderPipeline SCAN_RIPPLE = RenderPipelines.GUI_TEXTURED;
    public static final RenderPipeline PLAYER_GLITCH = RenderPipelines.LIGHTNING;

    private ArcaneFxPipelines() {
    }
}
