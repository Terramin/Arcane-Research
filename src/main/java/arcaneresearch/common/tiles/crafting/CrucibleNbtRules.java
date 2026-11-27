package arcaneresearch.common.tiles.crafting;

import net.minecraft.nbt.CompoundTag;

/** Pure NBT helpers for the crucible, testable without a level or block entity. */
public final class CrucibleNbtRules {

    /** Key of the heat value. */
    public static final String HEAT = "Heat";
    /** Legacy flag that forces the tank to be empty after loading. */
    public static final String EMPTY = "Empty";

    private CrucibleNbtRules() {
    }

    /** @return heat clamped into the signed-short range used by the tag */
    public static short readHeat(CompoundTag tag) {
        return tag.getShort(HEAT);
    }

    /** @param heat current heat, potentially above the short range */
    public static void writeHeat(CompoundTag tag, int heat) {
        tag.putShort(HEAT, (short) clampHeat(heat));
    }

    /** @return legal short value for heat */
    public static int clampHeat(int heat) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, heat));
    }

    /** @return whether the legacy empty-tank flag is present */
    public static boolean hasEmptyFlag(CompoundTag tag) {
        return tag.contains(EMPTY);
    }
}
