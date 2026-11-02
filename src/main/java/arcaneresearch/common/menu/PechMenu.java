package arcaneresearch.common.menu;

import arcaneresearch.common.config.ConfigEntities;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.common.menu.slot.OutputSlot;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PechMenu extends AbstractContainerMenu implements ContainerListener {

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

        this.tradeInventory = new SimpleContainer(5);
        tradeInventory.addListener(this);

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
        if (entity instanceof EntityPech pechEntity) return pechEntity;
        throw new IllegalStateException("Entity " + entityId + " is not a Pech: " + entity);
    }

    @Override
    public void containerChanged(@NotNull Container container) {
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int button) {
        if (button != 0) return false;
        generateTradeResults();
        return true;
    }

    private void generateTradeResults() {
        if (player.level().isClientSide()) return;

        ItemStack input = tradeInventory.getItem(0);
        if (input.isEmpty()) return;

        for (int i = 1; i <= 4; i++) {
            if (!tradeInventory.getItem(i).isEmpty()) return;
        }

        if (!pech.isValued(input)) return;

        RandomSource rand = player.level().random;
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

        List<ConfigEntities.TradeEntry> table = ConfigEntities.tradeInventory.get(pech.getPechType());
        if (table == null || table.isEmpty()) return;

        final List<ItemStack> loot = pech.loot;

        while (value > 0) {
            int am = Math.min(5, Math.max((value + 1) / 2, rand.nextInt(value) + 1));
            value -= am;

            if (am == 1 && rand.nextBoolean() && hasStuffInPack()) {
                ArrayList<Integer> indices = new ArrayList<>();
                for (int a = 0; a < loot.size(); a++) {
                    ItemStack s = loot.get(a);
                    if (!s.isEmpty() && s.getCount() > 0) indices.add(a);
                }
                if (indices.isEmpty()) continue;

                int r = indices.get(rand.nextInt(indices.size()));
                ItemStack is = loot.get(r).copy();
                is.setCount(1);
                addStack(is);
                loot.get(r).shrink(1);
                if (loot.get(r).getCount() <= 0) {
                    loot.set(r, ItemStack.EMPTY);
                }
            } else {
                if (am >= 4 && rand.nextBoolean()) continue;
                ConfigEntities.TradeEntry pick = pickRandomByCost(table, am, rand);
                if (pick == null) continue;
                addStack(pick.stack().copy());
            }
        }

        tradeInventory.removeItem(0, 1);
        broadcastChanges();
    }

    private static ConfigEntities.TradeEntry pickRandomByCost(
            List<ConfigEntities.TradeEntry> table, int cost, RandomSource rand) {
        int count = 0;
        for (ConfigEntities.TradeEntry e : table) {
            if (e.cost() == cost) count++;
        }
        if (count == 0) return null;

        int target = rand.nextInt(count);
        int idx = 0;
        for (ConfigEntities.TradeEntry e : table) {
            if (e.cost() == cost && idx++ == target) return e;
        }
        return null;
    }

    private boolean hasStuffInPack() {
        List<ItemStack> loot = pech.loot;
        if (loot == null) return false;
        for (ItemStack stack : loot) {
            if (stack != null && !stack.isEmpty() && stack.getCount() > 0) return true;
        }
        return false;
    }

    private void addStack(ItemStack s) {
        for (int a = 1; a < 5; a++) {
            ItemStack existing = tradeInventory.getItem(a);
            if (existing.isEmpty()) {
                tradeInventory.setItem(a, s);
                return;
            }
            if (ItemStack.isSameItemSameTags(existing, s)
                    && existing.getCount() + s.getCount() < existing.getMaxStackSize()) {
                existing.grow(s.getCount());
                return;
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return pech != null && pech.isAlive() && pech.isTamed() && pech.distanceTo(player) < 8.0f;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        boolean moved;
        if (index < 5) {
            moved = moveItemStackTo(stackInSlot, 5, total, true);
        } else {
            moved = moveItemStackTo(stackInSlot, 0, 1, false);
        }
        if (!moved) return ItemStack.EMPTY;

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        if (pech != null) pech.trading = false;
        if (player.level().isClientSide()) return;

        for (int i = 0; i < 5; i++) {
            ItemStack stack = tradeInventory.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                ItemEntity dropped = player.drop(stack, false);
                if (dropped != null) {
                    dropped.getPersistentData().putBoolean("PechDrop", true);
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