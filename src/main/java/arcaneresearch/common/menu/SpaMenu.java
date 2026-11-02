package arcaneresearch.common.menu;

import arcaneresearch.common.items.consumables.ItemBathSalts;
import arcaneresearch.common.menu.slot.LimitedByClassSlot;
import arcaneresearch.common.tiles.devices.TileSpa;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class SpaMenu extends AbstractContainerMenu {

    private final TileSpa blockEntity;

    public SpaMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public SpaMenu(int containerId, Inventory playerInventory, TileSpa blockEntity) {
        super(ModMenuTypes.SPA.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new LimitedByClassSlot(ItemBathSalts.class, blockEntity, 0, 65, 31));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static TileSpa getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileSpa tile) return tile;
        throw new IllegalStateException("Block entity is not a TileSpa: " + be);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int button) {
        if (button != 1 || blockEntity == null) return false;
        blockEntity.toggleMix();
        return true;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return blockEntity != null && blockEntity.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index == 0) {
            if (!moveItemStackTo(stackInSlot, 1, total, true)) return ItemStack.EMPTY;
        } else if (stackInSlot.getItem() instanceof ItemBathSalts) {
            if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                if (index < 28) {
                    if (!moveItemStackTo(stackInSlot, 28, total, false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stackInSlot, 1, 28, false)) return ItemStack.EMPTY;
                }
            }
        } else {
            if (index < 28) {
                if (!moveItemStackTo(stackInSlot, 28, total, false)) return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stackInSlot, 1, 28, false)) return ItemStack.EMPTY;
            }
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

    public TileSpa getBlockEntity() {
        return blockEntity;
    }
}