package arcaneresearch.common.lib.network.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.init.ModSounds;
import java.util.function.Supplier;
public class PacketMiscEvent {
    public static final byte WARP_EVENT = 0;
    public static final byte MIST_EVENT = 1;
    public static final byte MIST_EVENT_SHORT = 2;
    private final byte type;
    private final int value;
    public PacketMiscEvent(byte type) {
        this.type = type;
        this.value = 0;
    }
    public PacketMiscEvent(byte type, int value) {
        this.type = type;
        this.value = value;
    }
    public static void encode(PacketMiscEvent packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.type);
        if (packet.value != 0) {
            buf.writeInt(packet.value);
        }
    }
    public static PacketMiscEvent decode(FriendlyByteBuf buf) {
        byte type = buf.readByte();
        int value = 0;
        if (buf.isReadable()) {
            value = buf.readInt();
        }
        return new PacketMiscEvent(type, value);
    }
    public static void handle(PacketMiscEvent packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketMiscEvent packet) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        switch (packet.type) {
            case WARP_EVENT -> {
                if (!ModConfig.CONFIG_GRAPHICS.nostress && ModSounds.HEARTBEAT.get() != null) {
                    mc.level.playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            ModSounds.HEARTBEAT.get(), SoundSource.AMBIENT,
                            1.0f, 1.0f, false
                    );
                }
            }
            case MIST_EVENT -> {
                arcaneresearch.client.lib.events.FogHandler.triggerLongFog();
            }
            case MIST_EVENT_SHORT -> {
                arcaneresearch.client.lib.events.FogHandler.triggerShortFog();
            }
        }
    }
}
