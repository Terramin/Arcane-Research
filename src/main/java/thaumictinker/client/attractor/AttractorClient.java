package thaumictinker.client.attractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import thaumictinker.attractor.AttractorFeature;

 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AttractorClient {

    private AttractorClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                net.minecraft.client.gui.screens.MenuScreens.register(
                        AttractorFeature.CORPOREAL_MENU, CorporealAttractorScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AttractorFeature.KINETIC_ENTITY, AttractorRenderer::new);
        event.registerBlockEntityRenderer(AttractorFeature.CORPOREAL_ENTITY, AttractorRenderer::new);
    }
}
