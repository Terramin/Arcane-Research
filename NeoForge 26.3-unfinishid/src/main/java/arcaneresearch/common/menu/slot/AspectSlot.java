package arcaneresearch.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;

public class AspectSlot extends Slot {

    public AspectSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    public static boolean hasAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof IEssentiaContainerItem container) {
            var aspects = container.getAspects(stack);
            return aspects != null && aspects.size() > 0;
        }

        var aspects = ArcaneresearchCraftingManager.getObjectTags(stack);
        return aspects != null && aspects.size() > 0;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return hasAspects(stack);
    }
}
