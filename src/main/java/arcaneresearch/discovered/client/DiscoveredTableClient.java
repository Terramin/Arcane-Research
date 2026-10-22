package arcaneresearch.discovered.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.table.DiscoveredTableSetup;

 









 
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class DiscoveredTableClient {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        if (DiscoveredTableSetup.getMenuType() != null) {
            event.register(DiscoveredTableSetup.getMenuType(), CognitionNoteTableScreen::new);
        }
    }
}
