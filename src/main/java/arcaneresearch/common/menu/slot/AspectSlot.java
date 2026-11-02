package arcaneresearch.common.menu.slot;

import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AspectSlot extends Slot {

    public AspectSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public static boolean hasAspects(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof IEssentiaContainerItem container) {
            var aspects = container.getAspects(stack);
            return aspects != null && aspects.size() > 0;
        }

        try {
            var aspects = ArcaneresearchCraftingManager.getObjectTags(stack);
            return aspects.size() > 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return hasAspects(stack);
    }
}