package arcaneresearch.client.renderers;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

import java.util.function.Function;

// NEOFORGE_PORT (26.3 render rework, verified in 26.3 jars + Thaumaturge oracle
// TTFXRenderTypes pattern): RenderStateShard/CompositeState/DefaultVertexFormat
// all gone. Types are now RenderType.create(name, RenderSetup.builder(VANILLA
// PIPELINE).withTexture("Sampler0", tex).useLightmap().createRenderSetup()),
// memoized per texture. Split verified in-game:
//   EMISSIVE family (additive glow, node add/floaty, mirror add, tendrils) ->
//     derived pipelines with explicit LIGHTNING blend + GREATER_THAN_OR_EQUAL /
//     no-write depth (26.3 reversed-Z 1:1 of Forge LEQUAL; these render).
//   TRANSLUCENT family (portal, ghost, node alpha, mirror alpha) -> RAW vanilla
//     pipelines (deriving them, however carefully, makes the geometry vanish
//     in-game while EMISSIVE-derived and raw layers render fine).
// useOverlay() is REQUIRED (not optional): the ENTITY_* shaders sample the
// overlay (Sampler1) unconditionally, and passes without an overlay texture
// (GUI item atlas) fail validation with "Missing uniform Sampler1" — exactly
// like vanilla item types, which all declare .useOverlay().
public final class TCRenderTypes {

    private static final String SAMPLER = "Sampler0";

    // NEOFORGE_PORT: every custom pipeline built via explicit()/xrayPipeline()/
    // depthWriteOffPipeline()/toBuilder() MUST be registered via
    // RegisterRenderPipelinesEvent or geometry on it renders NOTHING (no error).
    // Registry collects them at build time; PipelineEvents drains it.
    // Declared FIRST: static initializers below (ADDITIVE etc.) call track().
    private static final java.util.List<RenderPipeline> CUSTOM_PIPELINES =
            new java.util.ArrayList<>();

    private static RenderPipeline track(RenderPipeline pipeline) {
        synchronized (CUSTOM_PIPELINES) {
            CUSTOM_PIPELINES.add(pipeline);
        }
        return pipeline;
    }

    private static final Function<Identifier, RenderType> ADDITIVE = derived(
            "arcaneresearch_additive", RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE,
            BlendFunction.LIGHTNING, true, false);
    private static final Function<Identifier, RenderType> ADDITIVE_NO_CULL = derived(
            "arcaneresearch_additive_nocull", RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE,
            BlendFunction.LIGHTNING, false, false);
    // NEOFORGE_PORT (world aspect popup parity): Forge 1.20.1 drew the aspect
    // billboards with RenderSystem.disableDepthTest() (no depth test at all).
    // LEQUAL (what additiveTexturedNoCull uses) clips quads embedded in/near
    // geometry in the new OIT pipeline, so the popup gets its own no-test
    // layer. Recipe mirrors NODE_ADD_XRAY (EMISSIVE family + ALWAYS_PASS +
    // no write, proven in-game); track() auto-registers it via PipelineEvents.
    private static final Function<Identifier, RenderType> ADDITIVE_NO_CULL_NODEPTH = setup(
            "arcaneresearch_additive_nocull_nodepth", explicitNoDepth(
                    "arcaneresearch_additive_nocull_nodepth",
                    RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, BlendFunction.LIGHTNING, false));
    private static final Function<Identifier, RenderType> PORTAL = lit(
            "arcaneresearch_portal", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> OBJ_SOLID = lit(
            "arcaneresearch_obj_solid", RenderPipelines.ENTITY_CUTOUT);
    // NEOFORGE_PORT (Forge 1:1): Forge cutoutTextured — QUADS-variant entity
    // cutout with a standalone texture (reforged rune sides used it). 26.3 has
    // no RenderStateShard/CompositeState: the standard cutout layer (same
    // pipeline OBJ_SOLID derives from) already samples a bound texture and
    // renders QUADS with backface culling, so this is the 1:1 here.
    private static final Function<Identifier, RenderType> CUTOUT = lit(
            "arcaneresearch_cutout_textured", RenderPipelines.ENTITY_CUTOUT);
    // NEOFORGE_PORT (wand parity, Thaumaturge ENTITY_CUTOUT_FLAT pattern):
    // Forge baked wand quads with shade=false (flat, ungraded) and no face
    // culling; the raw ENTITY_CUTOUT grades side faces by normal (cardinal
    // lighting) and culls backfaces, so the OBJ wand looks darker/holey vs
    // Forge. Derived from ENTITY_CUTOUT restating ONLY shader defines + cull
    // (color targets/depth/bias stay raw-vanilla — a rebuilt ColorTargetState
    // mismatches the level targets and renders nothing, see header).
    // NO_OVERLAY is declared (overlay sampler unused, like Thaumaturge) but
    // the setup still binds Sampler1: if the 26.3 entity shader ever ignores
    // the define, the bound texture avoids "Missing uniform Sampler1".
    private static final RenderPipeline OBJ_SOLID_FLAT_PIPE = RenderPipelines.ENTITY_CUTOUT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcaneresearch_obj_solid_flat"))
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("NO_OVERLAY")
            .withCull(false)
            .build();
    private static final Function<Identifier, RenderType> OBJ_SOLID_FLAT = setup(
            "arcaneresearch_obj_solid_flat", OBJ_SOLID_FLAT_PIPE);

    // NEOFORGE_PORT (wand focus alpha parity): Forge 1.20.1 pushed ALL baked wand
    // quads through ItemRenderer -> RenderType.translucent (fancy graphics), so
    // the focus texture's soft alpha blended translucently. Same derivation as
    // OBJ_SOLID_FLAT (ENTITY_SNIPPET + NO_CARDINAL_LIGHTING + NO_OVERLAY, no
    // cull, GREATER_THAN_OR_EQUAL + depth write) but the color target re-states
    // the vanilla TRANSLUCENT blend (proven MIRROR_ALPHA pattern; format/write
    // mask/depth bias are copied from the raw vanilla pipeline untouched).
    // No EMISSIVE define: the focus stays lightmap-lit like Forge item quads.
    private static final RenderPipeline OBJ_ALPHA_FLAT_PIPE;
    static {
        RenderPipeline base = RenderPipelines.ENTITY_TRANSLUCENT;
        ColorTargetState baseTarget = base.getColorTargetStates().get(0);
        DepthStencilState baseDepth = base.getDepthStencilState();
        OBJ_ALPHA_FLAT_PIPE = track(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                        "arcaneresearch_obj_alpha_flat"))
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(BlendFunction.TRANSLUCENT),
                        baseTarget.format(), baseTarget.writeMask()))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL,
                        true, baseDepth.depthBiasScaleFactor(), baseDepth.depthBiasConstant()))
                .build());
    }
    private static final Function<Identifier, RenderType> OBJ_ALPHA_FLAT = setup(
            "arcaneresearch_obj_alpha_flat", OBJ_ALPHA_FLAT_PIPE);

    // NEOFORGE_PORT (26.3): GUI blit with Forge blendFunc(SRC_ALPHA, ONE) (HUD bar
    // shimmer overlays). Built like vanilla GUI_NAUSEA_OVERLAY (GUI_TEXTURED_SNIPPET +
    // own ColorTargetState) but with LIGHTNING = SrcAlpha/One; registered via track().
    public static final RenderPipeline GUI_TEXTURED_ADDITIVE = track(
            RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                            "pipeline/gui_textured_additive"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                    .build());

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class PipelineEvents {
        @SubscribeEvent
        public static void onRegisterPipelines(
                net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent event) {
            event.registerPipeline(OBJ_SOLID_FLAT_PIPE);
            // BeamEngine additive/portal/ghost/mirror/node layers + xray/no-write
            // variants + orb tendrils: unregistered custom pipeline = invisible
            // beam (e.g. excavation beam). Force orbTendrils init (lazy) first.
            try {
                orbTendrils();
                orbTendrilsNoCull();
            } catch (Exception ignored) {
            }
            synchronized (CUSTOM_PIPELINES) {
                for (RenderPipeline pipeline : CUSTOM_PIPELINES) {
                    try {
                        event.registerPipeline(pipeline);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
    // NEOFORGE_PORT: TRANSLUCENT-derived pipelines stay RAW (see header): only
    // the EMISSIVE family is re-stated. Proven in-game: EMISSIVE-derived glow
    // renders, TRANSLUCENT-derived does not (jar glass/liquid/labels vanish
    // while node glow and brain/lid on raw+EMISSIVE layers render fine).
    private static final Function<Identifier, RenderType> GOLEM_GHOST = lit(
            "arcaneresearch_golem_ghost", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> GHOST = lit(
            "arcaneresearch_ghost", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> GHOST_XRAY = litXray(
            "arcaneresearch_ghost_xray", RenderPipelines.ENTITY_TRANSLUCENT);
    // NEOFORGE_PORT (mirror parity): Forge mirrorLayer was POSITION_TEX_COLOR
    // (unlit fullbright, unshaded, CULL, COLOR_DEPTH_WRITE). Deriving from the
    // vanilla entity pipelines keeps PER_FACE_LIGHTING + lightmap multiply
    // (verified in 26.3 entity.vsh: PER_FACE branch wins over
    // NO_CARDINAL_LIGHTING, EMISSIVE absent in the direct path) — the portal
    // rendered shaded (~0.8 for the viewer-facing normal) and dark in dark
    // rooms. These are built from ENTITY_SNIPPET with EMISSIVE (skip lightmap)
    // + NO_CARDINAL_LIGHTING (flat vertex color) + NO_OVERLAY, inheriting only
    // format/write-mask/depth-bias from the named vanilla pipeline — exact
    // Forge look. Used solely by MirrorRenderer.renderActivePortal.
    private static final Function<Identifier, RenderType> MIRROR_ADD = flatEmissive(
            "arcaneresearch_mirror_add", RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE,
            BlendFunction.LIGHTNING, true, true);
    private static final Function<Identifier, RenderType> MIRROR_ALPHA = flatEmissive(
            "arcaneresearch_mirror_alpha", RenderPipelines.ENTITY_TRANSLUCENT,
            BlendFunction.TRANSLUCENT, true, true);

    private static volatile RenderType orbTendrils;
    private static volatile RenderType orbTendrilsNoCull;

    private TCRenderTypes() {
    }

    private static Function<Identifier, RenderType> lit(String name,
            com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline) {
        return setup(name, pipeline);
    }

    private static Function<Identifier, RenderType> setup(String name, RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderType.create(name, RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, texture).useLightmap().useOverlay().createRenderSetup()));
    }

    // NEOFORGE_PORT: ENTITY_SNIPPET has no SAMPLER1 (overlay) layout, and the
    // flat-emissive pipelines below declare NO_OVERLAY, so the setup binds
    // only Sampler0 + lightmap (lightmap is bound but skipped by EMISSIVE).
    private static Function<Identifier, RenderType> setupNoOverlay(String name, RenderPipeline pipeline) {
        return Util.memoize(texture -> RenderType.create(name, RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, texture).useLightmap().createRenderSetup()));
    }

    private static Function<Identifier, RenderType> flatEmissive(String name, RenderPipeline inherit,
            BlendFunction blend, boolean cull, boolean writeDepth) {
        ColorTargetState oldTarget = inherit.getColorTargetStates().get(0);
        DepthStencilState oldDepth = inherit.getDepthStencilState();
        return setupNoOverlay(name, track(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, name))
                .withShaderDefine("EMISSIVE")
                .withShaderDefine("NO_CARDINAL_LIGHTING")
                .withShaderDefine("NO_OVERLAY")
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(blend), oldTarget.format(), oldTarget.writeMask()))
                .withCull(cull)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, writeDepth,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build()));
    }

    private static RenderPipeline explicit(String name, RenderPipeline base,
            BlendFunction blend, boolean cull, boolean writeDepth) {
        // Only these four properties are re-stated; format, write mask and
        // depth bias are inherited from the base pipeline untouched — a fully
        // rebuilt ColorTargetState (default RGBA8 format) does not match the
        // level targets and renders nothing.
        ColorTargetState oldTarget = base.getColorTargetStates().get(0);
        DepthStencilState oldDepth = base.getDepthStencilState();
        return track(base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, name))
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(blend), oldTarget.format(), oldTarget.writeMask()))
                .withCull(cull)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, writeDepth,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build());
    }

    // NEOFORGE_PORT: Forge CompositeState 1:1 — vanilla pipeline defaults are
    // NOT trusted (blend/cull/depth-write are unspecified for our purposes),
    // so every state Forge set explicitly is re-stated here:
    //   LIGHTNING_TRANSPARENCY -> BlendFunction.LIGHTNING (SrcAlpha/One),
    //   TRANSLUCENT_TRANSPARENCY -> BlendFunction.TRANSLUCENT,
    //   CULL/NO_CULL -> withCull, COLOR_WRITE -> no depth write + GREATER_THAN_OR_EQUAL test,
    //   COLOR_DEPTH_WRITE -> depth write + GREATER_THAN_OR_EQUAL test.
    // (26.3 reversed depth: GREATER_THAN_OR_EQUAL is the 1:1 of old-GL LEQUAL.)
    private static Function<Identifier, RenderType> derived(String name, RenderPipeline base,
            BlendFunction blend, boolean cull, boolean writeDepth) {
        return setup(name, explicit(name, base, blend, cull, writeDepth));
    }

    // NEOFORGE_PORT: explicit() with the depth test off (Forge disableDepthTest
    // 1:1). Same restating discipline: only blend/cull/depth-test change,
    // format/write-mask/bias stay vanilla (see explicit()).
    private static RenderPipeline explicitNoDepth(String name, RenderPipeline base,
            BlendFunction blend, boolean cull) {
        ColorTargetState oldTarget = base.getColorTargetStates().get(0);
        DepthStencilState oldDepth = base.getDepthStencilState();
        return track(base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, name))
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(blend), oldTarget.format(), oldTarget.writeMask()))
                .withCull(cull)
                .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build());
    }

    // NEOFORGE_PORT: world node layers (AuraNodeRenderHelper.createNodeLayer /
    // createFloatyLayer 1:1): LIGHTNING or TRANSLUCENT blend, NO_CULL,
    // COLOR_WRITE (no depth write), GREATER_THAN_OR_EQUAL test (reversed-Z
    // 1:1 of Forge LEQUAL), NO_DEPTH_TEST for xray.
    private static final RenderPipeline NODE_ADD_PIPE = explicit("aura_node_add",
            RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, BlendFunction.LIGHTNING, false, false);
    private static final RenderPipeline NODE_ADD_XRAY_PIPE =
            xrayPipeline("aura_node_add_xray", NODE_ADD_PIPE);
    private static final RenderPipeline NODE_ALPHA_PIPE = RenderPipelines.ENTITY_TRANSLUCENT;
    private static final RenderPipeline NODE_ALPHA_XRAY_PIPE =
            xrayPipeline("aura_node_alpha_xray", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final RenderPipeline FLOATY_PIPE = explicit("aura_floaty",
            RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, BlendFunction.LIGHTNING, false, false);
    private static final Function<Identifier, RenderType> NODE_ADD_T = setup("aura_node_add", NODE_ADD_PIPE);
    private static final Function<Identifier, RenderType> NODE_ADD_XRAY_T =
            setup("aura_node_add_xray", NODE_ADD_XRAY_PIPE);
    private static final Function<Identifier, RenderType> NODE_ALPHA_T =
            setup("aura_node_alpha", NODE_ALPHA_PIPE);
    private static final Function<Identifier, RenderType> NODE_ALPHA_XRAY_T =
            setup("aura_node_alpha_xray", NODE_ALPHA_XRAY_PIPE);
    private static final Function<Identifier, RenderType> FLOATY_T = setup("aura_floaty", FLOATY_PIPE);

    public static RenderType nodeAdd(Identifier texture) {
        return NODE_ADD_T.apply(texture);
    }

    public static RenderType nodeAddXray(Identifier texture) {
        return NODE_ADD_XRAY_T.apply(texture);
    }

    public static RenderType nodeAlpha(Identifier texture) {
        return NODE_ALPHA_T.apply(texture);
    }

    public static RenderType nodeAlphaXray(Identifier texture) {
        return NODE_ALPHA_XRAY_T.apply(texture);
    }

    public static RenderType nodeFloaty(Identifier texture) {
        return FLOATY_T.apply(texture);
    }

    // NEOFORGE_PORT: Forge NO_DEPTH_TEST layers (ghostXray, node xray). Derives
    // from the given vanilla pipeline, swapping only the depth state to
    // ALWAYS_PASS/no-write so the geometry draws through walls.
    public static com.mojang.renderpearl.api.pipeline.RenderPipeline xrayPipeline(
            String debugId, com.mojang.renderpearl.api.pipeline.RenderPipeline base) {
        DepthStencilState oldDepth = base.getDepthStencilState();
        return track(base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath("arcaneresearch", debugId))
                .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(
                        com.mojang.renderpearl.api.pipeline.CompareOp.ALWAYS_PASS, false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build());
    }

    // NEOFORGE_PORT: Forge defaultBlendFunc + depthMask(false) 1:1 (Sonic cone,
    // VoidStream ribbon). Derives from ENTITY_TRANSLUCENT swapping ONLY depth
    // write off and (per-effect) cull: blend, color targets, depth test and
    // bias stay raw-vanilla, so the ENTITY_TRANSLUCENT OIT chain is untouched
    // (re-stating the color target there makes geometry vanish in-game).
    public static com.mojang.renderpearl.api.pipeline.RenderPipeline depthWriteOffPipeline(
            String debugId, com.mojang.renderpearl.api.pipeline.RenderPipeline base, boolean cull) {
        DepthStencilState oldDepth = base.getDepthStencilState();
        return track(base.toBuilder()
                .withLocation(Identifier.fromNamespaceAndPath("arcaneresearch", debugId))
                .withCull(cull)
                .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(
                        oldDepth.depthTest(), false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build());
    }

    private static final Function<Identifier, RenderType> TRANSLUCENT_NOWRITE_T = setup(
            "arcaneresearch_translucent_no_depth_write", depthWriteOffPipeline(
                    "arcaneresearch_translucent_no_depth_write",
                    RenderPipelines.ENTITY_TRANSLUCENT, false));
    private static final Function<Identifier, RenderType> TRANSLUCENT_NOWRITE_CULL_T = setup(
            "arcaneresearch_translucent_no_depth_write_cull", depthWriteOffPipeline(
                    "arcaneresearch_translucent_no_depth_write_cull",
                    RenderPipelines.ENTITY_TRANSLUCENT, true));

    public static RenderType translucentNoDepthWrite(Identifier texture) {
        return TRANSLUCENT_NOWRITE_T.apply(texture);
    }

    public static RenderType translucentNoDepthWriteCull(Identifier texture) {
        return TRANSLUCENT_NOWRITE_CULL_T.apply(texture);
    }

    private static Function<Identifier, RenderType> litXray(String name,
            com.mojang.renderpearl.api.pipeline.RenderPipeline base) {
        com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline = xrayPipeline(name, base);
        return Util.memoize(texture -> RenderType.create(name, RenderSetup.builder(pipeline)
                .withTexture(SAMPLER, texture).useLightmap().useOverlay().createRenderSetup()));
    }

    public static RenderType additiveTextured(Identifier texture) {
        return ADDITIVE.apply(texture);
    }

    public static RenderType additiveTexturedNoCull(Identifier texture) {
        return ADDITIVE_NO_CULL.apply(texture);
    }

    public static RenderType additiveTexturedNoCullNoDepth(Identifier texture) {
        return ADDITIVE_NO_CULL_NODEPTH.apply(texture);
    }

    public static RenderType orbTendrils() {
        RenderType type = orbTendrils;
        if (type == null) {
            synchronized (TCRenderTypes.class) {
                type = orbTendrils;
                if (type == null) {
                    // NEOFORGE_PORT: Forge POSITION_COLOR TRIANGLE_FAN 1:1
                    // (LIGHTNING blend, CULL, COLOR_WRITE, GREATER_THAN_OR_EQUAL
                    // = reversed-Z 1:1 of Forge LEQUAL) — the raw
                    // DEBUG pipeline's blend is unspecified, so derive it.
                    type = RenderType.create("arcaneresearch_orb_tendrils",
                            RenderSetup.builder(explicit("arcaneresearch_orb_tendrils",
                                    RenderPipelines.DEBUG_TRIANGLE_FAN,
                                    BlendFunction.LIGHTNING, true, false))
                                    .createRenderSetup());
                    orbTendrils = type;
                }
            }
        }
        return type;
    }

    // NEOFORGE_PORT (26.3): orbTendrils with culling off - Forge drew the infusion matrix
    // halo and focal manipulator rays under RenderSystem.disableCull() (both faces).
    public static RenderType orbTendrilsNoCull() {
        RenderType type = orbTendrilsNoCull;
        if (type == null) {
            synchronized (TCRenderTypes.class) {
                type = orbTendrilsNoCull;
                if (type == null) {
                    type = RenderType.create("arcaneresearch_orb_tendrils_nocull",
                            RenderSetup.builder(explicit("arcaneresearch_orb_tendrils_nocull",
                                    RenderPipelines.DEBUG_TRIANGLE_FAN,
                                    BlendFunction.LIGHTNING, false, false))
                                    .createRenderSetup());
                    orbTendrilsNoCull = type;
                }
            }
        }
        return type;
    }

    public static RenderType translucentPortal(Identifier texture) {
        return PORTAL.apply(texture);
    }

    public static RenderType objSolid(Identifier texture) {
        return OBJ_SOLID.apply(texture);
    }

    public static RenderType cutoutTextured(Identifier texture) {
        return CUTOUT.apply(texture);
    }

    public static RenderType objSolidFlat(Identifier texture) {
        return OBJ_SOLID_FLAT.apply(texture);
    }

    public static RenderType objAlphaFlat(Identifier texture) {
        return OBJ_ALPHA_FLAT.apply(texture);
    }

    public static RenderType golemGhost(Identifier texture) {
        return GOLEM_GHOST.apply(texture);
    }

    public static RenderType ghostXray(Identifier texture) {
        return GHOST_XRAY.apply(texture);
    }

    public static RenderType mirrorLayer(Identifier texture, boolean additive) {
        return (additive ? MIRROR_ADD : MIRROR_ALPHA).apply(texture);
    }
}
