package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
@Deprecated
public class SlotCrystal extends Slot {
    protected Aspect aspect;
    public SlotCrystal(Aspect aspect, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.aspect = aspect;
    }
    public static boolean isValidCrystal(ItemStack stack, Aspect aspect) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ItemCrystalEssence crystalItem) {
            var aspects = crystalItem.getAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                Aspect[] contained = aspects.getAspects();
                return contained != null && contained.length > 0 && contained[0] == aspect;
            }
        }
        return false;
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidCrystal(stack, aspect);
    }
}
