package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.container.slot.SlotTurretBasic;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerTurretBasic extends AbstractContainerMenu {
    private EntityTurretCrossbow turret;
    private Player player;
    private Level theWorld;

    public ContainerTurretBasic(Inventory playerInventory, Level world, EntityTurretCrossbow turret) {
        super(ModMenuTypes.TURRET_BASIC.get(), containerIdOf(playerInventory));
        this.turret = turret;
        theWorld = world;
        player = playerInventory.player;
        addSlot(new SlotTurretBasic(turret, 0, 80, 29));
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
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index == 0) {
                if (!moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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

    public EntityTurretCrossbow getTurret() {
        return turret;
    }
}
