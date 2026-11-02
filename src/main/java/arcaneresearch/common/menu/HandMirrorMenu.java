package arcaneresearch.common.menu;

import arcaneresearch.common.items.tools.ItemHandMirror;
import arcaneresearch.init.ModMenuTypes;
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

public class HandMirrorMenu extends AbstractContainerMenu implements ContainerListener {

    private final Player player;
    private final ItemStack mirror;
    private final InteractionHand hand;
    private final SimpleContainer transportSlot;
    private boolean transporting = false;

    public HandMirrorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readEnum(InteractionHand.class));
    }

    public HandMirrorMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.HAND_MIRROR.get(), containerId);
        this.player = playerInventory.player;
        this.hand = hand;
        this.mirror = player.getItemInHand(hand);
        this.transportSlot = new SimpleContainer(1);
        transportSlot.addListener(this);

        addSlot(new Slot(transportSlot, 0, 80, 24));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void containerChanged(@NotNull Container container) {
        if (transporting) return;

        ItemStack stackInSlot = transportSlot.getItem(0);

        if (!stackInSlot.isEmpty() && !mirror.isEmpty() && ItemStack.isSameItem(stackInSlot, mirror)) {
            player.closeContainer();
            return;
        }

        if (player.level().isClientSide || stackInSlot.isEmpty()) return;

        transporting = true;
        boolean broadcast;
        try {
            ItemStack toTransport = stackInSlot.copy();
            transportSlot.setItem(0, ItemStack.EMPTY);
            if (!ItemHandMirror.transport(mirror, toTransport, player, player.level())) {
                transportSlot.setItem(0, toTransport);
            }
            broadcast = true;
        } finally {
            transporting = false;
        }
        if (broadcast) broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot.hasItem() && slot.getItem().getItem() instanceof ItemHandMirror) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        if (stackInSlot.getItem() instanceof ItemHandMirror) return ItemStack.EMPTY;

        ItemStack result = stackInSlot.copy();

        if (index == 0) {
            if (!moveItemStackTo(stackInSlot, 1, total, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
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
        return player.getItemInHand(hand).getItem() instanceof ItemHandMirror;
    }

    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;

        ItemStack remaining = transportSlot.removeItemNoUpdate(0);
        if (!remaining.isEmpty()) {
            player.drop(remaining, false);
        }
    }
}