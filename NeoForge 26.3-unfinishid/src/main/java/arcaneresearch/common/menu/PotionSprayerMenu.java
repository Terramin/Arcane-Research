package arcaneresearch.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.menu.slot.PotionSlot;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;
import arcaneresearch.init.ModMenuTypes;

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
        if (be instanceof TilePotionSprayer tile) {
            return tile;
        }
        throw new IllegalStateException("Block entity is not a TilePotionSprayer");
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {

                if (!blockEntity.canPlaceItem(0, stackInSlot) || !moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!blockEntity.canPlaceItem(0, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {

                return ItemStack.EMPTY;
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

    public TilePotionSprayer getBlockEntity() {
        return blockEntity;
    }
}
