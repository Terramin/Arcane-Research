package arcaneresearch.common.lib.network.wand;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.api.wands.IFocusHostWand;
import arcaneresearch.common.items.wands.FocusManager;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;

import java.util.function.Supplier;

public class PacketWandFocusChangeToServer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.NETWORK);
    /** Kept in the wire format for old clients; the server never trusts it. */
    private int playerId;
    private String focus;

    public PacketWandFocusChangeToServer() {
        this.focus = "";
    }

    public PacketWandFocusChangeToServer(String focus) {
        this.focus = focus == null ? "" : focus;
    }

    public PacketWandFocusChangeToServer(net.minecraft.world.entity.player.Player player, String focus) {
        this.playerId = player.getId();
        this.focus = focus == null ? "" : focus;
    }

    public static void encode(PacketWandFocusChangeToServer msg, FriendlyByteBuf buf) {
        buf.writeInt(0);
        buf.writeInt(msg.playerId);
        buf.writeUtf(msg.focus);
    }

    public static PacketWandFocusChangeToServer decode(FriendlyByteBuf buf) {
        buf.readInt();
        PacketWandFocusChangeToServer msg = new PacketWandFocusChangeToServer();
        msg.playerId = buf.readInt();
        msg.focus = buf.readUtf();
        return msg;
    }

    /** Sceptres are crafting-only wands: research states that foci cannot be fitted to them. */
    private static boolean acceptsFocus(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IFocusHost)) {
            return false;
        }
        return !(stack.getItem() instanceof IFocusHostWand wand && wand.isSceptre(stack));
    }

    public static void handle(PacketWandFocusChangeToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // The sender is the only authority here. Never look up another
            // player by a client-provided id: that allowed changing other
            // players' foci. The id field stays in the protocol for old
            // clients but is deliberately ignored.
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack held = player.getMainHandItem();
                if (!acceptsFocus(held)) {
                    held = player.getOffhandItem();
                    if (!acceptsFocus(held)) {
                        held = ItemStack.EMPTY;
                    }
                }
                if (!held.isEmpty()) {
                    LOGGER.debug("Focus change packet from {}: requested {}", player.getName().getString(), msg.focus);
                    FocusManager.changeFocus(held, player.level(), player, msg.focus);
                } else {
                    LOGGER.once("wand.focus.no.host", LogLevel.DEBUG,
                            "Focus change packet from {} ignored: no wand/caster in hand",
                            player.getName().getString());
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
