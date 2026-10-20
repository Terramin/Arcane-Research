package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketSealToClient.java (PLAY_TO_CLIENT).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Codec buffer is RegistryFriendlyByteBuf (REQUIRED: ItemStack.OPTIONAL_STREAM_CODEC
// is StreamCodec<RegistryFriendlyByteBuf, ItemStack>; registrar accepts
// StreamCodec<? super RegistryFriendlyByteBuf, T>). Wire format is the NeoForge-native
// ItemStack codec on both ends (same mod version), replacing FriendlyByteBuf.writeItem/readItem.
// Payload id "arcaneresearch:seal_to_client".
public class PacketSealToClient implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSealToClient> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "seal_to_client"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSealToClient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PacketSealToClient packet) {
            buf.writeLong(packet.pos.asLong());
            buf.writeByte(packet.face.ordinal());
            buf.writeByte(packet.priority);
            buf.writeByte(packet.color);
            buf.writeBoolean(packet.locked);
            buf.writeBoolean(packet.redstone);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.owner);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.type);
            buf.writeBoolean(packet.blacklist);
            buf.writeByte(packet.filterSize);

            for (int i = 0; i < packet.filterSize; i++) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.filter.get(i));
                buf.writeShort(packet.filterStackSize.get(i));
            }

            if (packet.area != 0L) {
                buf.writeBoolean(true);
                buf.writeLong(packet.area);
            } else {
                buf.writeBoolean(false);
            }

            if (packet.props != null && packet.props.length > 0) {
                buf.writeByte(packet.props.length);
                for (boolean prop : packet.props) {
                    buf.writeBoolean(prop);
                }
            } else {
                buf.writeByte(0);
            }
        }

        @Override
        public PacketSealToClient decode(RegistryFriendlyByteBuf buf) {
            PacketSealToClient msg = new PacketSealToClient();

            msg.pos = BlockPos.of(buf.readLong());
            msg.face = Direction.values()[buf.readByte()];
            msg.priority = buf.readByte();
            msg.color = buf.readByte();
            msg.locked = buf.readBoolean();
            msg.redstone = buf.readBoolean();
            msg.owner = ByteBufCodecs.STRING_UTF8.decode(buf);
            msg.type = ByteBufCodecs.STRING_UTF8.decode(buf);
            msg.blacklist = buf.readBoolean();
            msg.filterSize = buf.readByte();

            msg.filter = NonNullList.withSize(msg.filterSize, ItemStack.EMPTY);
            msg.filterStackSize = NonNullList.withSize(msg.filterSize, 0);
            for (int i = 0; i < msg.filterSize; i++) {
                msg.filter.set(i, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
                msg.filterStackSize.set(i, (int) buf.readShort());
            }

            boolean hasArea = buf.readBoolean();
            if (hasArea) {
                msg.area = buf.readLong();
            }

            int propsLength = buf.readByte();
            if (propsLength > 0) {
                msg.props = new boolean[propsLength];
                for (int i = 0; i < propsLength; i++) {
                    msg.props[i] = buf.readBoolean();
                }
            }

            return msg;
        }
    };

    private BlockPos pos;
    private Direction face;
    private String type;
    private long area;
    private boolean[] props;
    private boolean blacklist;
    private byte filterSize;
    private NonNullList<ItemStack> filter;
    private NonNullList<Integer> filterStackSize;
    private byte priority;
    private byte color;
    private boolean locked;
    private boolean redstone;
    private String owner;

    public PacketSealToClient() {
        props = null;
    }

    public PacketSealToClient(ISealEntity se) {
        props = null;
        pos = se.getSealPos().pos;
        face = se.getSealPos().face;
        type = (se.getSeal() == null) ? "REMOVE" : se.getSeal().getKey();

        if (se.getSeal() != null && se.getSeal() instanceof ISealConfigArea) {
            area = se.getArea().asLong();
        }

        if (se.getSeal() != null && se.getSeal() instanceof ISealConfigToggles configToggles) {
            ISealConfigToggles.SealToggle[] toggles = configToggles.getToggles();
            props = new boolean[toggles.length];
            for (int i = 0; i < toggles.length; i++) {
                props[i] = toggles[i].getValue();
            }
        }

        if (se.getSeal() != null && se.getSeal() instanceof ISealConfigFilter configFilter) {
            blacklist = configFilter.isBlacklist();
            filterSize = (byte) configFilter.getFilterSize();
            filter = configFilter.getInv();
            filterStackSize = configFilter.getSizes();
        } else {
            filterSize = 0;
            filter = NonNullList.create();
            filterStackSize = NonNullList.create();
        }

        priority = se.getPriority();
        color = se.getColor();
        locked = se.isLocked();
        redstone = se.isRedstoneSensitive();
        owner = se.getOwner();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSealToClient msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg));
    }

    private static void handleOnClient(PacketSealToClient msg) {
        try {
            if (msg.type.equals("REMOVE")) {

                SealHandler.removeSealEntity(
                arcaneresearch.proxies.Proxies.get().getClientLevel(),
                new SealPos(msg.pos, msg.face),
                true
                );
            } else {

                ISeal template = SealHandler.getSeal(msg.type);
                if (template == null) {
                    Arcaneresearch.LOGGER.warn("Unknown seal type in packet: {}", msg.type);
                    return;
                }

                // NEOFORGE_PORT (26.3): Forge parity — drop the update when no client
                // level is loaded, and update an existing seal of the same type in place
                // (applyData) instead of replacing it.
                net.minecraft.world.level.Level clientLevel = arcaneresearch.proxies.Proxies.get().getClientLevel();
                if (clientLevel == null) {
                    Arcaneresearch.LOGGER.warn("Seal packet arrived with no client world loaded, dropping update at {}", msg.pos);
                    return;
                }
                SealPos sealPos = new SealPos(msg.pos, msg.face);
                ISealEntity existing = SealHandler.getSealEntity(clientLevel.dimension(), sealPos);
                if (existing instanceof SealEntity existingEntity
                        && existing.getSeal() != null
                        && msg.type.equals(existing.getSeal().getKey())) {
                    applyData(existing.getSeal(), existingEntity, msg);
                    return;
                }

                ISeal seal = template.getClass().getDeclaredConstructor().newInstance();
                SealEntity sealEntity = new SealEntity(
                clientLevel,
                sealPos,
                seal
                );

                applyData(seal, sealEntity, msg);

                if (!SealHandler.addSealEntity(clientLevel, sealEntity)) {
                    SealHandler.removeSealEntity(clientLevel,
                    sealPos, true);
                    SealHandler.addSealEntity(clientLevel, sealEntity);
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("Error handling seal packet at {}", msg.pos, e);
        }
    }

    private static void applyData(ISeal seal, SealEntity sealEntity, PacketSealToClient msg) {
        if (msg.area != 0L) {
            sealEntity.setArea(BlockPos.of(msg.area));
        }

        if (msg.props != null && seal instanceof ISealConfigToggles configToggles) {
            for (int i = 0; i < msg.props.length; i++) {
                configToggles.setToggle(i, msg.props[i]);
            }
        }

        if (seal instanceof ISealConfigFilter configFilter) {
            configFilter.setBlacklist(msg.blacklist);
            for (int i = 0; i < msg.filterSize; i++) {
                configFilter.setFilterSlot(i, msg.filter.get(i));
                configFilter.setFilterSlotSize(i, msg.filterStackSize.get(i));
            }
        }

        sealEntity.setPriority(msg.priority);
        sealEntity.setColor(msg.color);
        sealEntity.setLocked(msg.locked);
        sealEntity.setRedstoneSensitive(msg.redstone);
        sealEntity.setOwner(msg.owner);
    }
}
