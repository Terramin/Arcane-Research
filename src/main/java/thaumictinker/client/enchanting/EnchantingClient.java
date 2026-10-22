package thaumictinker.client.enchanting;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import thaumictinker.enchanting.EnchantingFeature;

 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EnchantingClient {

    private EnchantingClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                net.minecraft.client.gui.screens.MenuScreens.register(
                        EnchantingFeature.ENCHANTER_MENU, EnchanterScreen::new));
    }

    @SubscribeEvent
    public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(EnchantingFeature.ENCHANTER_ENTITY, EnchanterRenderer::new);
    }
}
