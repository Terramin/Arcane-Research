package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class PacketFXSlash {
    private final int sourceId;
    private final int targetId;
    public PacketFXSlash(int sourceId, int targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }
    public PacketFXSlash(Entity source, Entity target) {
        this.sourceId = source.getId();
        this.targetId = target.getId();
    }
    public static void encode(PacketFXSlash packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.sourceId);
        buffer.writeInt(packet.targetId);
    }
    public static PacketFXSlash decode(FriendlyByteBuf buffer) {
        return new PacketFXSlash(buffer.readInt(), buffer.readInt());
    }
    public static void handle(PacketFXSlash packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXSlash packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity source = mc.level.getEntity(packet.sourceId);
        Entity target = mc.level.getEntity(packet.targetId);
        if (source != null && target != null) {
            double sourceY = source.getBoundingBox().minY + source.getBbHeight() / 2.0f;
            double targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0f;
            FXDispatcher.INSTANCE.drawSlash(
                    source.getX(), sourceY, source.getZ(),
                    target.getX(), targetY, target.getZ(),
                    8);
        }
    }
}
