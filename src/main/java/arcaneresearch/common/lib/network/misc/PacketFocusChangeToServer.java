package arcaneresearch.common.lib.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.common.items.wands.FocusManager;

import java.util.function.Supplier;

public class PacketFocusChangeToServer {
    private final String focus;

    public PacketFocusChangeToServer(String focus) {
        this.focus = focus;
    }

    public static void encode(PacketFocusChangeToServer packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.focus);
    }

    public static PacketFocusChangeToServer decode(FriendlyByteBuf buf) {
        return new PacketFocusChangeToServer(buf.readUtf(32767));
    }

    public static void handle(PacketFocusChangeToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof IFocusHost) {
                changeFocus(mainHand, player, packet.focus);
                return;
            }
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offHand.isEmpty() && offHand.getItem() instanceof IFocusHost) {
                changeFocus(offHand, player, packet.focus);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void changeFocus(ItemStack casterStack, ServerPlayer player, String focusKey) {
        if ("REMOVE".equals(focusKey)) {
            FocusManager.removeFocus(casterStack, player);
        } else {
            FocusManager.changeFocus(casterStack, player.level(), player, focusKey);
        }
    }
}
