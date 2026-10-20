package arcaneresearch.common.container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.container.slot.SlotPotion;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;
import arcaneresearch.init.ModMenuTypes;
@Deprecated
public class ContainerPotionSprayer extends AbstractContainerMenu {
    private TilePotionSprayer sprayer;
    private int lastBreakTime;
    public ContainerPotionSprayer(Inventory par1InventoryPlayer, TilePotionSprayer tilePotionSprayer) {
        super(ModMenuTypes.POTION_SPRAYER.get(), containerIdOf(par1InventoryPlayer));
        sprayer = tilePotionSprayer;
        addSlot(new SlotPotion(sprayer, 0, 56, 64));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(par1InventoryPlayer, j + i * 9 + 9, 16 + j * 18, 151 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(par1InventoryPlayer, i, 16 + i * 18, 209));
        }
    }
    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }
    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        return sprayer.stillValid(par1EntityPlayer);
    }
    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot == 0) {
                if (!sprayer.canPlaceItem(0, stackInSlot) || !moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!sprayer.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return stack;
    }
    public TilePotionSprayer getSprayer() {
        return sprayer;
    }
}
