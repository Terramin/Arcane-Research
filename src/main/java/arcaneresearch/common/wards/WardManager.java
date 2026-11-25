package arcaneresearch.common.wards;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.wards.PacketWardSync;

/** Server-side access to warded blocks plus client synchronization. */
public final class WardManager {
    private static final String DATA_NAME = "arcaneresearch_wards";

    private WardManager() {
    }

    public static WardStorage get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WardStorage::load, WardStorage::new, DATA_NAME);
    }

    public static boolean isWarded(LevelAccessor level, BlockPos pos) {
        if (pos == null) {
            return false;
        }
        if (level instanceof Level clientLevel && clientLevel.isClientSide) {
            return WardClientRegistry.get(dimensionKey(clientLevel), pos) != null;
        }
        return level instanceof ServerLevel serverLevel && get(serverLevel).isWarded(pos);
    }

    public static WardEntry getEntry(Level level, BlockPos pos) {
        if (pos == null) {
            return null;
        }
        if (level.isClientSide) {
            return WardClientRegistry.get(dimensionKey(level), pos);
        }
        return level instanceof ServerLevel serverLevel ? get(serverLevel).get(pos) : null;
    }

    public static String dimensionKey(Level level) {
        return level.dimension().location().toString();
    }

    public static void syncFull(ServerLevel level, ServerPlayer player) {
        java.util.Map<BlockPos, WardEntry> entries = new java.util.HashMap<>();
        for (java.util.Map.Entry<BlockPos, WardEntry> ward : get(level).entries().entrySet()) {
            WardEntry authoritative = getEntry(level, ward.getKey());
            entries.put(ward.getKey(), authoritative == null ? ward.getValue() : authoritative);
        }
        CompoundTag data = WardData.serialize(entries);
        PacketHandler.sendToPlayer(new PacketWardSync(dimensionKey(level), data), player);
    }

    public static void syncAdd(ServerLevel level, BlockPos pos, WardEntry entry) {
        PacketHandler.sendToDimension(new PacketWardSync(dimensionKey(level), pos, entry, true),
                level.dimension());
    }

    public static void syncRemove(ServerLevel level, BlockPos pos, WardEntry entry) {
        PacketHandler.sendToDimension(new PacketWardSync(dimensionKey(level), pos, entry, false),
                level.dimension());
    }
}
