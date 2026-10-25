package arcaneresearch.common.lib.network.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.golems.seals.ISealConfigFilter;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.common.golems.seals.SealHandler;
import java.util.function.Supplier;
public class PacketSealFilterToClient {
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
    public static void encode(PacketSealFilterToClient packet, FriendlyByteBuf buf) {
        buf.writeLong(packet.pos.asLong());
        buf.writeByte(packet.face.ordinal());
        buf.writeByte(packet.filterSize);
        for (int i = 0; i < packet.filterSize; i++) {
            buf.writeItem(packet.filter.get(i));
            buf.writeShort(packet.filterStackSizes.get(i));
        }
    }
    public static PacketSealFilterToClient decode(FriendlyByteBuf buf) {
        BlockPos pos = BlockPos.of(buf.readLong());
        Direction face = Direction.values()[buf.readByte()];
        byte filterSize = buf.readByte();
        NonNullList<ItemStack> filter = NonNullList.withSize(filterSize, ItemStack.EMPTY);
        NonNullList<Integer> filterStackSizes = NonNullList.withSize(filterSize, 0);
        for (int i = 0; i < filterSize; i++) {
            filter.set(i, buf.readItem());
            filterStackSizes.set(i, (int) buf.readShort());
        }
        return new PacketSealFilterToClient(pos, face, filterSize, filter, filterStackSizes);
    }
    public static void handle(PacketSealFilterToClient packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketSealFilterToClient packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
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
            arcaneresearch.Arcaneresearch.LOGGER.warn("Failed to apply seal filter packet at {}: {}", packet.pos, e.toString());
        }
    }
}
