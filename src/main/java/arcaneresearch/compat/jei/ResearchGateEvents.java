package arcaneresearch.compat.jei;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ResearchEvent;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ResearchGateEvents {
    private ResearchGateEvents() {
    }
    @SubscribeEvent
    public static void onResearch(ResearchEvent.Research event) {
        if (event.getPlayer() != null && event.getPlayer().level().isClientSide) {
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ResearchGate.clientTick();
        }
    }
}
