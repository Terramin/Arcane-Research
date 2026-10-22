package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXBlockArc implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXBlockArc> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_block_arc"));

    public static final StreamCodec<ByteBuf, PacketFXBlockArc> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXBlockArc packet) {
            buf.writeInt(packet.x);
            buf.writeInt(packet.y);
            buf.writeInt(packet.z);
            buf.writeFloat(packet.tx);
            buf.writeFloat(packet.ty);
            buf.writeFloat(packet.tz);
            buf.writeFloat(packet.r);
            buf.writeFloat(packet.g);
            buf.writeFloat(packet.b);
        }

        @Override
        public PacketFXBlockArc decode(ByteBuf buf) {
            return new PacketFXBlockArc(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
            );
        }
    };


    private final int x;
    private final int y;
    private final int z;
    private final float tx;
    private final float ty;
    private final float tz;
    private final float r;
    private final float g;
    private final float b;

    public PacketFXBlockArc(BlockPos pos, Entity target, float r, float g, float b) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.tx = (float) target.getX();
        this.ty = (float) (target.getBoundingBox().minY + target.getBbHeight() / 2.0f);
        this.tz = (float) target.getZ();
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public PacketFXBlockArc(BlockPos pos, BlockPos target, float r, float g, float b) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.tx = target.getX() + 0.5f;
        this.ty = target.getY() + 0.5f;
        this.tz = target.getZ() + 0.5f;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public PacketFXBlockArc(int x, int y, int z, float tx, float ty, float tz, float r, float g, float b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockArc msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

    private static void handleClient(PacketFXBlockArc packet) {
            FXDispatcher.INSTANCE.arcLightning(
                    packet.tx, packet.ty, packet.tz,
                    packet.x + 0.5, packet.y + 0.5, packet.z + 0.5,
                    packet.r, packet.g, packet.b,
                    0.5f);
        }
}
