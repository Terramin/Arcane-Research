package arcaneresearch.common.lib.network.fx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXBlockMist {
    private final long loc;
    private final int color;
    public PacketFXBlockMist(BlockPos pos, int color) {
        this.loc = pos.asLong();
        this.color = color;
    }
    private PacketFXBlockMist(long loc, int color) {
        this.loc = loc;
        this.color = color;
    }
    public static void encode(PacketFXBlockMist packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.loc);
        buffer.writeInt(packet.color);
    }
    public static PacketFXBlockMist decode(FriendlyByteBuf buffer) {
        long loc = buffer.readLong();
        int color = buffer.readInt();
        return new PacketFXBlockMist(loc, color);
    }
    public static void handle(PacketFXBlockMist packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXBlockMist packet) {
        BlockPos pos = BlockPos.of(packet.loc);
        FXDispatcher.INSTANCE.drawBlockMistParticles(pos, packet.color);
    }
}
