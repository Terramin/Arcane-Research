package thaumictinker.kami.trinkets;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.common.items.wands.ItemFocusPouch;

 




 
public class IchorPouchMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 13;
    public static final int ROWS = 9;
    public static final int POUCH_SLOTS = COLUMNS * ROWS;
    public static final int INVENTORY_START = POUCH_SLOTS;
    public static final int HOTBAR_START = INVENTORY_START + 27;
    public static final int SLOT_END = HOTBAR_START + 9;
    public static final int INVENTORY_X = 48;
    public static final int INVENTORY_Y = 177;

    private final SimpleContainer pouchContainer = new SimpleContainer(POUCH_SLOTS);
    private final ItemStack pouch;
    private final int blockedSlot;

    public IchorPouchMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ItemStack.EMPTY);
    }

    public IchorPouchMenu(int containerId, Inventory inventory, ItemStack pouch) {
        super(TrinketsFeature.ICHOR_POUCH_MENU, containerId);
        this.pouch = pouch;
        this.blockedSlot = HOTBAR_START + inventory.selected;
        if (!pouch.isEmpty()) {
            load(pouch);
        }
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = row * COLUMNS + column;
                addSlot(new PouchSlot(pouchContainer, index, 12 + column * 18, 8 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        INVENTORY_X + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * 18, INVENTORY_Y + 58));
        }
    }

    private void load(ItemStack stack) {
        NonNullList<ItemStack> contents = TrinketsFeature.focusPouch().getInventory(stack);
        for (int slot = 0; slot < POUCH_SLOTS && slot < contents.size(); slot++) {
            pouchContainer.setItem(slot, contents.get(slot));
        }
    }

    public int getBlockedSlot() {
        return blockedSlot;
    }

    public Container pouchContainer() {
        return pouchContainer;
    }

    private void save() {
        if (pouch.isEmpty()) {
            return;
        }
        NonNullList<ItemStack> contents = NonNullList.withSize(POUCH_SLOTS, ItemStack.EMPTY);
        for (int slot = 0; slot < POUCH_SLOTS; slot++) {
            contents.set(slot, pouchContainer.getItem(slot));
        }
        TrinketsFeature.focusPouch().setInventory(pouch, contents);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            save();
            player.getInventory().setChanged();
        }
    }

    @Override
    public void clicked(int slotIndex, int button, ClickType clickType, Player player) {
        if (slotIndex == blockedSlot) {
            return;
        }
        if (clickType == ClickType.SWAP && button == blockedSlot - HOTBAR_START) {
            return;
        }
        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == blockedSlot) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < POUCH_SLOTS) {
            if (!moveItemStackTo(stack, POUCH_SLOTS, SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (stack.getItem() instanceof ItemFocusPouch || !moveItemStackTo(stack, 0, POUCH_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return pouch.isEmpty() || player.getInventory().getSelected().getItem() instanceof IchorPouchItem;
    }

    private static final class PouchSlot extends Slot {

        private PouchSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !(stack.getItem() instanceof ItemFocusPouch);
        }
    }
}
