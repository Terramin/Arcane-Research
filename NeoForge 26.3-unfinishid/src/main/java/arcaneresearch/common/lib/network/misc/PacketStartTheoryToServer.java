package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketStartTheoryToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:start_theory_to_server".
public class PacketStartTheoryToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketStartTheoryToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "start_theory_to_server"));

    public static final StreamCodec<ByteBuf, PacketStartTheoryToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketStartTheoryToServer packet) {
            buf.writeLong(packet.pos);
            ByteBufCodecs.VAR_INT.encode(buf, packet.aids.size());
            for (String aid : packet.aids) {
                ByteBufCodecs.STRING_UTF8.encode(buf, aid);
            }
        }

        @Override
        public PacketStartTheoryToServer decode(ByteBuf buf) {
            long pos = buf.readLong();
            int size = ByteBufCodecs.VAR_INT.decode(buf);
            Set<String> aids = new HashSet<>();
            for (int i = 0; i < size; i++) {
                aids.add(ByteBufCodecs.STRING_UTF8.decode(buf));
            }
            return new PacketStartTheoryToServer(pos, aids);
        }
    };

    private final long pos;
    private final Set<String> aids;

    public PacketStartTheoryToServer(BlockPos pos, Set<String> aids) {
        this.pos = pos.asLong();
        this.aids = aids != null ? aids : new HashSet<>();
    }

    private PacketStartTheoryToServer(long pos, Set<String> aids) {
        this.pos = pos;
        this.aids = aids;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketStartTheoryToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            Level level = player.level();
            BlockPos blockPos = BlockPos.of(packet.pos);

            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TileResearchTable researchTable) {
                researchTable.startNewTheory(player, packet.aids);
            }
        });
    }
}
