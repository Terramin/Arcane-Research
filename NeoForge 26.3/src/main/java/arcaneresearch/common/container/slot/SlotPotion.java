package arcaneresearch.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;

@Deprecated
public class SlotPotion extends Slot {
    protected int limit;

    public SlotPotion(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limit = 64;
    }

    public SlotPotion(int limit, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limit = 64;
        this.limit = limit;
    }

    public static boolean isValidPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() != Items.POTION && stack.getItem() != Items.SPLASH_POTION
                && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        try {
            // NEOFORGE_PORT: PotionUtils removed -> PotionContents component (holders compared).
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            var potion = contents != null ? contents.potion().orElse(null) : null;
            return potion != null && !potion.equals(net.minecraft.world.item.alchemy.Potions.WATER)
                    && !potion.equals(net.minecraft.world.item.alchemy.Potions.AWKWARD)
                    && !potion.equals(net.minecraft.world.item.alchemy.Potions.MUNDANE)
                    && !potion.equals(net.minecraft.world.item.alchemy.Potions.THICK);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidPotion(stack);
    }

    @Override
    public int getMaxStackSize() {
        return limit;
    }
}
