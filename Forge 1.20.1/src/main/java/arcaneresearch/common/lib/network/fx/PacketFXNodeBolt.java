package arcaneresearch.common.lib.network.fx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXNodeBolt {
    private final double x1;
    private final double y1;
    private final double z1;
    private final double x2;
    private final double y2;
    private final double z2;
    private final long seed;
    public PacketFXNodeBolt(double x1, double y1, double z1,
                            double x2, double y2, double z2, long seed) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.seed = seed;
    }
    public static void encode(PacketFXNodeBolt packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x1);
        buffer.writeDouble(packet.y1);
        buffer.writeDouble(packet.z1);
        buffer.writeDouble(packet.x2);
        buffer.writeDouble(packet.y2);
        buffer.writeDouble(packet.z2);
        buffer.writeLong(packet.seed);
    }
    public static PacketFXNodeBolt decode(FriendlyByteBuf buffer) {
        return new PacketFXNodeBolt(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readLong());
    }
    public static void handle(PacketFXNodeBolt packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXNodeBolt packet) {
        FXDispatcher.INSTANCE.nodeBolt(packet.x1, packet.y1, packet.z1,
                packet.x2, packet.y2, packet.z2, packet.seed);
    }
}
