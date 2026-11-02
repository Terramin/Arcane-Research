package arcaneresearch.common.menu;

import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.items.wands.ItemFocusPouch;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.jetbrains.annotations.NotNull;

public class WandFocusPouchMenu extends AbstractContainerMenu {

    public final WandPouchInventory input = new WandPouchInventory(this);
    private ItemStack pouch;
    private Player player;
    private int blockSlot;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public WandFocusPouchMenu(int windowId, Inventory playerInv, FriendlyByteBuf extra) {
        this(WandMenuTypes.WAND_FOCUS_POUCH.get(), windowId, playerInv);
        try {
            this.hand = extra.readEnum(InteractionHand.class);
        } catch (Exception ignored) {
        }
    }

    public WandFocusPouchMenu(MenuType<?> type, int windowId, Inventory playerInv) {
        super(type, windowId);
        this.player = playerInv.player;
        this.pouch = playerInv.getSelected();
        this.blockSlot = playerInv.selected + 45;

        for (int a = 0; a < 18; a++) {
            addSlot(new SlotLimitedByFocus(this.input, a, 37 + a % 6 * 18, 51 + a / 6 * 18));
        }
        bindPlayerInventory(playerInv);

        if (this.pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            try {
                NonNullList<ItemStack> stored = pouchItem.getInventory(this.pouch);
                if (stored != null) {
                    int n = Math.min(18, stored.size());
                    for (int a = 0; a < n; a++) {
                        ItemStack s = stored.get(a);
                        this.input.stackList[a] = s.copy();
                    }
                }
            } catch (Exception ignored) {
            }
        }
        slotsChanged(this.input);
    }

    public int getBlockSlot() {
        return this.blockSlot;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    protected void bindPlayerInventory(Inventory inv) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 209));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slot) {
        if (slot < 0 || slot >= this.slots.size()) return ItemStack.EMPTY;
        if (slot == this.blockSlot) return ItemStack.EMPTY;

        ItemStack result = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);
        if (!slotObject.hasItem()) return result;

        ItemStack stackInSlot = slotObject.getItem();
        result = stackInSlot.copy();
        final int totalSlots = this.slots.size();

        if (slot < 18) {
            if (!this.input.canPlaceItem(slot, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 18, totalSlots, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.input.canPlaceItem(slot, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 18, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slotObject.set(ItemStack.EMPTY);
        } else {
            slotObject.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId == this.blockSlot) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);

        Player owner = this.player;
        if (owner == null || owner.level().isClientSide) return;
        if (this.pouch.isEmpty() || !(this.pouch.getItem() instanceof ItemFocusPouch pouchItem)) return;

        NonNullList<ItemStack> stored = NonNullList.withSize(18, ItemStack.EMPTY);
        for (int a = 0; a < 18; a++) {
            ItemStack s = this.input.stackList[a];
            stored.set(a, s == null ? ItemStack.EMPTY : s.copy());
        }
        pouchItem.setInventory(this.pouch, stored);

        int originalSlot = this.blockSlot - 45;
        if (originalSlot < 0 || originalSlot >= owner.getInventory().getContainerSize()) {
            owner.getInventory().setChanged();
            return;
        }
        ItemStack current = owner.getInventory().getItem(originalSlot);
        if (current != this.pouch) {
            owner.getInventory().setItem(originalSlot, this.pouch);
        }
        owner.getInventory().setChanged();
    }

    public static class SlotLimitedByFocus extends Slot {
        public SlotLimitedByFocus(net.minecraft.world.Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof ItemFocusBasic;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    public static class Types {
        public static final MenuType<WandFocusPouchMenu> DUMMY =
                IForgeMenuType.create(WandFocusPouchMenu::new);
    }
}