package arcaneresearch.invscan;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.helper.IDsHelper;
/**
 * Port of Thaumic Inventory Scanning (BlayTheNinth, MIT) network plumbing.
 * Lets a player scan by hovering targets inside container menus.
 */
public class InvScanNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            IDsHelper.mod("invscan"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int nextId = 0;
    public static void register() {
        CHANNEL.registerMessage(nextId++, MsgScanSlot.class,
                MsgScanSlot::encode, MsgScanSlot::new, MsgScanSlot::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, MsgScanSelf.class,
                MsgScanSelf::encode, MsgScanSelf::new, MsgScanSelf::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(InvScanNetwork::register);
        }
    }
}
