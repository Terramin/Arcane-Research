package arcaneresearch.common.menu;
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
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.casters.ItemFocusPouch;
import arcaneresearch.common.menu.slot.FocusSlot;
import arcaneresearch.init.ModMenuTypes;
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
        this.blockedSlot = (hand == InteractionHand.MAIN_HAND) ?
                playerInventory.selected + POUCH_SIZE + 27 : -1;
        this.pouchInventory = new SimpleContainer(POUCH_SIZE) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return !stack.isEmpty() && stack.getItem() instanceof ItemFocus;
            }
        };
        pouchInventory.addListener(this);
        if (pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            NonNullList<ItemStack> contents = pouchItem.getInventory(pouch);
            for (int i = 0; i < Math.min(contents.size(), POUCH_SIZE); i++) {
                pouchInventory.setItem(i, contents.get(i));
            }
        }
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
    public void containerChanged(Container container) {
        broadcastChanges();
    }
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == blockedSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index == blockedSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index < POUCH_SIZE) {
                if (!moveItemStackTo(stackInSlot, POUCH_SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (stackInSlot.getItem() instanceof ItemFocus) {
                    if (!moveItemStackTo(stackInSlot, 0, POUCH_SIZE, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return result;
    }
    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(hand).getItem() instanceof ItemFocusPouch;
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && pouch.getItem() instanceof ItemFocusPouch pouchItem) {
            NonNullList<ItemStack> contents = NonNullList.withSize(POUCH_SIZE, ItemStack.EMPTY);
            for (int i = 0; i < POUCH_SIZE; i++) {
                contents.set(i, pouchInventory.getItem(i));
            }
            pouchItem.setInventory(pouch, contents);
            player.setItemInHand(hand, pouch);
        }
    }
    public Container getPouchInventory() {
        return pouchInventory;
    }
    public InteractionHand getHand() {
        return hand;
    }
}
