package arcaneresearch.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class DataGenerators {

    
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        gatherData();
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        gatherData();
    }

    private static void gatherData() {
        
        
        

        Arcaneresearch.LOGGER.info("Arcaneresearch data generation setup complete");
    }
}
