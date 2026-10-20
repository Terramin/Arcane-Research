package arcaneresearch.common.lib.network.wand;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.items.wands.WandManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/wand/PacketWandFocusChangeToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with StreamCodec.composite (wire order identical to old encode/decode:
// legacy leading int 0, playerId, focus).
// Payload id "arcaneresearch:wand_focus_change_to_server".
public class PacketWandFocusChangeToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketWandFocusChangeToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_focus_change_to_server"));

    public static final StreamCodec<ByteBuf, PacketWandFocusChangeToServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> 0,
            ByteBufCodecs.INT, p -> p.playerId,
            ByteBufCodecs.STRING_UTF8, p -> p.focus,
            (zero, playerId, focus) -> {
                PacketWandFocusChangeToServer msg = new PacketWandFocusChangeToServer();
                msg.playerId = playerId;
                msg.focus = focus;
                return msg;
            });

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketWandFocusChangeToServer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            // playerId 0 = не указан (старые вызовы без игрока): верим сендеру.
            if (player != null && msg.playerId != 0 && player.getId() != msg.playerId) {
                player = null;
                net.minecraft.world.level.Level level = ctx.player().level();
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
    }
}
