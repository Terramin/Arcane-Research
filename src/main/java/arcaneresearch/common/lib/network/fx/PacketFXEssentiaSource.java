package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.events.EssentiaHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXEssentiaSource implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXEssentiaSource> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_essentia_source"));

    public static final StreamCodec<ByteBuf, PacketFXEssentiaSource> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXEssentiaSource packet) {
            buf.writeInt(packet.x);
            buf.writeInt(packet.y);
            buf.writeInt(packet.z);
            buf.writeInt(packet.color);
            buf.writeByte(packet.dx);
            buf.writeByte(packet.dy);
            buf.writeByte(packet.dz);
            buf.writeShort(packet.ext);
        }

        @Override
        public PacketFXEssentiaSource decode(ByteBuf buf) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            int color = buf.readInt();
            byte dx = buf.readByte();
            byte dy = buf.readByte();
            byte dz = buf.readByte();
            int ext = buf.readShort();
            return new PacketFXEssentiaSource(x, y, z, dx, dy, dz, color, ext);
        }
    };


    private final int x;
    private final int y;
    private final int z;
    private final byte dx;
    private final byte dy;
    private final byte dz;
    private final int color;
    private final int ext;

    public PacketFXEssentiaSource(BlockPos source, byte dx, byte dy, byte dz, int color, int ext) {
        this.x = source.getX();
        this.y = source.getY();
        this.z = source.getZ();
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.color = color;
        this.ext = ext;
    }

    public PacketFXEssentiaSource(BlockPos source, BlockPos target, int color, int ext) {
        this(source,
                (byte) (source.getX() - target.getX()),
                (byte) (source.getY() - target.getY()),
                (byte) (source.getZ() - target.getZ()),
                color, ext);
    }

    private PacketFXEssentiaSource(int x, int y, int z, byte dx, byte dy, byte dz, int color, int ext) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.color = color;
        this.ext = ext;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXEssentiaSource msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

    private static void handleClient(PacketFXEssentiaSource packet) {
    
            int tx = packet.x - packet.dx;
            int ty = packet.y - packet.dy;
            int tz = packet.z - packet.dz;
            String key = packet.x + ":" + packet.y + ":" + packet.z + ":"
                    + tx + ":" + ty + ":" + tz + ":" + packet.color;
            if (EssentiaHandler.sourceFX.containsKey(key)) {
                EssentiaHandler.EssentiaSourceFX sf = EssentiaHandler.sourceFX.get(key);
                EssentiaHandler.sourceFX.remove(key);
                EssentiaHandler.sourceFX.put(key, sf);
            } else {
                EssentiaHandler.sourceFX.put(key, new EssentiaHandler.EssentiaSourceFX(
                        new BlockPos(packet.x, packet.y, packet.z),
                        new BlockPos(tx, ty, tz), packet.color, packet.ext));
            }
        }
}
