package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketItemToClientContainer.java (PLAY_TO_CLIENT).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Codec buffer is RegistryFriendlyByteBuf (REQUIRED: ItemStack.OPTIONAL_STREAM_CODEC
// is StreamCodec<RegistryFriendlyByteBuf, ItemStack>; registrar accepts
// StreamCodec<? super RegistryFriendlyByteBuf, T>). Wire format is the NeoForge-native
// ItemStack codec on both ends (same mod version), replacing FriendlyByteBuf.writeItem/readItem.
// Payload id "arcaneresearch:item_to_client_container".
public class PacketItemToClientContainer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketItemToClientContainer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item_to_client_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketItemToClientContainer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PacketItemToClientContainer packet) {
            buf.writeInt(packet.windowId);
            buf.writeInt(packet.slot);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.item == null ? ItemStack.EMPTY : packet.item);
        }

        @Override
        public PacketItemToClientContainer decode(RegistryFriendlyByteBuf buf) {
            PacketItemToClientContainer msg = new PacketItemToClientContainer();
            msg.windowId = buf.readInt();
            msg.slot = buf.readInt();
            msg.item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return msg;
        }
    };

    private int windowId;
    private int slot;
    private ItemStack item;

    public PacketItemToClientContainer() {
    }

    public PacketItemToClientContainer(int windowIdIn, int slotIn, ItemStack itemIn) {
        windowId = windowIdIn;
        slot = slotIn;
        item = itemIn == null ? ItemStack.EMPTY : itemIn.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

public static void handle(PacketItemToClientContainer msg, IPayloadContext ctx) {
ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
}

    private static void handleOnClient(PacketItemToClientContainer message, Player player) {
        try {
            if (player != null && player.containerMenu != null
            && player.containerMenu.containerId == message.windowId) {
                player.containerMenu.setItem(message.slot, 0, message.item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}