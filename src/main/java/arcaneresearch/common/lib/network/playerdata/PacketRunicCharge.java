package arcaneresearch.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;






public class PacketRunicCharge implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketRunicCharge> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "runic_charge"));

    public static final StreamCodec<ByteBuf, PacketRunicCharge> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketRunicCharge packet) {
            buf.writeInt(packet.playerId);
            buf.writeInt(packet.charge);
            buf.writeInt(packet.max);
        }

        @Override
        public PacketRunicCharge decode(ByteBuf buf) {
            return new PacketRunicCharge(buf.readInt(), buf.readInt(), buf.readInt());
        }
    };

    private final int playerId;
    private final int charge;
    private final int max;

    public PacketRunicCharge(int playerId, int charge, int max) {
        this.playerId = playerId;
        this.charge = charge;
        this.max = max;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketRunicCharge msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketRunicCharge msg) {
        arcaneresearch.client.lib.events.RunicShieldHud.applySync(
                msg.playerId, msg.charge, msg.max);
    }
}
