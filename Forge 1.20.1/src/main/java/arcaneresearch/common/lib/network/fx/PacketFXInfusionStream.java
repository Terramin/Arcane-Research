package arcaneresearch.common.lib.network.fx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
public class PacketFXInfusionStream {
    private final int streamId;
    private final BlockPos source;
    private final BlockPos target;
    private final int color;
    private final int duration;
    private final long startTick;
    private final long serverNow;
    private final List<Vec3> waypoints;
    public PacketFXInfusionStream(int streamId, BlockPos source, BlockPos target, int color,
                                  int duration, long startTick, long serverNow, List<Vec3> waypoints) {
        this.streamId = streamId;
        this.source = source;
        this.target = target;
        this.color = color;
        this.duration = duration;
        this.startTick = startTick;
        this.serverNow = serverNow;
        this.waypoints = new ArrayList<>(waypoints);
    }
    private PacketFXInfusionStream(int streamId, BlockPos source, BlockPos target, int color,
                                   int duration, long startTick, long serverNow) {
        this(streamId, source, target, color, duration, startTick, serverNow, new ArrayList<>());
    }
    public static void encode(PacketFXInfusionStream packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.streamId);
        buffer.writeBlockPos(packet.source);
        buffer.writeBlockPos(packet.target);
        buffer.writeInt(packet.color);
        buffer.writeInt(packet.duration);
        buffer.writeLong(packet.startTick);
        buffer.writeLong(packet.serverNow);
        buffer.writeVarInt(packet.waypoints.size());
        for (Vec3 p : packet.waypoints) {
            buffer.writeDouble(p.x);
            buffer.writeDouble(p.y);
            buffer.writeDouble(p.z);
        }
    }
    public static PacketFXInfusionStream decode(FriendlyByteBuf buffer) {
        int streamId = buffer.readVarInt();
        BlockPos source = buffer.readBlockPos();
        BlockPos target = buffer.readBlockPos();
        int color = buffer.readInt();
        int duration = buffer.readInt();
        long startTick = buffer.readLong();
        long serverNow = buffer.readLong();
        int count = buffer.readVarInt();
        if (count < 0 || count > 1024) {
            throw new io.netty.handler.codec.DecoderException(
                    "PacketFXInfusionStream waypoint count out of range: " + count);
        }
        PacketFXInfusionStream packet = new PacketFXInfusionStream(streamId, source, target, color, duration, startTick, serverNow);
        for (int i = 0; i < count; i++) {
            packet.waypoints.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
        }
        return packet;
    }
    public static void handle(PacketFXInfusionStream packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXInfusionStream packet) {
        FXDispatcher.INSTANCE.infusionStreamFx(packet.streamId, packet.source, packet.target, packet.color,
                packet.duration, packet.startTick, packet.serverNow, packet.waypoints);
    }
}
