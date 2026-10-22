package thaumictinker.client.trinkets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import thaumictinker.kami.trinkets.TrinketsFeature;

 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TrinketsClient {

    private TrinketsClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                net.minecraft.client.gui.screens.MenuScreens.register(
                        TrinketsFeature.ICHOR_POUCH_MENU, IchorPouchScreen::new));
    }
}
