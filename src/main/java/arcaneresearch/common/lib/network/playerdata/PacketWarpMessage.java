package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.init.ModSounds;
import java.util.function.Supplier;
public class PacketWarpMessage {
    private int change;
    private byte type;
    public PacketWarpMessage() {
        this.change = 0;
        this.type = 0;
    }
    public PacketWarpMessage(Player player, byte type, int change) {
        this.change = change;
        this.type = type;
    }
    public static void encode(PacketWarpMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.change);
        buf.writeByte(msg.type);
    }
    public static PacketWarpMessage decode(FriendlyByteBuf buf) {
        PacketWarpMessage msg = new PacketWarpMessage();
        msg.change = buf.readInt();
        msg.type = buf.readByte();
        return msg;
    }
    public static void handle(PacketWarpMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        if (msg.change != 0) {
            ctx.enqueueWork(() -> handleOnClient(msg));
        }
        ctx.setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketWarpMessage msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        String textKey;
        boolean playSound = msg.change > 0;
        switch (msg.type) {
            case 0 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarp";
                    Minecraft.getInstance().level.playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            ModSounds.WHISPERS.get(), SoundSource.PLAYERS,
                            0.5f, 1.0f, false);
                } else {
                    textKey = "tc.removewarp";
                    playSound = false;
                }
            }
            case 1 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarpsticky";
                    Minecraft.getInstance().level.playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            ModSounds.WHISPERS.get(), SoundSource.PLAYERS,
                            0.5f, 1.0f, false);
                } else {
                    textKey = "tc.removewarpsticky";
                    playSound = false;
                }
                player.displayClientMessage(Component.translatable(textKey), true);
            }
            case 2 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarptemp";
                } else {
                    textKey = "tc.removewarptemp";
                }
                player.displayClientMessage(Component.translatable(textKey), true);
            }
            default -> {
                return;
            }
        }
    }
}
