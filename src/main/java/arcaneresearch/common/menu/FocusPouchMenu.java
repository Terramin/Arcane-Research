package arcaneresearch.common.menu;

import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.casters.ItemFocusPouch;
import arcaneresearch.common.menu.slot.FocusSlot;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FocusPouchMenu extends AbstractContainerMenu implements ContainerListener {

    public static final int POUCH_SIZE = 18;

    private final Player player;
    private final ItemStack pouch;
    private final InteractionHand hand;
    private final int blockedSlot;
    private final SimpleContainer pouchInventory;

    public FocusPouchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readEnum(InteractionHand.class));
    }

    public FocusPouchMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.FOCUS_POUCH.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;
        this.pouch = player.getItemInHand(hand);
        this.blockedSlot = (hand == InteractionHand.MAIN_HAND)
                ? playerInventory.selected + POUCH_SIZE + 27
                : -1;

        this.pouchInventory = new SimpleContainer(POUCH_SIZE) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemFocus;
            }
        };

        if (pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            NonNullList<ItemStack> contents = pouchItem.getInventory(pouch);
            if (contents != null) {
                int n = Math.min(contents.size(), POUCH_SIZE);
                for (int i = 0; i < n; i++) {
                    ItemStack stack = contents.get(i);
                    pouchInventory.setItem(i, stack);
                }
            }
        }

        pouchInventory.addListener(this);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 6; col++) {
                int index = col + row * 6;
                addSlot(new FocusSlot(pouchInventory, index, 37 + col * 18, 51 + row * 18, 1));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 209));
        }
    }

    @Override
    public void containerChanged(@NotNull Container container) {
        broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (blockedSlot >= 0 && slotId == blockedSlot) return;
        if (slotId >= 0 && slotId >= this.slots.size()) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        if (blockedSlot >= 0 && index == blockedSlot) return ItemStack.EMPTY;

        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index < POUCH_SIZE) {
            if (!moveItemStackTo(stackInSlot, POUCH_SIZE, total, true)) return ItemStack.EMPTY;
        } else {
            if (!(stackInSlot.getItem() instanceof ItemFocus)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stackInSlot, 0, POUCH_SIZE, false)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.getItemInHand(hand).getItem() instanceof ItemFocusPouch;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;
        if (!(pouch.getItem() instanceof ItemFocusPouch pouchItem)) return;

        if (player.getItemInHand(hand) != pouch) {
            return;
        }

        NonNullList<ItemStack> contents = NonNullList.withSize(POUCH_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < POUCH_SIZE; i++) {
            ItemStack stack = pouchInventory.getItem(i);
            contents.set(i, stack);
        }
        pouchItem.setInventory(pouch, contents);
        player.setItemInHand(hand, pouch);
    }

    public Container getPouchInventory() {
        return pouchInventory;
    }

    public InteractionHand getHand() {
        return hand;
    }
}