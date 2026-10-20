package arcaneresearch.invscan;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;

/**
 * Клиент -&gt; сервер: «сканирую себя» (таумометр на курсоре, зона игрока
 * в родном инвентарном меню). Пустое тело.
 *
 * NEOFORGE_PORT: Forge FriendlyByteBuf-«пустышка» -&gt; CustomPacketPayload
 * (пустой StreamCodec). Payload id "arcaneresearch:invscan_self".
 */
public class MsgScanSelf implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgScanSelf> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "invscan_self"));

    public static final StreamCodec<ByteBuf, MsgScanSelf> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgScanSelf packet) {
        }

        @Override
        public MsgScanSelf decode(ByteBuf buf) {
            return new MsgScanSelf();
        }
    };

    public MsgScanSelf() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgScanSelf msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                InvScanServer.handleSelf(player);
            }
        });
    }
}
