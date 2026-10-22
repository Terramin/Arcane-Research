package arcaneresearch.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.menu.slot.AspectSlot;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModMenuTypes;

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
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        this.data = new SimpleContainerData(5);
        addDataSlots(this.data);

        addSlot(new AspectSlot(blockEntity, TileSmelter.SLOT_INPUT, 80, 8));

        addSlot(new Slot(blockEntity, TileSmelter.SLOT_FUEL, 80, 48) {
            @Override
            public boolean mayPlace(ItemStack stack) {
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
        if (be instanceof TileSmelter smelter) {
            return smelter;
        }
        throw new IllegalStateException("Block entity is not a TileSmelter");
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        data.set(0, blockEntity.furnaceCookTime);
        data.set(1, blockEntity.furnaceBurnTime);
        data.set(2, blockEntity.currentItemBurnTime);
        data.set(3, blockEntity.vis);
        data.set(4, blockEntity.smeltTime);
    }

    @Override
    public boolean stillValid(Player player) {
        
        
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == INPUT_SLOT || index == FUEL_SLOT) {
                if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INV_START && index <= PLAYER_HOTBAR_END) {

                if (TileSmelter.isItemFuel(stackInSlot)) {

                    if (!moveItemStackTo(stackInSlot, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        if (!moveItemStackTo(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false)) {

                            if (index < PLAYER_HOTBAR_START) {
                                if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END + 1, false)) {
                                    return ItemStack.EMPTY;
                                }
                            } else {
                                if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                                    return ItemStack.EMPTY;
                                }
                            }
                        }
                    }
                } else {

                    if (!moveItemStackTo(stackInSlot, INPUT_SLOT, INPUT_SLOT + 1, false)) {

                        if (index < PLAYER_HOTBAR_START) {
                            if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END + 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
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
        return getVis() * scale / TileSmelter.MAX_VIS;
    }
}
