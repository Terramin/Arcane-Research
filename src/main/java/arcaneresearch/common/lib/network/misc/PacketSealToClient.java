package arcaneresearch.common.lib.network.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import java.util.function.Supplier;
public class PacketSealToClient {
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
    public static void encode(PacketSealToClient msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.pos.asLong());
        buf.writeByte(msg.face.ordinal());
        buf.writeByte(msg.priority);
        buf.writeByte(msg.color);
        buf.writeBoolean(msg.locked);
        buf.writeBoolean(msg.redstone);
        buf.writeUtf(msg.owner);
        buf.writeUtf(msg.type);
        buf.writeBoolean(msg.blacklist);
        buf.writeByte(msg.filterSize);
        for (int i = 0; i < msg.filterSize; i++) {
            buf.writeItem(msg.filter.get(i));
            buf.writeShort(msg.filterStackSize.get(i));
        }
        if (msg.area != 0L) {
            buf.writeBoolean(true);
            buf.writeLong(msg.area);
        } else {
            buf.writeBoolean(false);
        }
        if (msg.props != null && msg.props.length > 0) {
            buf.writeByte(msg.props.length);
            for (boolean prop : msg.props) {
                buf.writeBoolean(prop);
            }
        } else {
            buf.writeByte(0);
        }
    }
    public static PacketSealToClient decode(FriendlyByteBuf buf) {
        PacketSealToClient msg = new PacketSealToClient();
        msg.pos = BlockPos.of(buf.readLong());
        msg.face = Direction.values()[buf.readByte()];
        msg.priority = buf.readByte();
        msg.color = buf.readByte();
        msg.locked = buf.readBoolean();
        msg.redstone = buf.readBoolean();
        msg.owner = buf.readUtf();
        msg.type = buf.readUtf();
        msg.blacklist = buf.readBoolean();
        msg.filterSize = buf.readByte();
        msg.filter = NonNullList.withSize(msg.filterSize, ItemStack.EMPTY);
        msg.filterStackSize = NonNullList.withSize(msg.filterSize, 0);
        for (int i = 0; i < msg.filterSize; i++) {
            msg.filter.set(i, buf.readItem());
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
    public static void handle(PacketSealToClient msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            handleOnClient(msg);
        });
        ctx.setPacketHandled(true);
    }
    private static void handleOnClient(PacketSealToClient msg) {
        try {
            if (msg.type.equals("REMOVE")) {
                SealHandler.removeSealEntity(
                        Arcaneresearch.getClientWorld(),
                        new SealPos(msg.pos, msg.face),
                        true
                );
            } else {
                ISeal template = SealHandler.getSeal(msg.type);
                if (template == null) {
                    Arcaneresearch.LOGGER.warn("Unknown seal type in packet: {}", msg.type);
                    return;
                }
                net.minecraft.world.level.Level clientLevel = Arcaneresearch.getClientWorld();
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
