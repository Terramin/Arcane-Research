package arcaneresearch.common.lib.network.fx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.lib.events.EssentiaHandler;
import java.util.function.Supplier;
public class PacketFXEssentiaSource {
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
    public static void encode(PacketFXEssentiaSource packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.x);
        buffer.writeInt(packet.y);
        buffer.writeInt(packet.z);
        buffer.writeInt(packet.color);
        buffer.writeByte(packet.dx);
        buffer.writeByte(packet.dy);
        buffer.writeByte(packet.dz);
        buffer.writeShort(packet.ext);
    }
    public static PacketFXEssentiaSource decode(FriendlyByteBuf buffer) {
        int x = buffer.readInt();
        int y = buffer.readInt();
        int z = buffer.readInt();
        int color = buffer.readInt();
        byte dx = buffer.readByte();
        byte dy = buffer.readByte();
        byte dz = buffer.readByte();
        int ext = buffer.readShort();
        return new PacketFXEssentiaSource(x, y, z, dx, dy, dz, color, ext);
    }
    public static void handle(PacketFXEssentiaSource packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
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
