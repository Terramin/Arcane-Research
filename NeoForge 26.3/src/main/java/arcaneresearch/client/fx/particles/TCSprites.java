package arcaneresearch.client.fx.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import net.minecraft.client.particle.ParticleRenderType;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

// NEOFORGE_PORT: new helper (no Forge counterpart file; Forge bound the raw sheet via
// custom ParticleRenderType#begin, removed in 26.x). 26.x particle Layers bind RAW
// textures directly (no atlas/JSON/sprites needed), so grid fractions address the sheet
// 1:1 exactly like Forge did. Additive glow (old TC_ADDITIVE) is a real pipeline now
// (ADDITIVE_PARTICLE above, registered via RegisterRenderPipelinesEvent); no-depth
// variants still fall back to TRANSLUCENT_PARTICLE (visual-verify pass).
public final class TCSprites {

    /** Raw sheet texture (grid UV fractions address it exactly like Forge did). */
    public static final Identifier SHEET =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    private TCSprites() {
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class AtlasEvents {
        @SubscribeEvent
        public static void onAddReloadListeners(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
            // Ensure the sheet is GPU-resident before particles sample it.
            event.addListener(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "tc_sheet"),
                    (ResourceManagerReloadListener) manager -> {
                        TextureManager textures = Minecraft.getInstance().getTextureManager();
                        textures.getTexture(SHEET);
                    });
        }

        @SubscribeEvent
        public static void onRegisterPipelines(
                net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
            event.registerPipeline(ADDITIVE_PARTICLE);
            event.registerPipeline(ADDITIVE_NO_DEPTH_PARTICLE);
            event.registerPipeline(TRANSLUCENT_NO_DEPTH_PARTICLE);
        }
    }

    // NEOFORGE_PORT: Forge TC_ADDITIVE was blendFunc(770, 1) = (SRC_ALPHA, ONE).
    // Verified in the 26.3 client jar (BlendFunction bytecode): LIGHTNING =
    // (SRC_ALPHA, ONE) is the exact match; ADDITIVE = (ONE, ONE) is harsher.
    // Derived from TRANSLUCENT_PARTICLE preserving its color-target format,
    // write mask and depth state (a fully rebuilt ColorTargetState with the
    // default RGBA8 format does not match the level targets and renders
    // nothing — same failure as the entity glow layers had).
    // Vanilla 26.3 ships no additive particle pipeline (only OPAQUE/TRANSLUCENT).
    // Without this, glow sprites (nitor flames/core, aura, layer 0/4) alpha-blend
    // their dark falloff texels and show a blackish fringe (verified on the sheet:
    // halo texels are dark gray at partial alpha, e.g. A=73/RGB=111).
    public static final RenderPipeline ADDITIVE_PARTICLE = additivePipeline();

    private static RenderPipeline additivePipeline() {
        RenderPipeline base = RenderPipelines.TRANSLUCENT_PARTICLE;
        ColorTargetState oldTarget = base.getColorTargetStates().get(0);
        com.mojang.renderpearl.api.pipeline.DepthStencilState oldDepth = base.getDepthStencilState();
        return base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "pipeline/additive_particle"))
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(BlendFunction.LIGHTNING),
                        oldTarget.format(), oldTarget.writeMask()))
                // Forge TC_ADDITIVE depthMask(false), depth test on. 26.3 uses
                // reversed depth (vanilla depth-tested translucent pipelines all
                // use GREATER_THAN_OR_EQUAL, e.g. TRANSLUCENT_PARTICLE base,
                // WEATHER, BEACON_BEAM_TRANSLUCENT): that is the 1:1 of the old
                // GL LEQUAL test. LESS_THAN_OR_EQUAL inverts occlusion here
                // (glow hidden up close, visible through walls — nitor bug).
                .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(
                        com.mojang.renderpearl.api.pipeline.CompareOp.GREATER_THAN_OR_EQUAL, false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build();
    }

    // NEOFORGE_PORT: Forge TC_*_NO_DEPTH 1:1 (same blend as the depth-tested
    // twin, depthMask(false), depth test DISABLED). Only the depth state is
    // re-stated; blend/format/mask/cull come from the base untouched.
    public static final RenderPipeline ADDITIVE_NO_DEPTH_PARTICLE =
            depthIgnorePipeline("pipeline/additive_particle_no_depth", ADDITIVE_PARTICLE);
    public static final RenderPipeline TRANSLUCENT_NO_DEPTH_PARTICLE =
            depthIgnorePipeline("pipeline/translucent_particle_no_depth", RenderPipelines.TRANSLUCENT_PARTICLE);

    private static RenderPipeline depthIgnorePipeline(String path, RenderPipeline base) {
        com.mojang.renderpearl.api.pipeline.DepthStencilState oldDepth = base.getDepthStencilState();
        return base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, path))
                .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(
                        com.mojang.renderpearl.api.pipeline.CompareOp.ALWAYS_PASS, false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build();
    }

    /** Layer dispatch for the old TC sheet types (additive/normal/no-depth variants). */
    public static SingleQuadParticle.Layer layerFor(int layer) {
        return switch (layer) {
            case 0, 4 -> additive();
            case 2 -> additiveNoDepth();
            case 3 -> translucentNoDepth();
            default -> translucent();
        };
    }

    /** Normal alpha-blended sheet layer (old TC_NORMAL). */
    public static SingleQuadParticle.Layer translucent() {
        return layer(true, SHEET, RenderPipelines.TRANSLUCENT_PARTICLE);
    }

    /** Additive glow sheet layer (old TC_ADDITIVE). */
    public static SingleQuadParticle.Layer additive() {
        return layer(true, SHEET, ADDITIVE_PARTICLE);
    }

    /** No-depth-test variants (Forge TC_*_NO_DEPTH 1:1, visible through walls). */
    public static SingleQuadParticle.Layer translucentNoDepth() {
        return layer(true, SHEET, TRANSLUCENT_NO_DEPTH_PARTICLE);
    }

    public static SingleQuadParticle.Layer additiveNoDepth() {
        return layer(true, SHEET, ADDITIVE_NO_DEPTH_PARTICLE);
    }

    // NEOFORGE_PORT: vanilla translucent layers all carry RenderPipelines.OIT_PARTICLE;
    // the 3-arg Layer ctor leaves oitPipelineSet null, which drops the group from
    // the transparency pass (custom particles invisible in-game). Pass it explicitly.
    private static SingleQuadParticle.Layer layer(boolean translucent, Identifier texture,
                                                  com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline) {
        return new SingleQuadParticle.Layer(translucent, texture, pipeline,
                net.minecraft.client.renderer.RenderPipelines.OIT_PARTICLE);
    }
}