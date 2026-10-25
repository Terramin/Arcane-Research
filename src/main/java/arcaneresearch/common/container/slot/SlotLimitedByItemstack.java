package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
@Deprecated
public class SlotLimitedByItemstack extends Slot {
    protected ItemStack limitItem;
    public SlotLimitedByItemstack(ItemStack item, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limitItem = ItemStack.EMPTY;
        this.limitItem = item == null ? ItemStack.EMPTY : item.copy();
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && !limitItem.isEmpty() && ItemStack.isSameItem(stack, limitItem);
    }
}
