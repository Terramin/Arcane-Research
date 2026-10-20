package arcaneresearch.common.container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.items.casters.ItemFocus;

@Deprecated
public class InventoryFocusPouch extends SimpleContainer {
    public InventoryFocusPouch() {
        super(18);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFocus;
    }
}
