package arcaneresearch.api.wands;

import net.minecraft.world.item.ItemStack;

/**
 * A focus host with wand-specific construction: a rod and a cap.
 * Casters implement {@link IFocusHost} only — they have neither.
 */
public interface IFocusHostWand extends IFocusHost {

    WandRod getRod(ItemStack stack);

    void setRod(ItemStack stack, WandRod rod);

    WandCap getCap(ItemStack stack);

    void setCap(ItemStack stack, WandCap cap);

    boolean isStaff(ItemStack stack);

    /**
     * A sceptre is a wand with the {@code sceptre} flag set. The flag is part
     * of the wand's data, not of its rod/staff geometry: a sceptre can be
     * built from any rod. Always read the boolean, never just tag presence —
     * {@code sceptre: false} must not turn a wand into a sceptre.
     */
    default boolean isSceptre(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("sceptre");
    }

    default void setSceptre(ItemStack stack, boolean sceptre) {
        if (stack.isEmpty()) {
            return;
        }
        if (sceptre) {
            stack.getOrCreateTag().putBoolean("sceptre", true);
        } else if (stack.getTag() != null) {
            stack.getTag().remove("sceptre");
        }
    }
}
