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

/**
 * Аддитивные GUI-пайплайны hex-стола (паритет Forge
 * {@code RenderSystem.blendFunc(770, 1)} = SRC_ALPHA/ONE): орбы, линии связей,
 * подсветка гекса под курсором.
 *
 * NEOFORGE_PORT (26.3): RenderSystem-бленд удалён — бленд задаёт пайплайн.
 * Вариации ванильных GUI_TEXTURED / GUI с {@link BlendFunction#LIGHTNING}
 * (= GL 770, 1); кастомный пайплайн обязан быть зарегистрирован
 * (RegisterRenderPipelinesEvent), иначе ничего не рисуется.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class HexTablePipelines {

    /** Текстурный blit с бленд-функцией SRC_ALPHA/ONE. */
    public static final RenderPipeline GUI_TEXTURED_LIGHTNING = RenderPipeline
            .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID,
                    "pipeline/gui_textured_lightning"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build();

    /** Заливка (fill) с бленд-функцией SRC_ALPHA/ONE. */
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
