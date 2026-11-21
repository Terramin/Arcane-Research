package arcaneresearch.common.lib.network.wand;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.api.wands.IFocusHostWand;
import arcaneresearch.common.items.wands.FocusManager;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketWandFocusChangeToServer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.NETWORK);
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

    private static boolean isSceptre(ItemStack stack) {
        return stack.getItem() instanceof IFocusHostWand wand && wand.isSceptre(stack);
    }

    public static void handle(PacketWandFocusChangeToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && msg.playerId != 0 && player.getId() != msg.playerId) {
                player = null;
                Level level = Objects.requireNonNull(ctx.get().getSender()).level();
                if (level.getEntity(msg.playerId) instanceof ServerPlayer sp2) {
                    player = sp2;
                }
            }
            if (player != null) {
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty() || !(held.getItem() instanceof IFocusHost) || isSceptre(held)) {
                    ItemStack off = player.getOffhandItem();
                    if (!off.isEmpty() && off.getItem() instanceof IFocusHost && !isSceptre(off)) {
                        held = off;
                    } else {
                        held = ItemStack.EMPTY;
                    }
                }
                if (!held.isEmpty() && held.getItem() instanceof IFocusHost) {
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
