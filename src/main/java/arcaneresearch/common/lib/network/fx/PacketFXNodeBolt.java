package arcaneresearch.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;






public class PacketFXNodeBolt implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXNodeBolt> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_node_bolt"));

    public static final StreamCodec<ByteBuf, PacketFXNodeBolt> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXNodeBolt packet) {
            buf.writeDouble(packet.x1);
            buf.writeDouble(packet.y1);
            buf.writeDouble(packet.z1);
            buf.writeDouble(packet.x2);
            buf.writeDouble(packet.y2);
            buf.writeDouble(packet.z2);
            buf.writeLong(packet.seed);
        }

        @Override
        public PacketFXNodeBolt decode(ByteBuf buf) {
            return new PacketFXNodeBolt(
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readLong());
        }
    };

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXNodeBolt msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> FXDispatcher.INSTANCE.nodeBolt(
                msg.x1, msg.y1, msg.z1, msg.x2, msg.y2, msg.z2, msg.seed));
    }
}
