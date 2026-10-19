package arcaneresearch.init;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.CommandArcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge (RegisterCommandsEvent + CommandArcaneresearch.register;
// only bus/import changes: neo RegisterCommandsEvent on NeoForge.EVENT_BUS via @EventBusSubscriber).
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandArcaneresearch.register(event.getDispatcher());
    }
}
