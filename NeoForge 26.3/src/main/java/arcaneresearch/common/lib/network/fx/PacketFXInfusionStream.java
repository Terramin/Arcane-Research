package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXInfusionStream.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_infusion_stream".
public class PacketFXInfusionStream implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXInfusionStream> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_infusion_stream"));

    public static final StreamCodec<ByteBuf, PacketFXInfusionStream> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXInfusionStream packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.streamId);
            BlockPos.STREAM_CODEC.encode(buf, packet.source);
            BlockPos.STREAM_CODEC.encode(buf, packet.target);
            buf.writeInt(packet.color);
            buf.writeInt(packet.duration);
            buf.writeLong(packet.startTick);
            buf.writeLong(packet.serverNow);
            ByteBufCodecs.VAR_INT.encode(buf, packet.waypoints.size());
            for (Vec3 p : packet.waypoints) {
            buf.writeDouble(p.x);
            buf.writeDouble(p.y);
            buf.writeDouble(p.z);
            }
        }

        @Override
        public PacketFXInfusionStream decode(ByteBuf buf) {
            int streamId = ByteBufCodecs.VAR_INT.decode(buf);
            BlockPos source = BlockPos.STREAM_CODEC.decode(buf);
            BlockPos target = BlockPos.STREAM_CODEC.decode(buf);
            int color = buf.readInt();
            int duration = buf.readInt();
            long startTick = buf.readLong();
            long serverNow = buf.readLong();
            int count = ByteBufCodecs.VAR_INT.decode(buf);

            if (count < 0 || count > 1024) {
            throw new io.netty.handler.codec.DecoderException(
            "PacketFXInfusionStream waypoint count out of range: " + count);
            }
            PacketFXInfusionStream packet = new PacketFXInfusionStream(streamId, source, target, color, duration, startTick, serverNow);
            for (int i = 0; i < count; i++) {
            packet.waypoints.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            }
            return packet;
        }
    };


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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXInfusionStream msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

    private static void handleClient(PacketFXInfusionStream packet) {
            FXDispatcher.INSTANCE.infusionStreamFx(packet.streamId, packet.source, packet.target, packet.color,
                    packet.duration, packet.startTick, packet.serverNow, packet.waypoints);
        }
}
