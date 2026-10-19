package arcaneresearch.discovered.scan;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import arcaneresearch.Arcaneresearch;

/**
 * Регистрация сети изучения таумометром. Свои сообщения, регистрация
 * пакетов проекта не тронута.
 *
 * NEOFORGE_PORT: отдельный SimpleChannel "hex_scan" -&gt; те же payload-id
 * в общем реестре мода (свой RegisterPayloadHandlersEvent-регистратор,
 * тот же modid/version "1", что у PacketHandler; типы не пересекаются).
 * Отправки: сервер -&gt; PacketHandler.sendToPlayer, клиент -&gt;
 * PacketHandler.sendToServer (см. Msg*).
 */
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
