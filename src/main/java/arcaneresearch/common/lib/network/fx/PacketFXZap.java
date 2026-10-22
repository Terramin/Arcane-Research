package arcaneresearch.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;






public class PacketFXZap implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXZap> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_zap"));

    public static final StreamCodec<ByteBuf, PacketFXZap> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXZap packet) {
            buf.writeDouble(packet.source.x);
            buf.writeDouble(packet.source.y);
            buf.writeDouble(packet.source.z);
            buf.writeDouble(packet.target.x);
            buf.writeDouble(packet.target.y);
            buf.writeDouble(packet.target.z);
            buf.writeInt(packet.color);
            buf.writeFloat(packet.width);
        }

        @Override
        public PacketFXZap decode(ByteBuf buf) {
            return new PacketFXZap(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readInt(),
                    buf.readFloat());
        }
    };

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXZap msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

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