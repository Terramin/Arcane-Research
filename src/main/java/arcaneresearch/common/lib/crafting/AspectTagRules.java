package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** Pure helpers for reading aspect data stored on item stacks. */
public final class AspectTagRules {

    /** NBT key holding the enchantments of an enchanted book. */
    public static final String STORED_ENCHANTMENTS = "StoredEnchantments";

    private AspectTagRules() {
    }

    /**
     * Chooses the enchantment list for an item. Books store them under a different key.
     *
     * @param book whether the item is an enchanted book
     * @param stackTag the stack's NBT, may be null
     * @param normal the standard {@code Enchantments} list, used for non-books
     * @return an enchantment list, never null and never the caller's list by reference
     */
    public static ListTag enchantmentTag(boolean book, CompoundTag stackTag, ListTag normal) {
        if (!book) {
            return normal == null ? new ListTag() : normal;
        }
        return stackTag == null ? new ListTag() : stackTag.getList(STORED_ENCHANTMENTS, 10);
    }

    /**
     * Resolves the aspects of an essentia container: stored essentia replaces the
     * item's base aspects instead of being added to them.
     *
     * @param base aspects registered for the item itself
     * @param contained aspects stored inside the container
     * @param ignoreContained when true, {@code contained} is ignored
     * @return a copy of the winning aspect list, never null
     */
    public static AspectList containerAspects(AspectList base, AspectList contained, boolean ignoreContained) {
        if (ignoreContained || contained == null || contained.size() == 0) {
            return base == null ? new AspectList() : base.copy();
        }
        return contained.copy();
    }

    /**
     * Copies aspects, clamping every amount to {@code maxAmount}. Negative amounts
     * are preserved (they are removed later by recipe generation).
     *
     * @param source aspects to copy, may be null
     * @param maxAmount per-aspect ceiling
     * @return a new, non-null list
     */
    public static AspectList cap(AspectList source, int maxAmount) {
        if (source == null) {
            return new AspectList();
        }
        AspectList result = new AspectList();
        for (Aspect aspect : source.getAspects()) {
            if (aspect != null) {
                result.add(aspect, Math.min(maxAmount, source.getAmount(aspect)));
            }
        }
        return result;
    }
}
