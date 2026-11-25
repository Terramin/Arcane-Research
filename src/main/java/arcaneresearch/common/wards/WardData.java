package arcaneresearch.common.wards;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Pure NBT helpers for the warded-block registry. No world/session state. */
public final class WardData {
    public static final String TAG_WARDS = "wards";
    public static final String TAG_POS = "pos";
    public static final String TAG_OWNER = "owner";
    public static final String TAG_NAME = "name";

    private WardData() {
    }

    public static CompoundTag serialize(Map<BlockPos, WardEntry> wards) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, WardEntry> entry : wards.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            CompoundTag tag = entry.getValue().toNbt();
            tag.putLong(TAG_POS, entry.getKey().asLong());
            list.add(tag);
        }
        root.put(TAG_WARDS, list);
        return root;
    }

    public static Map<BlockPos, WardEntry> deserialize(CompoundTag tag) {
        Map<BlockPos, WardEntry> out = new HashMap<>();
        if (tag == null) {
            return out;
        }
        ListTag list = tag.getList(TAG_WARDS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.contains(TAG_POS)) {
                continue;
            }
            out.put(BlockPos.of(entry.getLong(TAG_POS)), WardEntry.fromNbt(entry));
        }
        return out;
    }

    /** A block can only be warded when nobody else has already warded it. */
    public static boolean canAdd(Map<BlockPos, WardEntry> wards, BlockPos pos, UUID player) {
        WardEntry current = wards.get(pos);
        return current == null || (player != null && current.isOwnedBy(player));
    }

    /** Only the owner (or a legacy entry without an owner) may lift a ward. */
    public static boolean canRemove(Map<BlockPos, WardEntry> wards, BlockPos pos, UUID player) {
        WardEntry current = wards.get(pos);
        return current != null && (player != null && current.isOwnedBy(player));
    }

    /** Explosions must never include a warded coordinate in their block list. */
    public static List<BlockPos> filterProtected(Collection<BlockPos> affected,
                                                 Collection<BlockPos> protectedPositions) {
        return affected.stream().filter(pos -> !protectedPositions.contains(pos)).toList();
    }
}
