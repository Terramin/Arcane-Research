package arcaneresearch.invscan;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import arcaneresearch.Arcaneresearch;

/**
 * Port of Thaumic Inventory Scanning (BlayTheNinth, MIT) network plumbing.
 * Lets a player scan by hovering targets inside container menus.
 *
 * NEOFORGE_PORT: Forge SimpleChannel("arcaneresearch:invscan") -&gt; CustomPacketPayload
 * регистр (тот же modid/version "1", что у PacketHandler/ScanNetwork;
 * playToServer для обоих сообщений; payload-id "invscan_slot"/"invscan_self").
 * Отправка клиентом — PacketHandler.sendToServer (ClientPacketDistributor).
 */
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
