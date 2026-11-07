package arcaneresearch.client.lib.events;

import arcaneresearch.Arcaneresearch;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BlurVisionHandler {

    private static final List<ResourceLocation> CANCELLED_OVERLAYS = List.of(
            ResourceLocation.withDefaultNamespace("vignette"),
            ResourceLocation.withDefaultNamespace("spyglass"),
            ResourceLocation.withDefaultNamespace("helmet"),
            ResourceLocation.withDefaultNamespace("frostbite"),
            ResourceLocation.withDefaultNamespace("portal"));

    private BlurVisionHandler() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (TCShaderHandler.isBlurActive() && CANCELLED_OVERLAYS.contains(event.getOverlay().id())) {
            event.setCanceled(true);
        }
    }
}
