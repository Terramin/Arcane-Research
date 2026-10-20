package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.container.slot.SlotLimitedByClass;
import arcaneresearch.common.items.consumables.ItemBathSalts;
import arcaneresearch.common.tiles.devices.TileSpa;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerSpa extends AbstractContainerMenu {
    private TileSpa spa;
    private int lastBreakTime;

    public ContainerSpa(Inventory playerInventory, TileSpa tile) {
        super(ModMenuTypes.SPA.get(), containerIdOf(playerInventory));
        spa = tile;
        addSlot(new SlotLimitedByClass(ItemBathSalts.class, tile, 0, 65, 31));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 1) {
            spa.toggleMix();
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return spa.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {
                if (!spa.canPlaceItem(index, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!spa.canPlaceItem(index, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
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

    public TileSpa getBlockEntity() {
        return spa;
    }
}
