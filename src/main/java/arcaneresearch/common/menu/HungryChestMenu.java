package arcaneresearch.common.menu;

import arcaneresearch.common.tiles.devices.TileHungryChest;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class HungryChestMenu extends AbstractContainerMenu {

    public static final int CHEST_SIZE = 27;
    public static final int CHEST_ROWS = 3;
    public static final int CHEST_START = 0;
    public static final int CHEST_END = 26;
    public static final int PLAYER_INV_START = 27;
    public static final int PLAYER_INV_END = 53;
    public static final int PLAYER_HOTBAR_START = 54;
    public static final int PLAYER_HOTBAR_END = 62;
    public static final int PLAYER_SLOTS_END = 63;

    private final TileHungryChest blockEntity;
    private final ContainerLevelAccess access;

    public HungryChestMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public HungryChestMenu(int containerId, Inventory playerInventory, TileHungryChest blockEntity) {
        super(ModMenuTypes.HUNGRY_CHEST.get(), containerId);
        this.blockEntity = blockEntity;

        Level level = blockEntity.getLevel();
        this.access = level != null
                ? ContainerLevelAccess.create(level, blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        blockEntity.startOpen(playerInventory.player);

        for (int row = 0; row < CHEST_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(blockEntity, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static TileHungryChest getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileHungryChest chest) return chest;
        throw new IllegalStateException("Block entity is not a TileHungryChest: " + be);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        return stillValid(access, player, ModBlocks.HUNGRY_CHEST.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index < CHEST_SIZE) {
            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_SLOTS_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index <= PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(stackInSlot, CHEST_START, CHEST_END + 1, false)) {
                if (index < PLAYER_HOTBAR_START) {
                    if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_SLOTS_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            return ItemStack.EMPTY;
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
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (blockEntity != null) {
            blockEntity.stopOpen(player);
        }
    }

    public TileHungryChest getBlockEntity() {
        return blockEntity;
    }
}