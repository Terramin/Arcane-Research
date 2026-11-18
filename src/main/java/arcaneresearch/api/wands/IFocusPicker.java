package arcaneresearch.api.wands;

import net.minecraft.world.item.ItemStack;

public interface IFocusPicker {
    ItemStack getPickedBlock(ItemStack stack);
}
