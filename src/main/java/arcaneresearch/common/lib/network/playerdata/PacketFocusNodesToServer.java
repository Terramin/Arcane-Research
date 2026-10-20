package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.tiles.crafting.FocusElementNode;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import java.util.HashMap;
import java.util.function.Supplier;
public class PacketFocusNodesToServer {
    private final long pos;
    private final HashMap<Integer, FocusElementNode> data;
    private final String name;
    public PacketFocusNodesToServer(BlockPos pos, HashMap<Integer, FocusElementNode> data, String name) {
        this.pos = pos.asLong();
        this.data = data;
        this.name = name;
    }
    private PacketFocusNodesToServer(long pos, HashMap<Integer, FocusElementNode> data, String name) {
        this.pos = pos;
        this.data = data;
        this.name = name;
    }
    public static void encode(PacketFocusNodesToServer packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.pos);
        buf.writeByte(packet.data.size());
        for (FocusElementNode node : packet.data.values()) {
            CompoundTag nodeTag = node.serialize();
            buf.writeNbt(nodeTag);
        }
        buf.writeUtf(packet.name);
    }
    public static PacketFocusNodesToServer decode(FriendlyByteBuf buf) {
        long pos = buf.readLong();
        int count = buf.readByte();
        HashMap<Integer, FocusElementNode> data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            CompoundTag nodeTag = buf.readNbt();
            if (nodeTag != null) {
                FocusElementNode node = new FocusElementNode();
                node.deserialize(nodeTag);
                data.put(node.id, node);
            }
        }
        String name = buf.readUtf(32767);
        return new PacketFocusNodesToServer(pos, data, name);
    }
    public static void handle(PacketFocusNodesToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            BlockPos blockPos = BlockPos.of(packet.pos);
            if (!level.isLoaded(blockPos)) return;
            if (player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > 64.0) return;
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TileFocalManipulator focalManipulator) {
                focalManipulator.data.clear();
                focalManipulator.data.putAll(packet.data);
                focalManipulator.focusName = packet.name;
                focalManipulator.setChanged();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
