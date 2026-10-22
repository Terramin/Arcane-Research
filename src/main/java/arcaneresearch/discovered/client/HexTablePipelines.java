package arcaneresearch.discovered.client;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

import arcaneresearch.Arcaneresearch;

 








 
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class HexTablePipelines {

      
    public static final RenderPipeline GUI_TEXTURED_LIGHTNING = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                    "pipeline/gui_textured_lightning"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build();

      
    public static final RenderPipeline GUI_LIGHTNING = RenderPipeline
            .builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                    "pipeline/gui_lightning"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build();

    private HexTablePipelines() {
    }

    @SubscribeEvent
    public static void register(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GUI_TEXTURED_LIGHTNING);
        event.registerPipeline(GUI_LIGHTNING);
    }
}
