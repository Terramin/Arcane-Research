package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PacketFXShield {
    private final int sourceEntityId;
    private final int targetEntityId;
    public PacketFXShield(int sourceEntityId, int targetEntityId) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
    }
    public static void encode(PacketFXShield packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sourceEntityId);
        buf.writeInt(packet.targetEntityId);
    }
    public static PacketFXShield decode(FriendlyByteBuf buf) {
        return new PacketFXShield(buf.readInt(), buf.readInt());
    }
    public static void handle(PacketFXShield packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXShield packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        Entity source = level.getEntity(packet.sourceEntityId);
        if (source == null) return;
        float pitch = 0.0f;
        float yaw = 0.0f;
        if (packet.targetEntityId >= 0) {
            Entity target = level.getEntity(packet.targetEntityId);
            if (target != null) {
                double dx = source.getX() - target.getX();
                AABB sourceBB = source.getBoundingBox();
                AABB targetBB = target.getBoundingBox();
                double dy = (sourceBB.minY + sourceBB.maxY) / 2.0 - (targetBB.minY + targetBB.maxY) / 2.0;
                double dz = source.getZ() - target.getZ();
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
            } else {
                pitch = 90.0f;
                yaw = 0.0f;
            }
            spawnShieldRunes(level, source, yaw, pitch);
        } else if (packet.targetEntityId == -1) {
            spawnShieldRunes(level, source, 0.0f, 90.0f);
            spawnShieldRunes(level, source, 0.0f, 270.0f);
        } else if (packet.targetEntityId == -2) {
            spawnShieldRunes(level, source, 0.0f, 270.0f);
        } else if (packet.targetEntityId == -3) {
            spawnShieldRunes(level, source, 0.0f, 90.0f);
        }
    }
    @OnlyIn(Dist.CLIENT)
    private static void spawnShieldRunes(Level level, Entity entity, float yaw, float pitch) {
        if (!(level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel)) {
            return;
        }
        arcaneresearch.client.fx.other.FXShieldRunes fb =
                new arcaneresearch.client.fx.other.FXShieldRunes(
                        clientLevel, entity.getX(), entity.getY(), entity.getZ(), entity, 8, yaw, pitch);
        Minecraft.getInstance().particleEngine.add(fb);
    }
}
