package arcaneresearch.common.lib.network.misc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.menu.LogisticsMenu;
import java.util.function.Supplier;
public class PacketMiscStringToServer {
    private final int messageId;
    private final String text;
    public PacketMiscStringToServer(int messageId, String text) {
        this.messageId = messageId;
        this.text = text != null ? text : "";
    }
    public static void encode(PacketMiscStringToServer packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.messageId);
        buf.writeUtf(packet.text, 256);
    }
    public static PacketMiscStringToServer decode(FriendlyByteBuf buf) {
        int messageId = buf.readInt();
        String text = buf.readUtf(256);
        return new PacketMiscStringToServer(messageId, text);
    }
    public static void handle(PacketMiscStringToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            switch (packet.messageId) {
                case 0 -> {
                    if (player.containerMenu instanceof LogisticsMenu logisticsMenu) {
                        logisticsMenu.setSearchText(packet.text);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
