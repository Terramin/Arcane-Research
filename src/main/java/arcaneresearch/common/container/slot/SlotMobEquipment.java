package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
@Deprecated
public class SlotMobEquipment extends Slot {
    protected final Mob entity;
    public SlotMobEquipment(Mob entity, int slot, int x, int y) {
        super(new DummyContainer(), slot, x, y);
        this.entity = entity;
    }
    @Override
    public ItemStack getItem() {
        return entity.getItemInHand(InteractionHand.MAIN_HAND);
    }
    @Override
    public void set(ItemStack stack) {
        entity.setItemInHand(InteractionHand.MAIN_HAND, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }
    @Override
    public void setChanged() {
    }
    @Override
    public int getMaxStackSize() {
        return 64;
    }
    @Override
    public ItemStack remove(int amount) {
        if (getItem().isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (getItem().getCount() <= amount) {
            ItemStack held = getItem();
            set(ItemStack.EMPTY);
            return held;
        }
        ItemStack split = getItem().split(amount);
        if (getItem().isEmpty()) {
            set(ItemStack.EMPTY);
        }
        return split;
    }
    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }
    private static class DummyContainer implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }
        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }
        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }
        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }
        @Override
        public void setItem(int slot, ItemStack stack) {
        }
        @Override
        public void setChanged() {
        }
        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return true;
        }
        @Override
        public void clearContent() {
        }
    }
}
