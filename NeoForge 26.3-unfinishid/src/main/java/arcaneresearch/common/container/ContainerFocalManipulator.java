package arcaneresearch.common.container;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.container.slot.SlotFocus;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModSounds;

@Deprecated
public class ContainerFocalManipulator extends AbstractContainerMenu {
    private TileFocalManipulator table;
    private int lastBreakTime;

    public ContainerFocalManipulator(Inventory playerInventory, TileFocalManipulator tile) {
        super(ModMenuTypes.FOCAL_MANIPULATOR.get(), containerIdOf(playerInventory));
        table = tile;
        addSlot(new SlotFocus(tile, 0, 31, 191));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, i * 18 - 62, 64 + j * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new Slot(playerInventory, i + j * 3, i * 18 - 62, j * 18 + 7));
            }
        }
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 0) {
            if (!table.startCraft(button, player)) {
                player.level().playSound(player, table.getBlockPos(),
                        ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
            }
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return table.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {
                if (!moveItemStackTo(stackInSlot, 1, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof ItemFocus) {
                if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index < 28) {
                if (!moveItemStackTo(stackInSlot, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 28 && index < 37) {
                if (!moveItemStackTo(stackInSlot, 1, 28, false)) {
                    return ItemStack.EMPTY;
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

    public TileFocalManipulator getBlockEntity() {
        return table;
    }
}
