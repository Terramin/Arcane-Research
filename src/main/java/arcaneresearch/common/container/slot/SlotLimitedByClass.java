package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
@Deprecated
public class SlotLimitedByClass extends Slot {
    protected Class<?> clazz;
    protected int limit;
    public SlotLimitedByClass(Class<?> clazz, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.clazz = Object.class;
        this.limit = 64;
        this.clazz = clazz;
    }
    public SlotLimitedByClass(Class<?> clazz, int limit, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.clazz = Object.class;
        this.limit = 64;
        this.clazz = clazz;
        this.limit = limit;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && clazz.isAssignableFrom(stack.getItem().getClass());
    }
    @Override
    public int getMaxStackSize() {
        return limit;
    }
}
