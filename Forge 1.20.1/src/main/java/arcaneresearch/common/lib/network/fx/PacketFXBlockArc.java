package arcaneresearch.common.lib.network.fx;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXBlockArc {
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
    public static void encode(PacketFXBlockArc packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.x);
        buffer.writeInt(packet.y);
        buffer.writeInt(packet.z);
        buffer.writeFloat(packet.tx);
        buffer.writeFloat(packet.ty);
        buffer.writeFloat(packet.tz);
        buffer.writeFloat(packet.r);
        buffer.writeFloat(packet.g);
        buffer.writeFloat(packet.b);
    }
    public static PacketFXBlockArc decode(FriendlyByteBuf buffer) {
        return new PacketFXBlockArc(
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }
    public static void handle(PacketFXBlockArc packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXBlockArc packet) {
        FXDispatcher.INSTANCE.arcLightning(
                packet.tx, packet.ty, packet.tz,
                packet.x + 0.5, packet.y + 0.5, packet.z + 0.5,
                packet.r, packet.g, packet.b,
                0.5f);
    }
}
