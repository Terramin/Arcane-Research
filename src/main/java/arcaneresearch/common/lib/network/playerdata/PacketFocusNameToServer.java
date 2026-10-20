package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketFocusNameToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:focus_name_to_server".
public class PacketFocusNameToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFocusNameToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_name_to_server"));

    public static final StreamCodec<ByteBuf, PacketFocusNameToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFocusNameToServer packet) {
            buf.writeLong(packet.pos);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.name);
        }

        @Override
        public PacketFocusNameToServer decode(ByteBuf buf) {
            return new PacketFocusNameToServer(
            buf.readLong(),
            ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }
    };

    private final long pos;
    private final String name;

    public PacketFocusNameToServer(BlockPos pos, String name) {
        this.pos = pos.asLong();
        this.name = name;
    }

    private PacketFocusNameToServer(long pos, String name) {
        this.pos = pos;
        this.name = name;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusNameToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            Level level = player.level();
            BlockPos blockPos = BlockPos.of(packet.pos);

            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TileFocalManipulator focalManipulator) {
                focalManipulator.focusName = packet.name;
                focalManipulator.setChanged();
            }
        });
    }
}
