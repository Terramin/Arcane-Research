package arcaneresearch.init;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.CommandArcaneresearch;



@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandArcaneresearch.register(event.getDispatcher());
    }
}
