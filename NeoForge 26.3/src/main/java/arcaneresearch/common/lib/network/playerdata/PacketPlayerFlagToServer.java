package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketPlayerFlagToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:player_flag_to_server".
public class PacketPlayerFlagToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketPlayerFlagToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "player_flag_to_server"));

    public static final StreamCodec<ByteBuf, PacketPlayerFlagToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketPlayerFlagToServer packet) {
            buf.writeByte(packet.flag);
        }

        @Override
        public PacketPlayerFlagToServer decode(ByteBuf buf) {
            return new PacketPlayerFlagToServer(buf.readByte());
        }
    };

    public static final byte FLAG_RESET_FALL_DISTANCE = 1;

    private final byte flag;

    public PacketPlayerFlagToServer(int flag) {
        this.flag = (byte) flag;
    }

    private PacketPlayerFlagToServer(byte flag) {
        this.flag = flag;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketPlayerFlagToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            switch (packet.flag) {
                case FLAG_RESET_FALL_DISTANCE -> {

                    player.fallDistance = 0.0f;
                }

            }
        });
    }
}
