package arcaneresearch.common.lib.network.misc;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.common.items.wands.FocusManager;
import arcaneresearch.common.items.tools.elemental.ElementalShovelItem;

import java.util.function.Supplier;

public class PacketItemKeyToServer {
    private final byte key;
    private final byte modifier;

    public PacketItemKeyToServer(int key) {
        this.key = (byte) key;
        this.modifier = 0;
    }

    public PacketItemKeyToServer(int key, int modifier) {
        this.key = (byte) key;
        this.modifier = (byte) modifier;
    }

    private PacketItemKeyToServer(byte key, byte modifier) {
        this.key = key;
        this.modifier = modifier;
    }

    public static void encode(PacketItemKeyToServer packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.key);
        buf.writeByte(packet.modifier);
    }

    public static PacketItemKeyToServer decode(FriendlyByteBuf buf) {
        return new PacketItemKeyToServer(buf.readByte(), buf.readByte());
    }

    public static void handle(PacketItemKeyToServer packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            boolean handled = false;
            if (!mainHand.isEmpty()) {
                if (packet.key == 1 && mainHand.getItem() instanceof IFocusHost) {
                    toggleMisc(mainHand, player, packet.modifier);
                    handled = true;
                }
                if (packet.key == 1 && mainHand.getItem() instanceof ElementalShovelItem) {
                    byte orientation = ElementalShovelItem.getOrientation(mainHand);
                    ElementalShovelItem.setOrientation(mainHand, (byte) (orientation + 1));
                    handled = true;
                }
            }
            if (!handled && !offHand.isEmpty()) {
                if (packet.key == 1 && offHand.getItem() instanceof IFocusHost) {
                    toggleMisc(offHand, player, packet.modifier);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void toggleMisc(ItemStack casterStack, ServerPlayer player, int modifier) {
        FocusManager.toggleMisc(casterStack, player.level(), player, modifier);
    }
}
