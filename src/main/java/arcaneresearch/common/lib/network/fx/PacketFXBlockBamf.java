package arcaneresearch.common.lib.network.fx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXBlockBamf {
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
    public static void encode(PacketFXBlockBamf packet, FriendlyByteBuf buffer) {
        buffer.writeDouble(packet.x);
        buffer.writeDouble(packet.y);
        buffer.writeDouble(packet.z);
        buffer.writeInt(packet.color);
        buffer.writeByte(packet.flags);
        buffer.writeByte(packet.face);
    }
    public static PacketFXBlockBamf decode(FriendlyByteBuf buffer) {
        return new PacketFXBlockBamf(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readInt(),
                buffer.readByte(),
                buffer.readByte()
        );
    }
    public static void handle(PacketFXBlockBamf packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
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
