package arcaneresearch.discovered.table;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import arcaneresearch.Arcaneresearch;

/**
 * Регистрация сети мини-игры исследовательского стола (стадия 2).
 * Ничего в проектной регистрации пакетов не трогает: свои сообщения.
 * Серверная привязка к реальному тайлу/меню стола — вопрос отдельно;
 * источником записки на сервере служит предмет в слоте меню
 * (см. хендлеры).
 *
 * NEOFORGE_PORT: отдельный SimpleChannel "hex_table" -&gt; те же payload-id
 * в общем реестре мода (свой RegisterPayloadHandlersEvent-регистратор,
 * тот же modid/version "1", что у PacketHandler; типы не пересекаются).
 * Отправки: сервер -&gt; PacketHandler.sendToPlayer, клиент -&gt;
 * PacketHandler.sendToServer (см. Msg*).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class HexTableNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(Arcaneresearch.MODID).versioned("1");
        registrar.playToServer(
                MsgPlaceAspect.TYPE,
                MsgPlaceAspect.STREAM_CODEC,
                MsgPlaceAspect::handle);
        registrar.playToServer(
                MsgCombine.TYPE,
                MsgCombine.STREAM_CODEC,
                MsgCombine::handle);
        registrar.playToClient(
                MsgSyncNote.TYPE,
                MsgSyncNote.STREAM_CODEC,
                MsgSyncNote::handle);
        registrar.playToServer(
                MsgDupe.TYPE,
                MsgDupe.STREAM_CODEC,
                MsgDupe::handle);
        registrar.playToServer(
                MsgEnsureHex.TYPE,
                MsgEnsureHex.STREAM_CODEC,
                MsgEnsureHex::handle);
        registrar.playToServer(
                MsgStudyTable.TYPE,
                MsgStudyTable.STREAM_CODEC,
                MsgStudyTable::handle);
    }
}
