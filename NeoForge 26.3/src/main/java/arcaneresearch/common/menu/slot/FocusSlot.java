package arcaneresearch.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.items.casters.ItemFocus;

public class FocusSlot extends Slot {

    private final int limit;

    public FocusSlot(Container container, int slot, int x, int y) {
        this(container, slot, x, y, 64);
    }

    public FocusSlot(Container container, int slot, int x, int y, int limit) {
        super(container, slot, x, y);
        this.limit = limit;
    }

    public static boolean isValidFocus(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemFocus;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidFocus(stack);
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }
}
