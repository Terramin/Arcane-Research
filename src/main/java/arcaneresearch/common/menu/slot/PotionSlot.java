package arcaneresearch.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

public class PotionSlot extends Slot {

    private final int limit;

    public PotionSlot(Container container, int slot, int x, int y) {
        this(container, slot, x, y, 64);
    }

    public PotionSlot(Container container, int slot, int x, int y, int limit) {
        super(container, slot, x, y);
        this.limit = Math.max(1, limit);
    }

    public static boolean isValidPotion(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() != Items.POTION
                && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }

        Potion potion;
        try {
            potion = PotionUtils.getPotion(stack);
        } catch (RuntimeException e) {
            return false;
        }

        return potion != Potions.WATER
                && potion != Potions.MUNDANE
                && potion != Potions.THICK
                && potion != Potions.AWKWARD
                && potion != Potions.EMPTY;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return isValidPotion(stack);
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }
}