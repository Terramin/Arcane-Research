package arcaneresearch.invscan;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;

/**
 * Клиент -&gt; сервер: «сканирую слот N меню M» (таумометр на курсоре).
 *
 * NEOFORGE_PORT: Forge FriendlyByteBuf encode/decode/handle -&gt;
 * CustomPacketPayload + ручной StreamCodec (порядок полей 1:1).
 * Payload id "arcaneresearch:invscan_slot".
 */
public class MsgScanSlot implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgScanSlot> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "invscan_slot"));

    public static final StreamCodec<ByteBuf, MsgScanSlot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgScanSlot packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.containerId);
            ByteBufCodecs.VAR_INT.encode(buf, packet.slotIndex);
        }

        @Override
        public MsgScanSlot decode(ByteBuf buf) {
            return new MsgScanSlot(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
        }
    };

    private final int containerId;
    private final int slotIndex;

    public MsgScanSlot(int containerId, int slotIndex) {
        this.containerId = containerId;
        this.slotIndex = slotIndex;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgScanSlot msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                InvScanServer.handleSlot(player, msg.containerId, msg.slotIndex);
            }
        });
    }
}
