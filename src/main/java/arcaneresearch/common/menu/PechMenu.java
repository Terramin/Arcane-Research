package arcaneresearch.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.common.menu.slot.OutputSlot;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModSounds;

public class PechMenu extends AbstractContainerMenu    {

    private final EntityPech pech;
    private final Player player;
    private final SimpleContainer tradeInventory;

    public PechMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getEntity(playerInventory, extraData));
    }

    public PechMenu(int containerId, Inventory playerInventory, EntityPech pech) {
        super(ModMenuTypes.PECH_TRADING.get(), containerId);
        this.pech = pech;
        this.player = playerInventory.player;

        pech.trading = true;

        this.tradeInventory = new SimpleContainer(5) {
            
            
            
            @Override
            public void setChanged() {
                super.setChanged();
                PechMenu.this.containerChanged(this);
            }
        };

        addSlot(new Slot(tradeInventory, 0, 36, 29));

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                addSlot(new OutputSlot(tradeInventory, 1 + col + row * 2, 106 + col * 18, 20 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static EntityPech getEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        int entityId = extraData.readInt();
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof EntityPech pechEntity) {
            return pechEntity;
        }
        throw new IllegalStateException("Entity is not a Pech");
    }

    

    public void containerChanged(Container container) {
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 0) {

            generateTradeResults();
            return true;
        }
        return false;
    }

    private void generateTradeResults() {
        if (player.level().isClientSide()) return;

        ItemStack input = tradeInventory.getItem(0);
        if (input.isEmpty()) return;

        boolean outputsEmpty = true;
        for (int i = 1; i <= 4; i++) {
            if (!tradeInventory.getItem(i).isEmpty()) {
                outputsEmpty = false;
                break;
            }
        }
        if (!outputsEmpty) return;

        if (!pech.isValued(input)) return;

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
        java.util.List<arcaneresearch.common.config.ConfigEntities.TradeEntry> table =
                arcaneresearch.common.config.ConfigEntities.tradeInventory.get(pech.getPechType());
        if (table == null || table.isEmpty()) return;
        while (value > 0) {
            int am = Math.min(5, Math.max((value + 1) / 2, rand.nextInt(value) + 1));
            value -= am;
            if (am == 1 && rand.nextBoolean() && hasStuffInPack()) {
                java.util.ArrayList<Integer> loot = new java.util.ArrayList<>();
                for (int a = 0; a < pech.loot.size(); a++) {
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
                arcaneresearch.common.config.ConfigEntities.TradeEntry pick = null;
                do {
                    pick = table.get(rand.nextInt(table.size()));
                } while (pick.cost() != am);
                addStack(pick.stack().copy());
            }
        }

        tradeInventory.removeItem(0, 1);

        broadcastChanges();
    }

    private boolean hasStuffInPack() {
        for (ItemStack stack : pech.loot) {
            if (!stack.isEmpty() && stack.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    private void addStack(ItemStack s) {
        for (int a = 1; a < 5; a++) {
            if (tradeInventory.getItem(a).isEmpty()) {
                tradeInventory.setItem(a, s);
                break;
            }
            if (ItemStack.isSameItemSameComponents(tradeInventory.getItem(a), s)
                    && tradeInventory.getItem(a).getCount() + s.getCount()
                    < tradeInventory.getItem(a).getMaxStackSize()) {
                tradeInventory.getItem(a).grow(s.getCount());
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return pech.isAlive() && pech.isTamed() && pech.distanceTo(player) < 8.0f;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index == 0) {

                if (!moveItemStackTo(stackInSlot, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index < 5) {

                if (!moveItemStackTo(stackInSlot, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 5 && index < 41) {

                if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        pech.trading = false;

        if (!player.level().isClientSide()) {
            for (int i = 0; i < 5; i++) {
                ItemStack stack = tradeInventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    net.minecraft.world.entity.item.ItemEntity dropped = player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
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

    public Container getTradeInventory() {
        return tradeInventory;
    }
}
