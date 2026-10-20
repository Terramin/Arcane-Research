package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.common.items.casters.CasterManager;
import arcaneresearch.common.items.tools.ItemElementalShovel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketItemKeyToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:item_key_to_server".
public class PacketItemKeyToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketItemKeyToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item_key_to_server"));

    public static final StreamCodec<ByteBuf, PacketItemKeyToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketItemKeyToServer packet) {
            buf.writeByte(packet.key);
            buf.writeByte(packet.modifier);
        }

        @Override
        public PacketItemKeyToServer decode(ByteBuf buf) {
            return new PacketItemKeyToServer(buf.readByte(), buf.readByte());
        }
    };

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketItemKeyToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) {
                return;
            }

            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

            boolean handled = false;

            if (!mainHand.isEmpty()) {

                if (packet.key == 1 && mainHand.getItem() instanceof ICaster) {
                    toggleMisc(mainHand, player, packet.modifier);
                    handled = true;
                }

                if (packet.key == 1 && mainHand.getItem() instanceof ItemElementalShovel) {
                    byte orientation = ItemElementalShovel.getOrientation(mainHand);
                    ItemElementalShovel.setOrientation(mainHand, (byte) (orientation + 1));
                    handled = true;
                }

            }

            if (!handled && !offHand.isEmpty()) {
                if (packet.key == 1 && offHand.getItem() instanceof ICaster) {
                    toggleMisc(offHand, player, packet.modifier);
                }
            }
        });
    }

    private static void toggleMisc(ItemStack casterStack, ServerPlayer player, int modifier) {
        CasterManager.toggleMisc(casterStack, player.level(), player, modifier);
    }
}
