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







public final class TCSprites {

      
    public static final Identifier SHEET =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    private TCSprites() {
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class AtlasEvents {
        @SubscribeEvent
        public static void onAddReloadListeners(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent event) {
            
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
                
                
                
                
                
                
                .withDepthStencilState(new com.mojang.renderpearl.api.pipeline.DepthStencilState(
                        com.mojang.renderpearl.api.pipeline.CompareOp.GREATER_THAN_OR_EQUAL, false,
                        oldDepth.depthBiasScaleFactor(), oldDepth.depthBiasConstant()))
                .build();
    }

    
    
    
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

      
    public static SingleQuadParticle.Layer layerFor(int layer) {
        return switch (layer) {
            case 0, 4 -> additive();
            case 2 -> additiveNoDepth();
            case 3 -> translucentNoDepth();
            default -> translucent();
        };
    }

      
    public static SingleQuadParticle.Layer translucent() {
        return layer(true, SHEET, RenderPipelines.TRANSLUCENT_PARTICLE);
    }

      
    public static SingleQuadParticle.Layer additive() {
        return layer(true, SHEET, ADDITIVE_PARTICLE);
    }

      
    public static SingleQuadParticle.Layer translucentNoDepth() {
        return layer(true, SHEET, TRANSLUCENT_NO_DEPTH_PARTICLE);
    }

    public static SingleQuadParticle.Layer additiveNoDepth() {
        return layer(true, SHEET, ADDITIVE_NO_DEPTH_PARTICLE);
    }

    
    
    
    private static SingleQuadParticle.Layer layer(boolean translucent, Identifier texture,
                                                  com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline) {
        return new SingleQuadParticle.Layer(translucent, texture, pipeline,
                net.minecraft.client.renderer.RenderPipelines.OIT_PARTICLE);
    }
}