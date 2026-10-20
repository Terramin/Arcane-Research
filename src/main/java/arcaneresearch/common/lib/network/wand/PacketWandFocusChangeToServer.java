package arcaneresearch.common.lib.network.wand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.items.wands.WandManager;
import java.util.function.Supplier;
public class PacketWandFocusChangeToServer {
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
    public static void handle(PacketWandFocusChangeToServer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && msg.playerId != 0 && player.getId() != msg.playerId) {
                player = null;
                net.minecraft.world.level.Level level = ctx.get().getSender().level();
                if (level != null && level.getEntity(msg.playerId) instanceof ServerPlayer sp2) {
                    player = sp2;
                }
            }
            if (player != null) {
                ItemStack held = player.getMainHandItem();
                if (held == null || held.isEmpty() || !(held.getItem() instanceof IWand)
                        || ((IWand) held.getItem()).isSceptre(held)) {
                    ItemStack off = player.getOffhandItem();
                    if (off != null && !off.isEmpty() && off.getItem() instanceof IWand
                            && !((IWand) off.getItem()).isSceptre(off)) {
                        held = off;
                    } else {
                        held = ItemStack.EMPTY;
                    }
                }
                if (held != null && !held.isEmpty() && held.getItem() instanceof IWand) {
                    WandManager.changeFocus(held, player.level(), player, msg.focus);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
