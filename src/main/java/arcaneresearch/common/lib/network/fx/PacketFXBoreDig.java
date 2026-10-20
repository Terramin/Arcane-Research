package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXBoreDig {
    private final int x;
    private final int y;
    private final int z;
    private final int boreId;
    private final int delay;
    public PacketFXBoreDig(BlockPos pos, Entity bore, int delay) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.boreId = bore.getId();
        this.delay = delay;
    }
    private PacketFXBoreDig(int x, int y, int z, int boreId, int delay) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.boreId = boreId;
        this.delay = delay;
    }
    public static void encode(PacketFXBoreDig packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.x);
        buffer.writeInt(packet.y);
        buffer.writeInt(packet.z);
        buffer.writeInt(packet.boreId);
        buffer.writeInt(packet.delay);
    }
    public static PacketFXBoreDig decode(FriendlyByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        int boreId = buffer.readInt();
        int delay = buffer.readInt();
        return new PacketFXBoreDig(x, y, z, boreId, delay);
    }
    public static void handle(PacketFXBoreDig packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXBoreDig packet) {
        try {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            BlockPos pos = new BlockPos(packet.x, packet.y, packet.z);
            Entity bore = level.getEntity(packet.boreId);
            if (bore == null) {
                return;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.AIR)) {
                return;
            }
            FXDispatcher.INSTANCE.boreDigFx(
                    pos.getX(), pos.getY(), pos.getZ(),
                    bore, state, 0, packet.delay
            );
        } catch (Exception ignored) {
        }
    }
}
