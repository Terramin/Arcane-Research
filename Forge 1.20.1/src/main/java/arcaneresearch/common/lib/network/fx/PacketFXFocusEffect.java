package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.IFocusElement;
import java.util.function.Supplier;
public class PacketFXFocusEffect {
    private final float x, y, z;
    private final float motionX, motionY, motionZ;
    private final String parts;
    public PacketFXFocusEffect(float x, float y, float z, float motionX, float motionY, float motionZ, String[] parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("%");
            sb.append(parts[i]);
        }
        this.parts = sb.toString();
    }
    private PacketFXFocusEffect(float x, float y, float z, float motionX, float motionY, float motionZ, String parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.parts = parts;
    }
    public static void encode(PacketFXFocusEffect packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.x);
        buf.writeFloat(packet.y);
        buf.writeFloat(packet.z);
        buf.writeFloat(packet.motionX);
        buf.writeFloat(packet.motionY);
        buf.writeFloat(packet.motionZ);
        buf.writeUtf(packet.parts);
    }
    public static PacketFXFocusEffect decode(FriendlyByteBuf buf) {
        return new PacketFXFocusEffect(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readUtf(32767)
        );
    }
    public static void handle(PacketFXFocusEffect packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXFocusEffect packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        String[] partKeys = packet.parts.split("%");
        int amount = Math.max(1, 10 / partKeys.length);
        for (String key : partKeys) {
            IFocusElement element = FocusEngine.getElement(key);
            if (element instanceof FocusEffect effect) {
                for (int i = 0; i < amount; i++) {
                    double mx = packet.motionX + level.random.nextGaussian() / 20.0;
                    double my = packet.motionY + level.random.nextGaussian() / 20.0;
                    double mz = packet.motionZ + level.random.nextGaussian() / 20.0;
                    effect.renderParticleFX(level, packet.x, packet.y, packet.z, mx, my, mz);
                }
            }
        }
    }
}
