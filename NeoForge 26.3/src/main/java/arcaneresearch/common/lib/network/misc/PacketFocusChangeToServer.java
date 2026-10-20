package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.common.items.casters.CasterManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketFocusChangeToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:focus_change_to_server".
public class PacketFocusChangeToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFocusChangeToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_change_to_server"));

    public static final StreamCodec<ByteBuf, PacketFocusChangeToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFocusChangeToServer packet) {
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.focus);
        }

        @Override
        public PacketFocusChangeToServer decode(ByteBuf buf) {
            return new PacketFocusChangeToServer(ByteBufCodecs.STRING_UTF8.decode(buf));
        }
    };

    private final String focus;

    public PacketFocusChangeToServer(String focus) {
        this.focus = focus;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusChangeToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) {
                return;
            }

            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof ICaster) {
                changeFocus(mainHand, player, packet.focus);
                return;
            }

            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!offHand.isEmpty() && offHand.getItem() instanceof ICaster) {
                changeFocus(offHand, player, packet.focus);
            }
        });
    }

    private static void changeFocus(ItemStack casterStack, ServerPlayer player, String focusKey) {
        if ("REMOVE".equals(focusKey)) {

            CasterManager.removeFocus(casterStack, player);
        } else {
            CasterManager.changeFocus(casterStack, player.level(), player, focusKey);
        }
    }
}
