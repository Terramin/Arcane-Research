package arcaneresearch.common.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.wands.ItemFocusBasic;

import java.util.Arrays;

public class WandPouchInventory implements Container {
    public ItemStack[] stackList;
    private AbstractContainerMenu eventHandler;

    public WandPouchInventory(AbstractContainerMenu eventHandler) {
        this.stackList = new ItemStack[18];
        Arrays.fill(this.stackList, ItemStack.EMPTY);
        this.eventHandler = eventHandler;
    }

    @Override
    public int getContainerSize() {
        return this.stackList.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stackList) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= getContainerSize() ? ItemStack.EMPTY : this.stackList[index];
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index >= getContainerSize() || this.stackList[index].isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.stackList[index];
        this.stackList[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack stack = getItem(index);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (stack.getCount() <= count) {
            setItem(index, ItemStack.EMPTY);
            this.eventHandler.slotsChanged(this);
            return stack;
        }
        ItemStack split = stack.split(count);
        if (stack.getCount() == 0) {
            setItem(index, ItemStack.EMPTY);
        }
        this.eventHandler.slotsChanged(this);
        return split;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.stackList[index] = stack.isEmpty() ? ItemStack.EMPTY : stack;
        this.eventHandler.slotsChanged(this);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFocusBasic;
    }

    // NEOFORGE_PORT: Container no longer declares startOpen/stopOpen in 26.x
    // (Forge no-op overrides kept as plain no-op methods for parity callers).
    public void startOpen(Player player) {
    }

    // NEOFORGE_PORT: see startOpen above.
    public void stopOpen(Player player) {
    }

    @Override
    public void clearContent() {
        Arrays.fill(this.stackList, ItemStack.EMPTY);
    }
}
