package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXSonic {
    private final int sourceId;
    public PacketFXSonic(Entity source) {
        this.sourceId = source.getId();
    }
    public PacketFXSonic(int sourceId) {
        this.sourceId = sourceId;
    }
    public static void encode(PacketFXSonic packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.sourceId);
    }
    public static PacketFXSonic decode(FriendlyByteBuf buffer) {
        int sourceId = buffer.readInt();
        return new PacketFXSonic(sourceId);
    }
    public static void handle(PacketFXSonic packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXSonic packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        Entity source = level.getEntity(packet.sourceId);
        if (source != null) {
            FXDispatcher.INSTANCE.sonicBoom(source.getX(), source.getY(), source.getZ(), source, 10);
        }
    }
}
