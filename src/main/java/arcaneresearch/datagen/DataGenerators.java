package arcaneresearch.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class DataGenerators {

    // NEOFORGE_PORT: GatherDataEvent is abstract in 26.x; subscribe to subclasses.
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        gatherData();
    }

    @SubscribeEvent
    public static void gatherServerData(GatherDataEvent.Server event) {
        gatherData();
    }

    private static void gatherData() {
        // TODO_PORT (datagen phase): 26.x GatherDataEvent has no ExistingFileHelper/getLookupProvider;
        // providers are added via event.addProvider/createProvider (see GatherDataEvent javap 26.3.0.23-beta).
        // Port Forge providers (blockstates/models/recipes/loot/tags) 1:1 here.

        Arcaneresearch.LOGGER.info("Arcaneresearch data generation setup complete");
    }
}
