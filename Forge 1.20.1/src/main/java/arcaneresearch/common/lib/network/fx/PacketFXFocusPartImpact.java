package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.IFocusElement;
import java.util.function.Supplier;
public class PacketFXFocusPartImpact {
    private final float x, y, z;
    private final String parts;
    public PacketFXFocusPartImpact(double x, double y, double z, String[] parts) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("%");
            sb.append(parts[i]);
        }
        this.parts = sb.toString();
    }
    private PacketFXFocusPartImpact(float x, float y, float z, String parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.parts = parts;
    }
    public static void encode(PacketFXFocusPartImpact packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.x);
        buf.writeFloat(packet.y);
        buf.writeFloat(packet.z);
        buf.writeUtf(packet.parts);
    }
    public static PacketFXFocusPartImpact decode(FriendlyByteBuf buf) {
        return new PacketFXFocusPartImpact(
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readUtf(32767)
        );
    }
    public static void handle(PacketFXFocusPartImpact packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXFocusPartImpact packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        String[] partKeys = packet.parts.split("%");
        int amount = Math.max(1, 15 / partKeys.length);
        RandomSource rand = level.random;
        for (String key : partKeys) {
            IFocusElement element = FocusEngine.getElement(key);
            if (element instanceof FocusEffect effect) {
                for (int i = 0; i < amount; i++) {
                    double mx = rand.nextGaussian() * 0.15;
                    double my = rand.nextGaussian() * 0.15;
                    double mz = rand.nextGaussian() * 0.15;
                    effect.renderParticleFX(level, packet.x, packet.y, packet.z, mx, my, mz);
                }
            }
        }
    }
}
