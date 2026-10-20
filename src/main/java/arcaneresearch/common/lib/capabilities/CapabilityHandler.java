package arcaneresearch.common.lib.capabilities;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityHandler {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerKnowledge.class);
        event.register(IPlayerWarp.class);
        Arcaneresearch.LOGGER.info("Arcaneresearch capabilities registered");
    }
}
