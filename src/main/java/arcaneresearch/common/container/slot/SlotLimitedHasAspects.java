package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
@Deprecated
public class SlotLimitedHasAspects extends Slot {
    public SlotLimitedHasAspects(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }
    public static boolean hasAspects(ItemStack stack) {
        AspectList tags = ArcaneresearchCraftingManager.getObjectTags(stack);
        return tags != null && tags.size() > 0;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return hasAspects(stack);
    }
}
