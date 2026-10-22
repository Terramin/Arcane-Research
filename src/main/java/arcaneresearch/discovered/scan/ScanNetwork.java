package arcaneresearch.discovered.scan;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import arcaneresearch.Arcaneresearch;

 








 
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ScanNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(Arcaneresearch.MODID).versioned("1");
        registrar.playToServer(
                MsgStudyComplete.TYPE,
                MsgStudyComplete.STREAM_CODEC,
                MsgStudyComplete::handle);
        registrar.playToClient(
                MsgStudyReward.TYPE,
                MsgStudyReward.STREAM_CODEC,
                MsgStudyReward::handle);
        registrar.playToClient(
                MsgWalletSync.TYPE,
                MsgWalletSync.STREAM_CODEC,
                MsgWalletSync::handle);
        registrar.playToServer(
                MsgWalletRequest.TYPE,
                MsgWalletRequest.STREAM_CODEC,
                MsgWalletRequest::handle);
    }
}
