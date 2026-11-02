package arcaneresearch.common.menu;

import arcaneresearch.common.menu.slot.AspectSlot;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModMenuTypes;
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

public class SmelterMenu extends AbstractContainerMenu {

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int PLAYER_INV_START = 2;
    public static final int PLAYER_INV_END = 28;
    public static final int PLAYER_HOTBAR_START = 29;
    public static final int PLAYER_HOTBAR_END = 37;

    private final TileSmelter blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public SmelterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public SmelterMenu(int containerId, Inventory playerInventory, TileSmelter blockEntity) {
        super(ModMenuTypes.SMELTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = blockEntity.getLevel() != null
                ? ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;
        this.data = new SimpleContainerData(5);
        addDataSlots(this.data);

        addSlot(new AspectSlot(blockEntity, TileSmelter.SLOT_INPUT, 80, 8));
        addSlot(new Slot(blockEntity, TileSmelter.SLOT_FUEL, 80, 48) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return TileSmelter.isItemFuel(stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static TileSmelter getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileSmelter smelter) return smelter;
        throw new IllegalStateException("Block entity is not a TileSmelter: " + be);
    }

    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide) {
                this.data.set(0, blockEntity.furnaceCookTime);
                this.data.set(1, blockEntity.furnaceBurnTime);
                this.data.set(2, blockEntity.currentItemBurnTime);
                this.data.set(3, blockEntity.vis);
                this.data.set(4, blockEntity.smeltTime);
            }
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index == INPUT_SLOT || index == FUEL_SLOT) {
            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, total, true)) return ItemStack.EMPTY;
        } else if (index <= PLAYER_HOTBAR_END) {
            boolean moved = false;
            if (TileSmelter.isItemFuel(stackInSlot)) {
                moved = moveItemStackTo(stackInSlot, FUEL_SLOT, FUEL_SLOT + 1, false);
            }
            if (!moved) {
                moved = moveItemStackTo(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false);
            }
            if (!moved) {
                if (index < PLAYER_HOTBAR_START) {
                    moved = moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, total, false);
                } else {
                    moved = moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false);
                }
            }
            if (!moved) return ItemStack.EMPTY;
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

    public TileSmelter getBlockEntity() {
        return blockEntity;
    }

    public int getCookProgress() {
        return data.get(0);
    }

    public int getBurnTime() {
        return data.get(1);
    }

    public int getCurrentBurnTime() {
        return data.get(2);
    }

    public int getVis() {
        return data.get(3);
    }

    public int getSmeltTime() {
        return data.get(4);
    }

    public boolean isBurning() {
        return data.get(1) > 0;
    }

    public int getCookProgressScaled(int scale) {
        int smeltTime = getSmeltTime();
        if (smeltTime <= 0) smeltTime = 1;
        return getCookProgress() * scale / smeltTime;
    }

    public int getBurnTimeScaled(int scale) {
        int currentBurnTime = getCurrentBurnTime();
        if (currentBurnTime == 0) currentBurnTime = 200;
        return getBurnTime() * scale / currentBurnTime;
    }

    public int getVisScaled(int scale) {
        int max = TileSmelter.MAX_VIS;
        return getVis() * scale / max;
    }
}