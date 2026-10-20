package arcaneresearch.compat.jei;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent; // NEOFORGE_PORT: TickEvent.ClientTickEvent removed -> client ClientTickEvent Pre/Post
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod (annotation-only)
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ResearchEvent;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class ResearchGateEvents {

    private ResearchGateEvents() {
    }

    @SubscribeEvent
    public static void onResearch(ResearchEvent.Research event) {
        if (event.getPlayer() != null && event.getPlayer().level().isClientSide()) {
            ResearchGate.requestRefresh();
        }
    }

    @SubscribeEvent
    public static void onLevelJoin(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ResearchGate.requestRefresh();
        }
    }

    // NEOFORGE_PORT: phased ClientTickEvent -> Post (fires at end of tick).
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ResearchGate.clientTick();
    }
}
