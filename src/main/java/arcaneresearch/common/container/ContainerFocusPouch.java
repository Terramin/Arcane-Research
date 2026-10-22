package arcaneresearch.common.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput; 
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.container.slot.SlotLimitedByClass;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.casters.ItemFocusPouch;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerFocusPouch extends AbstractContainerMenu {
    public Container input;
    ItemStack pouch;
    Player player;
    private Level worldObj;
    private int posX;
    private int posY;
    private int posZ;
    private int blockSlot;

    public ContainerFocusPouch(Inventory playerInventory, Level world, int x, int y, int z) {
        super(ModMenuTypes.FOCUS_POUCH.get(), containerIdOf(playerInventory));
        input = new InventoryFocusPouch();
        pouch = ItemStack.EMPTY;
        player = null;
        worldObj = world;
        posX = x;
        posY = y;
        posZ = z;
        player = playerInventory.player;
        pouch = playerInventory.getSelectedItem();
        blockSlot = playerInventory.getSelectedSlot() + 45;
        for (int a = 0; a < 18; ++a) {
            addSlot(new SlotLimitedByClass(ItemFocus.class, input, a, 37 + a % 6 * 18, 51 + a / 6 * 18));
        }
        bindPlayerInventory(playerInventory);
        if (!world.isClientSide()) {
            try {
                NonNullList<ItemStack> list = ((ItemFocusPouch) pouch.getItem()).getInventory(pouch);
                for (int a = 0; a < list.size(); ++a) {
                    input.setItem(a, list.get(a));
                }
            } catch (Exception e) {
            }
        }
        slotsChanged(input);
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    
    public void containerChanged(Container container) {
        broadcastChanges();
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 151 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 209));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        if (slot == blockSlot) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot < 18) {
                if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 18, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 18, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (slotId == blockSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(Player par1EntityPlayer) {
        super.removed(par1EntityPlayer);
        if (!worldObj.isClientSide()) {
            NonNullList<ItemStack> list = NonNullList.withSize(18, ItemStack.EMPTY);
            for (int a = 0; a < list.size(); ++a) {
                list.set(a, input.getItem(a));
            }
            if (pouch.getItem() instanceof ItemFocusPouch) {
                ((ItemFocusPouch) pouch.getItem()).setInventory(pouch, list);
            }
            if (player == null) {
                return;
            }
            if (ItemStack.isSameItem(player.getItemInHand(player.getUsedItemHand()), pouch)) {
                player.setItemInHand(player.getUsedItemHand(), pouch);
            }
            player.getInventory().setChanged();
        }
    }
}
