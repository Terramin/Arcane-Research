package arcaneresearch.common.menu;

import arcaneresearch.api.wands.ItemFocusBasic;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class WandPouchInventory implements Container {

    public ItemStack[] stackList;
    private final AbstractContainerMenu eventHandler;

    public WandPouchInventory(AbstractContainerMenu eventHandler) {
        this.stackList = new ItemStack[18];
        Arrays.fill(this.stackList, ItemStack.EMPTY);
        this.eventHandler = Objects.requireNonNull(eventHandler, "eventHandler");
    }

    private boolean inBounds(int index) {
        return index >= 0 && index < this.stackList.length;
    }

    @Override
    public int getContainerSize() {
        return this.stackList.length;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stackList) {
            if (stack != null && !stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        if (!inBounds(index)) return ItemStack.EMPTY;
        ItemStack stack = this.stackList[index];
        return stack != null ? stack : ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int index) {
        if (!inBounds(index)) return ItemStack.EMPTY;
        ItemStack stack = this.stackList[index];
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
        this.stackList[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int count) {
        if (!inBounds(index) || count <= 0) return ItemStack.EMPTY;
        ItemStack stack = this.stackList[index];
        if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;

        if (stack.getCount() <= count) {
            this.stackList[index] = ItemStack.EMPTY;
            this.eventHandler.slotsChanged(this);
            return stack;
        }

        ItemStack split = stack.split(count);
        this.eventHandler.slotsChanged(this);
        return split;
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (!inBounds(index)) return;
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
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFocusBasic;
    }

    @Override
    public void startOpen(@NotNull Player player) {
    }

    @Override
    public void stopOpen(@NotNull Player player) {
    }

    @Override
    public void clearContent() {
        Arrays.fill(this.stackList, ItemStack.EMPTY);
    }
}