package arcaneresearch.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MobEquipmentSlot extends Slot {

    protected final Mob entity;
    protected final InteractionHand hand;

    public MobEquipmentSlot(Mob entity, int slotIndex, int x, int y) {
        this(entity, slotIndex, x, y, InteractionHand.MAIN_HAND);
    }

    public MobEquipmentSlot(Mob entity, int slotIndex, int x, int y, InteractionHand hand) {
        super(new DummyContainer(), slotIndex, x, y);
        this.entity = entity;
        this.hand = hand;
    }

    @Override
    public @NotNull ItemStack getItem() {
        return entity.getItemInHand(hand);
    }

    @Override
    public void set(@NotNull ItemStack stack) {

        int max = Math.max(1, getMaxStackSize());
        if (!stack.isEmpty() && stack.getCount() > max) {
            stack.setCount(max);
        }
        entity.setItemInHand(hand, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        if (amount <= 0) return ItemStack.EMPTY;

        ItemStack current = getItem();
        if (current.isEmpty()) return ItemStack.EMPTY;

        if (current.getCount() <= amount) {
            ItemStack result = current.copy();
            set(ItemStack.EMPTY);
            return result;
        }

        ItemStack result = current.copyWithCount(amount);
        ItemStack remaining = current.copy();
        remaining.shrink(amount);
        set(remaining);
        return result;
    }

    @Override
    public boolean hasItem() {
        return !getItem().isEmpty();
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return true;
    }

    private static class DummyContainer implements Container {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public @NotNull ItemStack getItem(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }
    }
}