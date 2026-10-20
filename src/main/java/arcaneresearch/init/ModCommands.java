package arcaneresearch.init;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.CommandArcaneresearch;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandArcaneresearch.register(event.getDispatcher());
    }
}
