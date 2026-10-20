package arcaneresearch.common.container;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.config.ConfigEntities;
import arcaneresearch.common.container.slot.SlotOutput;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;

@Deprecated
public class ContainerPech extends AbstractContainerMenu {
    private EntityPech pech;
    private InventoryPech inventory;
    private Player player;
    private Level theWorld;

    public ContainerPech(Inventory par1InventoryPlayer, Level par3World, EntityPech par2IMerchant) {
        super(ModMenuTypes.PECH_TRADING.get(), containerIdOf(par1InventoryPlayer));
        pech = par2IMerchant;
        theWorld = par3World;
        player = par1InventoryPlayer.player;
        inventory = new InventoryPech(par1InventoryPlayer.player, par2IMerchant);
        pech.trading = true;
        addSlot(new Slot(inventory, 0, 36, 29));
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                addSlot(new SlotOutput(inventory, 1 + j + i * 2, 106 + 18 * j, 20 + 18 * i));
            }
        }
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

    public InventoryPech getMerchantInventory() {
        return inventory;
    }

    // NEOFORGE_PORT: ContainerListener removed; plain callback kept.
    public void containerChanged(Container invBasic) {

    }

    @Override
    public boolean clickMenuButton(Player par1EntityPlayer, int par2) {
        if (par2 == 0) {
            generateContents();
            return true;
        }
        return super.clickMenuButton(par1EntityPlayer, par2);
    }

    private boolean hasStuffInPack() {
        for (ItemStack stack : pech.loot) {
            if (!stack.isEmpty() && stack.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void generateContents() {
        if (player.level().isClientSide()) {
            return;
        }
        ItemStack input = inventory.getItem(0);
        if (input.isEmpty()) {
            return;
        }
        boolean outputsEmpty = true;
        for (int i = 1; i <= 4; i++) {
            if (!inventory.getItem(i).isEmpty()) {
                outputsEmpty = false;
                break;
            }
        }
        if (!outputsEmpty) {
            return;
        }
        if (!pech.isValued(input)) {
            return;
        }
        RandomSource rand = player.level().getRandom();
        int value = pech.getValue(input);
        if (rand.nextInt(100) <= value / 2) {
            pech.setTamed(false);
            pech.playSound(ModSounds.PECH_TRADE.get(), 0.4f, 1.0f);
        }
        if (rand.nextInt(5) == 0) {
            value += rand.nextInt(3);
        } else if (rand.nextBoolean()) {
            value -= rand.nextInt(3);
        }
        java.util.List<ConfigEntities.TradeEntry> table = ConfigEntities.tradeInventory.get(pech.getPechType());
        if (table == null || table.isEmpty()) {
            return;
        }
        while (value > 0) {
            int am = Math.min(5, Math.max((value + 1) / 2, rand.nextInt(value) + 1));
            value -= am;
            if (am == 1 && rand.nextBoolean() && hasStuffInPack()) {
                ArrayList<Integer> loot = new ArrayList<Integer>();
                for (int a = 0; a < pech.loot.size(); ++a) {
                    if (!pech.loot.get(a).isEmpty() && pech.loot.get(a).getCount() > 0) {
                        loot.add(a);
                    }
                }
                int r = loot.get(rand.nextInt(loot.size()));
                ItemStack is = pech.loot.get(r).copy();
                is.setCount(1);
                addStack(is);
                pech.loot.get(r).shrink(1);
                if (pech.loot.get(r).getCount() <= 0) {
                    pech.loot.set(r, ItemStack.EMPTY);
                }
            } else {
                if (am >= 4 && rand.nextBoolean()) {
                    continue;
                }
                ConfigEntities.TradeEntry pick = null;
                do {
                    pick = table.get(rand.nextInt(table.size()));
                } while (pick.cost() != am);
                addStack(pick.stack().copy());
            }
        }
        inventory.removeItem(0, 1);
        broadcastChanges();
    }

    private void addStack(ItemStack s) {
        for (int a = 1; a < 5; ++a) {
            if (inventory.getItem(a).isEmpty()) {
                inventory.setItem(a, s);
                break;
            }
            if (ItemStack.isSameItemSameComponents(inventory.getItem(a), s)
                    && inventory.getItem(a).getCount() + s.getCount() < inventory.getItem(a).getMaxStackSize()) {
                inventory.getItem(a).grow(s.getCount());
            }
        }
    }

    @Override
    public boolean stillValid(Player par1EntityPlayer) {
        return pech.isTamed();
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int par2) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = slots.get(par2);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack2 = slot.getItem();
            itemstack = itemstack2.copy();
            if (par2 == 0) {
                if (!moveItemStackTo(itemstack2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (par2 >= 1 && par2 < 5) {
                if (!moveItemStackTo(itemstack2, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (par2 != 0 && par2 >= 5 && par2 < 41 && !moveItemStackTo(itemstack2, 0, 1, true)) {
                return ItemStack.EMPTY;
            }
            if (itemstack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
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

    @Override
    public void removed(Player par1EntityPlayer) {
        super.removed(par1EntityPlayer);
        pech.trading = false;
        if (!par1EntityPlayer.level().isClientSide()) {
            for (int a = 0; a < 5; ++a) {
                ItemStack itemstack = inventory.removeItemNoUpdate(a);
                if (!itemstack.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity dropped = par1EntityPlayer.drop(itemstack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                    if (dropped != null) {
                        dropped.getPersistentData().putBoolean("PechDrop", true);
                    }
                }
            }
        }
    }

    public EntityPech getPech() {
        return pech;
    }
}
