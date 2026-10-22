package arcaneresearch.compat.jei;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent; 
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; 
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

    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ResearchGate.clientTick();
    }
}
