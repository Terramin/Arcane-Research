package arcaneresearch.client.renderers;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

import java.util.function.Function;
















public final class TCRenderTypes {

    private static final String SAMPLER = "Sampler0";

    
    
    
    
    
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
    
    
    
    
    
    
    private static final Function<Identifier, RenderType> ADDITIVE_NO_CULL_NODEPTH = setup(
            "arcaneresearch_additive_nocull_nodepth", explicitNoDepth(
                    "arcaneresearch_additive_nocull_nodepth",
                    RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE, BlendFunction.LIGHTNING, false));
    private static final Function<Identifier, RenderType> PORTAL = lit(
            "arcaneresearch_portal", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> OBJ_SOLID = lit(
            "arcaneresearch_obj_solid", RenderPipelines.ENTITY_CUTOUT);
    
    
    
    
    
    private static final Function<Identifier, RenderType> CUTOUT = lit(
            "arcaneresearch_cutout_textured", RenderPipelines.ENTITY_CUTOUT);
    
    
    
    
    
    
    
    
    
    
    private static final RenderPipeline OBJ_SOLID_FLAT_PIPE = RenderPipelines.ENTITY_CUTOUT.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcaneresearch_obj_solid_flat"))
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("NO_OVERLAY")
            .withCull(false)
            .build();
    private static final Function<Identifier, RenderType> OBJ_SOLID_FLAT = setup(
            "arcaneresearch_obj_solid_flat", OBJ_SOLID_FLAT_PIPE);

    
    
    
    
    
    
    
    
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

    
    
    
    
    
    
    
    
    
    
    
    
    private static final RenderPipeline GHOST_ITEM_PIPE;
    static {
        RenderPipeline base = RenderPipelines.ENTITY_TRANSLUCENT;
        ColorTargetState baseTarget = base.getColorTargetStates().get(0);
        DepthStencilState baseDepth = base.getDepthStencilState();
        GHOST_ITEM_PIPE = track(RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                        "arcaneresearch_ghost_item"))
                .withShaderDefine("NO_OVERLAY")
                .withColorTargetState(0, new ColorTargetState(
                        java.util.Optional.of(BlendFunction.LIGHTNING),
                        baseTarget.format(), baseTarget.writeMask()))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL,
                        true, baseDepth.depthBiasScaleFactor(), baseDepth.depthBiasConstant()))
                .build());
    }
    private static final Function<Identifier, RenderType> GHOST_ITEM_T = setup(
            "arcaneresearch_ghost_item", GHOST_ITEM_PIPE);

    public static RenderType ghostItem(Identifier texture) {
        return GHOST_ITEM_T.apply(texture);
    }

    
    
    
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
    
    
    
    
    private static final Function<Identifier, RenderType> GOLEM_GHOST = lit(
            "arcaneresearch_golem_ghost", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> GHOST = lit(
            "arcaneresearch_ghost", RenderPipelines.ENTITY_TRANSLUCENT);
    private static final Function<Identifier, RenderType> GHOST_XRAY = litXray(
            "arcaneresearch_ghost_xray", RenderPipelines.ENTITY_TRANSLUCENT);
    
    
    
    
    
    
    
    
    
    
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

    
    
    
    
    
    
    
    
    private static Function<Identifier, RenderType> derived(String name, RenderPipeline base,
            BlendFunction blend, boolean cull, boolean writeDepth) {
        return setup(name, explicit(name, base, blend, cull, writeDepth));
    }

    
    
    
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
