package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.GolemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketLogisticsRequestToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Codec buffer is RegistryFriendlyByteBuf (REQUIRED: ItemStack.OPTIONAL_STREAM_CODEC
// is StreamCodec<RegistryFriendlyByteBuf, ItemStack>; registrar accepts
// StreamCodec<? super RegistryFriendlyByteBuf, T>). Wire format is the NeoForge-native
// ItemStack codec on both ends (same mod version), replacing FriendlyByteBuf.writeItem/readItem.
// Payload id "arcaneresearch:logistics_request_to_server".
public class PacketLogisticsRequestToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketLogisticsRequestToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "logistics_request_to_server"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketLogisticsRequestToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PacketLogisticsRequestToServer packet) {
            if (packet.pos == null || packet.side == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeLong(packet.pos.asLong());
                buf.writeByte(packet.side.ordinal());
            }
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.stack);
            buf.writeInt(packet.stackSize);
        }

        @Override
        public PacketLogisticsRequestToServer decode(RegistryFriendlyByteBuf buf) {
            boolean hasPos = buf.readBoolean();
            BlockPos pos = null;
            Direction side = null;
            if (hasPos) {
                pos = BlockPos.of(buf.readLong());
                side = Direction.values()[buf.readByte()];
            }
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            int stackSize = buf.readInt();

            if (pos != null) {
                return new PacketLogisticsRequestToServer(pos, side, stack, stackSize);
            } else {
                return new PacketLogisticsRequestToServer(stack, stackSize);
            }
        }
    };

    private final BlockPos pos;
    private final Direction side;
    private final ItemStack stack;
    private final int stackSize;

    public PacketLogisticsRequestToServer(ItemStack stack, int size) {
        this.pos = null;
        this.side = null;
        this.stack = stack;
        this.stackSize = size;
    }

    public PacketLogisticsRequestToServer(BlockPos pos, Direction side, ItemStack stack, int size) {
        this.pos = pos;
        this.side = side;
        this.stack = stack;
        this.stackSize = size;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketLogisticsRequestToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            Level level = player.level();
            int remainingSize = packet.stackSize;
            int uniqueId = 0;

            while (remainingSize > 0) {
                ItemStack requestStack = packet.stack.copy();
                int count = Math.min(remainingSize, requestStack.getMaxStackSize());
                requestStack.setCount(count);
                remainingSize -= count;

                if (packet.pos != null) {

                    GolemHelper.requestProvisioning(level, packet.pos, packet.side, requestStack, uniqueId);
                } else {

                    GolemHelper.requestProvisioning(level, player, requestStack, uniqueId);
                }
                uniqueId++;
            }
        });
    }
}
