package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXBlockBamf.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_block_bamf".
public class PacketFXBlockBamf implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXBlockBamf> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_block_bamf"));

    public static final StreamCodec<ByteBuf, PacketFXBlockBamf> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXBlockBamf packet) {
            buf.writeDouble(packet.x);
            buf.writeDouble(packet.y);
            buf.writeDouble(packet.z);
            buf.writeInt(packet.color);
            buf.writeByte(packet.flags);
            buf.writeByte(packet.face);
        }

        @Override
        public PacketFXBlockBamf decode(ByteBuf buf) {
            return new PacketFXBlockBamf(
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readInt(),
            buf.readByte(),
            buf.readByte()
            );
        }
    };


    private final double x;
    private final double y;
    private final double z;
    private final int color;
    private final byte flags;
    private final byte face;

    public PacketFXBlockBamf(double x, double y, double z, int color, boolean sound, boolean flair, Direction side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        int f = 0;
        if (sound) f = setBit(f, 0);
        if (flair) f = setBit(f, 1);
        this.face = (side != null) ? (byte) side.ordinal() : -1;
        this.flags = (byte) f;
    }

    public PacketFXBlockBamf(BlockPos pos, int color, boolean sound, boolean flair, Direction side) {
        this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, color, sound, flair, side);
    }

    private PacketFXBlockBamf(double x, double y, double z, int color, byte flags, byte face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
        this.flags = flags;
        this.face = face;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBlockBamf msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg));
    }

    private static void handleClient(PacketFXBlockBamf packet) {
            Direction side = null;
            if (packet.face >= 0 && packet.face < Direction.values().length) {
                side = Direction.values()[packet.face];
            }
    
            boolean sound = getBit(packet.flags, 0);
            boolean flair = getBit(packet.flags, 1);
    
            if (packet.color != -9999) {
                FXDispatcher.INSTANCE.drawBamf(packet.x, packet.y, packet.z, packet.color, sound, flair, side);
            } else {
                FXDispatcher.INSTANCE.drawBamf(packet.x, packet.y, packet.z, sound, flair, side);
            }
        }
    
        private static int setBit(int value, int bit) {
            return value | (1 << bit);
        }
    
        private static boolean getBit(int value, int bit) {
            return (value & (1 << bit)) != 0;
        }
}
