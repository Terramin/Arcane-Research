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

    boolean isSceptre(ItemStack stack);
}
