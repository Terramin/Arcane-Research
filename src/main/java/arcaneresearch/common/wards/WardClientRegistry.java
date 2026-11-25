package arcaneresearch.common.wards;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side mirror of the ward registry. Kept in a common package so the
 * architect preview can consult it without depending on a client-only class.
 */
public final class WardClientRegistry {
    private static final Map<String, Map<BlockPos, WardEntry>> WARDS = new HashMap<>();

    private WardClientRegistry() {
    }

    public static synchronized void replace(String dimension, CompoundTag data) {
        WARDS.put(dimension, WardData.deserialize(data));
    }

    public static synchronized void add(String dimension, BlockPos pos, WardEntry entry) {
        WARDS.computeIfAbsent(dimension, key -> new HashMap<>()).put(pos, entry);
    }

    public static synchronized void remove(String dimension, BlockPos pos) {
        Map<BlockPos, WardEntry> entries = WARDS.get(dimension);
        if (entries != null) {
            entries.remove(pos);
        }
    }

    public static synchronized void clear() {
        WARDS.clear();
    }

    public static synchronized Map<BlockPos, WardEntry> snapshot(String dimension) {
        Map<BlockPos, WardEntry> entries = WARDS.get(dimension);
        return entries == null ? Map.of() : new HashMap<>(entries);
    }

    public static synchronized WardEntry get(String dimension, BlockPos pos) {
        Map<BlockPos, WardEntry> entries = WARDS.get(dimension);
        return entries == null ? null : entries.get(pos);
    }
}
