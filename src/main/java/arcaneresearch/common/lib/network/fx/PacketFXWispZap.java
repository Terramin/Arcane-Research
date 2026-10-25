package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.EntityWisp;
import java.awt.*;
import java.util.function.Supplier;
public class PacketFXWispZap {
    private final int sourceEntityId;
    private final int targetEntityId;
    public PacketFXWispZap(int sourceEntityId, int targetEntityId) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
    }
    public static void encode(PacketFXWispZap packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sourceEntityId);
        buf.writeInt(packet.targetEntityId);
    }
    public static PacketFXWispZap decode(FriendlyByteBuf buf) {
        return new PacketFXWispZap(buf.readInt(), buf.readInt());
    }
    public static void handle(PacketFXWispZap packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXWispZap packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        Entity source = getEntityById(packet.sourceEntityId, mc);
        Entity target = getEntityById(packet.targetEntityId, mc);
        if (source == null || target == null) return;
        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        if (source instanceof EntityWisp wisp) {
            Aspect aspect = wisp.getAspect();
            if (aspect != null) {
                Color c = new Color(aspect.getColor());
                r = c.getRed() / 255.0f;
                g = c.getGreen() / 255.0f;
                b = c.getBlue() / 255.0f;
            }
        }
        FXDispatcher.INSTANCE.arcBolt(
                source.getX(), source.getY(), source.getZ(),
                target.getX(), target.getY(), target.getZ(),
                r, g, b, 0.6f);
    }
    @OnlyIn(Dist.CLIENT)
    private static Entity getEntityById(int entityId, Minecraft mc) {
        if (mc.player != null && entityId == mc.player.getId()) {
            return mc.player;
        }
        return mc.level != null ? mc.level.getEntity(entityId) : null;
    }
}
