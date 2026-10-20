package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.container.slot.SlotLimitedHasAspects;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;

@Deprecated
public class ContainerSmelter extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;
    private TileSmelter furnace;

    public ContainerSmelter(Inventory par1InventoryPlayer, TileSmelter tileEntity) {
        super(ModMenuTypes.SMELTER.get(), containerIdOf(par1InventoryPlayer));
        furnace = tileEntity;
        this.access = ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos());
        this.data = new SimpleContainerData(5);
        addDataSlots(this.data);
        addSlot(new SlotLimitedHasAspects(tileEntity, 0, 80, 8));
        addSlot(new Slot(tileEntity, 1, 80, 48));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(par1InventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(par1InventoryPlayer, i, 8 + i * 18, 142));
        }
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        data.set(0, furnace.furnaceCookTime);
        data.set(1, furnace.furnaceBurnTime);
        data.set(2, furnace.currentItemBurnTime);
        data.set(3, furnace.vis);
        data.set(4, furnace.smeltTime);
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        return stillValid(access, par1EntityPlayer, ModBlocks.SMELTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(par2);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (par2 != 1 && par2 != 0) {
                AspectList al = ArcaneresearchCraftingManager.getObjectTags(itemstack2);
                if (TileSmelter.isItemFuel(itemstack2)) {
                    if (!moveItemStackTo(itemstack2, 1, 2, false) && !moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (al != null && al.size() > 0) {
                    if (!moveItemStackTo(itemstack2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 >= 2 && par2 < 29) {
                    if (!moveItemStackTo(itemstack2, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (par2 >= 29 && par2 < 38 && !moveItemStackTo(itemstack2, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(itemstack2, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack2.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(par1EntityPlayer, itemstack2);
        }
        return itemstack;
    }

    public TileSmelter getBlockEntity() {
        return furnace;
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
}
