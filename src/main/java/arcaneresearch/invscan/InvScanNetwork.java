package arcaneresearch.invscan;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import arcaneresearch.Arcaneresearch;

 







 
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class InvScanNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(Arcaneresearch.MODID).versioned("1");
        registrar.playToServer(
                MsgScanSlot.TYPE,
                MsgScanSlot.STREAM_CODEC,
                MsgScanSlot::handle);
        registrar.playToServer(
                MsgScanSelf.TYPE,
                MsgScanSelf.STREAM_CODEC,
                MsgScanSelf::handle);
    }
}
