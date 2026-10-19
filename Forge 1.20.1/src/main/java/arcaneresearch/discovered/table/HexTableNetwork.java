package arcaneresearch.discovered.table;
import arcaneresearch.helper.IDsHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import arcaneresearch.Arcaneresearch;
public class HexTableNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            IDsHelper.mod("hex_table"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int nextId = 0;
    public static void register() {
        CHANNEL.registerMessage(nextId++, MsgPlaceAspect.class,
                MsgPlaceAspect::encode, MsgPlaceAspect::new, MsgPlaceAspect::handle);
        CHANNEL.registerMessage(nextId++, MsgCombine.class,
                MsgCombine::encode, MsgCombine::new, MsgCombine::handle);
        CHANNEL.registerMessage(nextId++, MsgSyncNote.class,
                MsgSyncNote::encode, MsgSyncNote::new, MsgSyncNote::handle);
        CHANNEL.registerMessage(nextId++, MsgDupe.class,
                MsgDupe::encode, MsgDupe::new, MsgDupe::handle);
        CHANNEL.registerMessage(nextId++, MsgEnsureHex.class,
                MsgEnsureHex::encode, MsgEnsureHex::new, MsgEnsureHex::handle);
        CHANNEL.registerMessage(nextId++, MsgStudyTable.class,
                MsgStudyTable::encode, MsgStudyTable::new, MsgStudyTable::handle);
    }
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(HexTableNetwork::register);
        }
    }
}
