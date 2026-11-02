package arcaneresearch.common.menu;

import arcaneresearch.common.menu.slot.PotionSlot;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class PotionSprayerMenu extends AbstractContainerMenu {

    private final TilePotionSprayer blockEntity;

    public PotionSprayerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public PotionSprayerMenu(int containerId, Inventory playerInventory, TilePotionSprayer blockEntity) {
        super(ModMenuTypes.POTION_SPRAYER.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new PotionSlot(blockEntity, 0, 56, 64));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 16 + col * 18, 209));
        }
    }

    private static TilePotionSprayer getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TilePotionSprayer tile) return tile;
        throw new IllegalStateException("Block entity is not a TilePotionSprayer: " + be);
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

        boolean moved;
        if (index == 0) {
            moved = moveItemStackTo(stackInSlot, 1, total, true);
        } else {
            moved = blockEntity.canPlaceItem(0, stackInSlot)
                    && moveItemStackTo(stackInSlot, 0, 1, false);
        }
        if (!moved) return ItemStack.EMPTY;

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    public TilePotionSprayer getBlockEntity() {
        return blockEntity;
    }
}