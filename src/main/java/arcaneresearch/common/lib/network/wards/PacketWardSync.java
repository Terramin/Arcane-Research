package arcaneresearch.common.lib.network.wards;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.wards.WardClientRegistry;
import arcaneresearch.common.wards.WardEntry;

import java.util.function.Supplier;

/**
 * Sends the warded-block registry to a client (full, on login/dimension change)
 * or broadcasts a single ward add/removal (delta).
 */
public class PacketWardSync {
    private final String dimension;
    private final boolean full;
    private final CompoundTag data;
    private final BlockPos pos;
    private final String ownerUuid;
    private final String ownerName;
    private final boolean added;

    public PacketWardSync(String dimension, CompoundTag data) {
        this(dimension, true, data == null ? new CompoundTag() : data, BlockPos.ZERO,
                "", "", false);
    }

    public PacketWardSync(String dimension, BlockPos pos, WardEntry entry, boolean added) {
        this(dimension, false, new CompoundTag(), pos == null ? BlockPos.ZERO : pos,
                entry == null ? "" : entry.getOwnerUuid(),
                entry == null ? "" : entry.getOwnerName(), added);
    }

    private PacketWardSync(String dimension, boolean full, CompoundTag data, BlockPos pos,
                           String ownerUuid, String ownerName, boolean added) {
        this.dimension = dimension == null ? "" : dimension;
        this.full = full;
        this.data = data == null ? new CompoundTag() : data;
        this.pos = pos == null ? BlockPos.ZERO : pos;
        this.ownerUuid = ownerUuid == null ? "" : ownerUuid;
        this.ownerName = ownerName == null ? "" : ownerName;
        this.added = added;
    }

    public static void encode(PacketWardSync packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.dimension, 256);
        buffer.writeBoolean(packet.full);
        buffer.writeNbt(packet.data);
        buffer.writeBlockPos(packet.pos);
        buffer.writeUtf(packet.ownerUuid, 256);
        buffer.writeUtf(packet.ownerName, 256);
        buffer.writeBoolean(packet.added);
    }

    public static PacketWardSync decode(FriendlyByteBuf buffer) {
        String dimension = buffer.readUtf(256);
        boolean full = buffer.readBoolean();
        CompoundTag data = buffer.readNbt();
        BlockPos pos = buffer.readBlockPos();
        String ownerUuid = buffer.readUtf(256);
        String ownerName = buffer.readUtf(256);
        boolean added = buffer.readBoolean();
        return new PacketWardSync(dimension, full, data, pos, ownerUuid, ownerName, added);
    }

    public static void handle(PacketWardSync packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketWardSync packet) {
        if (packet.full) {
            WardClientRegistry.replace(packet.dimension, packet.data);
        } else if (packet.added && !packet.ownerUuid.isEmpty()) {
            WardClientRegistry.add(packet.dimension, packet.pos,
                    new WardEntry(packet.ownerUuid, packet.ownerName));
        } else {
            WardClientRegistry.remove(packet.dimension, packet.pos);
        }
    }
}
