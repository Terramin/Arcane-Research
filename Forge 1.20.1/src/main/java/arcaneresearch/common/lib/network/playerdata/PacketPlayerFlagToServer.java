package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PacketPlayerFlagToServer {
    public static final byte FLAG_RESET_FALL_DISTANCE = 1;
    private final byte flag;
    public PacketPlayerFlagToServer(int flag) {
        this.flag = (byte) flag;
    }
    private PacketPlayerFlagToServer(byte flag) {
        this.flag = flag;
    }
    public static void encode(PacketPlayerFlagToServer packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.flag);
    }
    public static PacketPlayerFlagToServer decode(FriendlyByteBuf buf) {
        return new PacketPlayerFlagToServer(buf.readByte());
    }
    public static void handle(PacketPlayerFlagToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            switch (packet.flag) {
                case FLAG_RESET_FALL_DISTANCE -> {
                    player.fallDistance = 0.0f;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
