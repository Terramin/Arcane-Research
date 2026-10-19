package arcaneresearch.common.lib.network.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.golems.GolemHelper;
import java.util.function.Supplier;
public class PacketLogisticsRequestToServer {
    private final BlockPos pos;
    private final Direction side;
    private final ItemStack stack;
    private final int stackSize;
    public PacketLogisticsRequestToServer(ItemStack stack, int size) {
        this.pos = null;
        this.side = null;
        this.stack = stack;
        this.stackSize = size;
    }
    public PacketLogisticsRequestToServer(BlockPos pos, Direction side, ItemStack stack, int size) {
        this.pos = pos;
        this.side = side;
        this.stack = stack;
        this.stackSize = size;
    }
    public static void encode(PacketLogisticsRequestToServer packet, FriendlyByteBuf buf) {
        if (packet.pos == null || packet.side == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(packet.pos.asLong());
            buf.writeByte(packet.side.ordinal());
        }
        buf.writeItem(packet.stack);
        buf.writeInt(packet.stackSize);
    }
    public static PacketLogisticsRequestToServer decode(FriendlyByteBuf buf) {
        boolean hasPos = buf.readBoolean();
        BlockPos pos = null;
        Direction side = null;
        if (hasPos) {
            pos = BlockPos.of(buf.readLong());
            side = Direction.values()[buf.readByte()];
        }
        ItemStack stack = buf.readItem();
        int stackSize = buf.readInt();
        if (pos != null) {
            return new PacketLogisticsRequestToServer(pos, side, stack, stackSize);
        } else {
            return new PacketLogisticsRequestToServer(stack, stackSize);
        }
    }
    public static void handle(PacketLogisticsRequestToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            Level level = player.level();
            int remainingSize = packet.stackSize;
            int uniqueId = 0;
            while (remainingSize > 0) {
                ItemStack requestStack = packet.stack.copy();
                int count = Math.min(remainingSize, requestStack.getMaxStackSize());
                requestStack.setCount(count);
                remainingSize -= count;
                if (packet.pos != null) {
                    GolemHelper.requestProvisioning(level, packet.pos, packet.side, requestStack, uniqueId);
                } else {
                    GolemHelper.requestProvisioning(level, player, requestStack, uniqueId);
                }
                uniqueId++;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
