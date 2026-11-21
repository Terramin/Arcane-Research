package arcaneresearch.client.lib.events;

import arcaneresearch.Arcaneresearch;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class BlurVisionHandler {

    private static final List<Identifier> CANCELLED_OVERLAYS = List.of(
            Identifier.withDefaultNamespace("vignette"),
            Identifier.withDefaultNamespace("spyglass"),
            Identifier.withDefaultNamespace("helmet"),
            Identifier.withDefaultNamespace("frostbite"),
            Identifier.withDefaultNamespace("portal"));

    private BlurVisionHandler() {
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (TCShaderHandler.isBlurActive() && CANCELLED_OVERLAYS.contains(event.getName())) {
            event.setCanceled(true);
        }
    }
}
