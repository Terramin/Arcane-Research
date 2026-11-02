package arcaneresearch.common.menu;

import arcaneresearch.common.menu.slot.OutputSlot;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class GolemBuilderMenu extends AbstractContainerMenu {

    private static final int OUTPUT_SLOT = 0;

    private final TileGolemBuilder blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public GolemBuilderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public GolemBuilderMenu(int containerId, Inventory playerInventory, TileGolemBuilder blockEntity) {
        super(ModMenuTypes.GOLEM_BUILDER.get(), containerId);
        this.blockEntity = blockEntity;

        Level level = blockEntity.getLevel();
        this.access = level != null
                ? ContainerLevelAccess.create(level, blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        this.data = new SimpleContainerData(2);
        addDataSlots(data);

        addSlot(new OutputSlot(blockEntity, OUTPUT_SLOT, 160, 104));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 24 + col * 18, 142 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 24 + col * 18, 200));
        }
    }

    private static TileGolemBuilder getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof TileGolemBuilder tileGolemBuilder) return tileGolemBuilder;
        throw new IllegalStateException("Block entity is not a TileGolemBuilder at " + pos + ": " + be);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        return stillValid(access, player, ModBlocks.GOLEM_BUILDER.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        final int total = this.slots.size();
        if (slotIndex < 0 || slotIndex >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (slotIndex == OUTPUT_SLOT) {
            if (!moveItemStackTo(stackInSlot, 1, total, true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stackInSlot, result);
        } else {
            boolean moved = blockEntity.canPlaceItem(OUTPUT_SLOT, stackInSlot)
                    && moveItemStackTo(stackInSlot, OUTPUT_SLOT, OUTPUT_SLOT + 1, false);
            if (!moved) {
                if (slotIndex < 28) {
                    moved = moveItemStackTo(stackInSlot, 28, total, false);
                } else {
                    moved = moveItemStackTo(stackInSlot, 1, 28, false);
                }
            }
            if (!moved) return ItemStack.EMPTY;
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
    public void broadcastChanges() {
        if (blockEntity != null) {
            data.set(0, blockEntity.cost);
            data.set(1, blockEntity.maxCost);
        }
        super.broadcastChanges();
    }

    public TileGolemBuilder getBlockEntity() {
        return blockEntity;
    }

    public int getCost() {
        return data.get(0);
    }

    public int getMaxCost() {
        return data.get(1);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        return buttonId == 99;
    }
}