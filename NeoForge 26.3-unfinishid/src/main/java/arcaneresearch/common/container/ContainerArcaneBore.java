package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.container.slot.SlotArcaneBorePickaxe;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerArcaneBore extends AbstractContainerMenu {
    private EntityArcaneBore turret;
    private Player player;
    private Level theWorld;

    public ContainerArcaneBore(Inventory playerInventory, Level world, EntityArcaneBore bore) {
        super(ModMenuTypes.ARCANE_BORE.get(), containerIdOf(playerInventory));
        turret = bore;
        theWorld = world;
        player = playerInventory.player;
        addSlot(new SlotArcaneBorePickaxe(turret, 0, 80, 29));
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
        return button == 0 || super.clickMenuButton(player, button);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.setByPlayer(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return stack;
    }

    public EntityArcaneBore getBore() {
        return turret;
    }
}
