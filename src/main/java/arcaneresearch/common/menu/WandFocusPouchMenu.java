package arcaneresearch.common.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput; // NEOFORGE_PORT: ContainerInput -> ContainerInput
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension; // NEOFORGE_PORT: same create(factory) shape
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.items.wands.ItemFocusPouch;

public class WandFocusPouchMenu extends AbstractContainerMenu {
    public final Container input = new WandPouchInventory(this);
    private ItemStack pouch = ItemStack.EMPTY;
    private Player player = null;
    private int blockSlot;

    public WandFocusPouchMenu(int windowId, Inventory playerInv, FriendlyByteBuf extra) {
        this(WandMenuTypes.WAND_FOCUS_POUCH.get(), windowId, playerInv);
        try {
            extra.readEnum(InteractionHand.class);
        } catch (Exception ignored) {
        }
    }

    public WandFocusPouchMenu(MenuType<?> type, int windowId, Inventory playerInv) {
        super(type, windowId);
        this.player = playerInv.player;
        this.pouch = playerInv.getSelectedItem();
        this.blockSlot = playerInv.getSelectedSlot() + 45;
        for (int a = 0; a < 18; a++) {
            addSlot(new SlotLimitedByFocus(this.input, a, 37 + a % 6 * 18, 51 + a / 6 * 18));
        }
        bindPlayerInventory(playerInv);
        if (this.pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            try {
                NonNullList<ItemStack> stored = pouchItem.getInventory(this.pouch);
                for (int a = 0; a < 18 && a < stored.size(); a++) {
                    ((WandPouchInventory) this.input).stackList[a] = stored.get(a).copy();
                }
            } catch (Exception ignored) {
            }
        }
        slotsChanged(this.input);
    }

    public int getBlockSlot() {
        return this.blockSlot;
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        int i;
        for (i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
            }
        }
        for (i = 0; i < 9; i++) {
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 209));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        if (slot == this.blockSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            result = stackInSlot.copy();
            if (slot < 18) {
                if (!this.input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 18, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 18, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId == this.blockSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && !this.pouch.isEmpty() && this.pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            NonNullList<ItemStack> stored = NonNullList.withSize(18, ItemStack.EMPTY);
            for (int a = 0; a < 18; a++) {
                stored.set(a, ((WandPouchInventory) this.input).stackList[a].copy());
            }
            pouchItem.setInventory(this.pouch, stored);
            if (this.player == null) {
                return;
            }
            if (!ItemStack.isSameItem(this.player.getInventory().getSelectedItem(), this.pouch)) {
                this.player.getInventory().setItem(this.player.getInventory().getSelectedSlot(), this.pouch);
            }
            this.player.getInventory().setChanged();
        }
    }

    public static class SlotLimitedByFocus extends Slot {
        public SlotLimitedByFocus(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ItemFocusBasic;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static class Types {
        public static final MenuType<WandFocusPouchMenu> DUMMY =
                IMenuTypeExtension.create(WandFocusPouchMenu::new);
    }
}
