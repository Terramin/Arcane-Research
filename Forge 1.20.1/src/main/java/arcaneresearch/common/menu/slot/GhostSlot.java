package arcaneresearch.common.menu.slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class GhostSlot extends Slot {
    private final int maxStackSize;
    private final java.util.function.BooleanSupplier active;
    private final boolean placeable;
    public GhostSlot(Container container, int slot, int x, int y) {
        this(container, slot, x, y, Integer.MAX_VALUE, () -> true, true);
    }
    public GhostSlot(Container container, int slot, int x, int y, int maxStackSize) {
        this(container, slot, x, y, maxStackSize, () -> true, true);
    }
    public GhostSlot(Container container, int slot, int x, int y, int maxStackSize,
                     java.util.function.BooleanSupplier active) {
        this(container, slot, x, y, maxStackSize, active, true);
    }
    public GhostSlot(Container container, int slot, int x, int y, int maxStackSize,
                     java.util.function.BooleanSupplier active, boolean placeable) {
        super(container, slot, x, y);
        this.maxStackSize = maxStackSize;
        this.active = active;
        this.placeable = placeable;
    }
    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }
    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return placeable && isActive();
    }
    @Override
    public boolean isActive() {
        return active.getAsBoolean();
    }
    public void setGhostItem(ItemStack stack) {
        set(stack.copy());
    }
    public void clearGhost() {
        set(ItemStack.EMPTY);
    }
}
