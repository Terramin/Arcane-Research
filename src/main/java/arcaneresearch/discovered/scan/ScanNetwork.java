package arcaneresearch.discovered.scan;

import arcaneresearch.helper.IDsHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import arcaneresearch.Arcaneresearch;

public class ScanNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            IDsHelper.mod("hex_scan"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int nextId = 0;

    public static void register() {
        CHANNEL.registerMessage(nextId++, MsgStudyComplete.class,
                MsgStudyComplete::encode, MsgStudyComplete::new, MsgStudyComplete::handle);
        CHANNEL.registerMessage(nextId++, MsgStudyReward.class,
                MsgStudyReward::encode, MsgStudyReward::new, MsgStudyReward::handle);
        CHANNEL.registerMessage(nextId++, MsgWalletSync.class,
                MsgWalletSync::encode, MsgWalletSync::new, MsgWalletSync::handle);
        CHANNEL.registerMessage(nextId++, MsgWalletRequest.class,
                MsgWalletRequest::encode, MsgWalletRequest::new, MsgWalletRequest::handle);
    }

    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(ScanNetwork::register);
        }
    }
}
