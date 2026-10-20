package arcaneresearch.common.lib.network.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
public class PacketStartTheoryToServer {
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
    public static void encode(PacketStartTheoryToServer packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.pos);
        buf.writeVarInt(packet.aids.size());
        for (String aid : packet.aids) {
            buf.writeUtf(aid);
        }
    }
    public static PacketStartTheoryToServer decode(FriendlyByteBuf buf) {
        long pos = buf.readLong();
        int size = buf.readVarInt();
        Set<String> aids = new HashSet<>();
        for (int i = 0; i < size; i++) {
            aids.add(buf.readUtf(32767));
        }
        return new PacketStartTheoryToServer(pos, aids);
    }
    public static void handle(PacketStartTheoryToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            BlockPos blockPos = BlockPos.of(packet.pos);
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TileResearchTable researchTable) {
                researchTable.startNewTheory(player, packet.aids);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
