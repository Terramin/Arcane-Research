package arcaneresearch.common.lib.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;

import java.util.List;

public class ArcaneWorkbenchCraftingContainer implements CraftingContainer, IArcaneWorkbench {

    private final TileArcaneWorkbench tile;
    private final AbstractContainerMenu menu;

    public ArcaneWorkbenchCraftingContainer(TileArcaneWorkbench tile, AbstractContainerMenu menu) {
        this.tile = tile;
        this.menu = menu;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public List<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, getItem(i));
        }
        return items;
    }

    @Override
    public int getContainerSize() {
        return TileArcaneWorkbench.TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (!getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return tile.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = tile.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result;
        if (stack.getCount() <= amount) {
            result = stack.copy();
            tile.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            result = stack.split(amount);
            tile.setStackInSlot(slot, stack);
        }

        if (menu != null) {
            menu.slotsChanged(this);
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = tile.getStackInSlot(slot);
        tile.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        tile.setStackInSlot(slot, stack);
        if (menu != null) {
            menu.slotsChanged(this);
        }
    }

    @Override
    public void setChanged() {
        tile.setChanged();
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        return tile.stillValid(player);
    }

    @Override
    public void clearContent() {
        tile.clearCraftMatrix();
    }

    // NEOFORGE_PORT: StackedContentsCompatible takes StackedItemContents in 26.x.
    @Override
    public void fillStackedContents(net.minecraft.world.entity.player.StackedItemContents contents) {

        for (int i = 0; i < TileArcaneWorkbench.GRID_SIZE; i++) {
            contents.accountStack(getItem(i));
        }
    }

    public TileArcaneWorkbench getTile() {
        return tile;
    }
}
