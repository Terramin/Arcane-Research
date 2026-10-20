package arcaneresearch.common.lib.network.fx;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXZap {
    private final Vec3 source;
    private final Vec3 target;
    private final int color;
    private final float width;
    public PacketFXZap(Vec3 source, Vec3 target, int color, float width) {
        this.source = source;
        this.target = target;
        this.color = color;
        this.width = width;
    }
    public PacketFXZap(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        this(new Vec3(sx, sy, sz), new Vec3(tx, ty, tz), color, width);
    }
    public static void encode(PacketFXZap packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.source.x);
        buffer.writeDouble(packet.source.y);
        buffer.writeDouble(packet.source.z);
        buffer.writeDouble(packet.target.x);
        buffer.writeDouble(packet.target.y);
        buffer.writeDouble(packet.target.z);
        buffer.writeInt(packet.color);
        buffer.writeFloat(packet.width);
    }
    public static PacketFXZap decode(FriendlyByteBuf buffer) {
        return new PacketFXZap(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readInt(),
                buffer.readFloat()
        );
    }
    public static void handle(PacketFXZap packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXZap packet) {
        int color = packet.color;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        FXDispatcher.INSTANCE.arcBolt(
                packet.source.x, packet.source.y, packet.source.z,
                packet.target.x, packet.target.y, packet.target.z,
                r, g, b,
                packet.width);
    }
}
