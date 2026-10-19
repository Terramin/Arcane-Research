package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.items.casters.ItemFocus;
@Deprecated
public class SlotFocus extends Slot {
    protected int limit;
    public SlotFocus(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limit = 64;
    }
    public SlotFocus(int limit, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limit = 64;
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
