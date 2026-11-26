package arcaneresearch.common.lib.network.wards;

import arcaneresearch.common.wards.WardControlState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sends the client's Ctrl key state to the server for the ward removal shortcut. */
public class PacketWardControlToServer {
    private final boolean controlDown;

    public PacketWardControlToServer(boolean controlDown) {
        this.controlDown = controlDown;
    }

    private PacketWardControlToServer(FriendlyByteBuf buf) {
        this.controlDown = buf.readBoolean();
    }

    public static void encode(PacketWardControlToServer packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.controlDown);
    }

    public static PacketWardControlToServer decode(FriendlyByteBuf buf) {
        return new PacketWardControlToServer(buf);
    }

    public static void handle(PacketWardControlToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                WardControlState.set(player, packet.controlDown);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
