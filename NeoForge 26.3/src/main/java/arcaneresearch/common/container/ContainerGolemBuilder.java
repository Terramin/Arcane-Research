package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.container.slot.SlotOutput;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerGolemBuilder extends AbstractContainerMenu {
    public static boolean redo = false;
    private final TileGolemBuilder builder;
    private final ContainerData data;

    public ContainerGolemBuilder(Inventory playerInventory, TileGolemBuilder tileEntity) {
        super(ModMenuTypes.GOLEM_BUILDER.get(), containerIdOf(playerInventory));
        builder = tileEntity;
        data = new SimpleContainerData(2);
        addDataSlots(data);
        addSlot(new SlotOutput(tileEntity, 0, 160, 104));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 24 + j * 18, 142 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 24 + i * 18, 200));
        }
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        redo = true;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        redo = true;
        super.setItem(slotId, stateId, stack);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 99) {
            redo = true;
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        data.set(0, builder.cost);
        data.set(1, builder.maxCost);
    }

    @Override
    public boolean stillValid(Player player) {
        return builder.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (slotIndex == 0) {
                if (!builder.canPlaceItem(slotIndex, stackInSlot)
                        || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!builder.canPlaceItem(slotIndex, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    public TileGolemBuilder getBlockEntity() {
        return builder;
    }

    public int getCost() {
        return data.get(0);
    }

    public int getMaxCost() {
        return data.get(1);
    }
}
