package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.ISealConfigFilter;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.common.golems.seals.SealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketSealFilterToClient.java (PLAY_TO_CLIENT).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Codec buffer is RegistryFriendlyByteBuf (REQUIRED: ItemStack.OPTIONAL_STREAM_CODEC
// is StreamCodec<RegistryFriendlyByteBuf, ItemStack>; registrar accepts
// StreamCodec<? super RegistryFriendlyByteBuf, T>). Wire format is the NeoForge-native
// ItemStack codec on both ends (same mod version), replacing FriendlyByteBuf.writeItem/readItem.
// Payload id "arcaneresearch:seal_filter_to_client".
public class PacketSealFilterToClient implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSealFilterToClient> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "seal_filter_to_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSealFilterToClient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PacketSealFilterToClient packet) {
            buf.writeLong(packet.pos.asLong());
            buf.writeByte(packet.face.ordinal());
            buf.writeByte(packet.filterSize);

            for (int i = 0; i < packet.filterSize; i++) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.filter.get(i));
                buf.writeShort(packet.filterStackSizes.get(i));
            }
        }

        @Override
        public PacketSealFilterToClient decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = BlockPos.of(buf.readLong());
            Direction face = Direction.values()[buf.readByte()];
            byte filterSize = buf.readByte();

            NonNullList<ItemStack> filter = NonNullList.withSize(filterSize, ItemStack.EMPTY);
            NonNullList<Integer> filterStackSizes = NonNullList.withSize(filterSize, 0);

            for (int i = 0; i < filterSize; i++) {
                filter.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                filterStackSizes.set(i, (int) buf.readShort());
            }

            return new PacketSealFilterToClient(pos, face, filterSize, filter, filterStackSizes);
        }
    };

    private final BlockPos pos;
    private final Direction face;
    private final byte filterSize;
    private final NonNullList<ItemStack> filter;
    private final NonNullList<Integer> filterStackSizes;

    public PacketSealFilterToClient(ISealEntity sealEntity) {
        this.pos = sealEntity.getSealPos().pos;
        this.face = sealEntity.getSealPos().face;

        if (sealEntity.getSeal() instanceof ISealConfigFilter configFilter) {
            this.filterSize = (byte) configFilter.getFilterSize();
            this.filter = configFilter.getInv();
            this.filterStackSizes = configFilter.getSizes();
        } else {
            this.filterSize = 0;
            this.filter = NonNullList.create();
            this.filterStackSizes = NonNullList.create();
        }
    }

    private PacketSealFilterToClient(BlockPos pos, Direction face, byte filterSize,
    NonNullList<ItemStack> filter, NonNullList<Integer> filterStackSizes) {
        this.pos = pos;
        this.face = face;
        this.filterSize = filterSize;
        this.filter = filter;
        this.filterStackSizes = filterStackSizes;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSealFilterToClient packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(packet, ctx.player()));
    }

    private static void handleClient(PacketSealFilterToClient packet, Player player) {
        if (player == null) return;
        Level level = player.level();
        if (level == null) return;

        try {
            ISealEntity seal = SealHandler.getSealEntity(
            level.dimension(),
            new SealPos(packet.pos, packet.face)
            );

            if (seal != null && seal.getSeal() instanceof ISealConfigFilter configFilter) {
                for (int i = 0; i < packet.filterSize; i++) {
                    configFilter.setFilterSlot(i, packet.filter.get(i));
                    configFilter.setFilterSlotSize(i, packet.filterStackSizes.get(i));
                }
            }
        } catch (Exception e) {

        }
    }
}