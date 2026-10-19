package arcaneresearch.common.menu.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
public class CrystalSlot extends Slot {
    private final Aspect requiredAspect;
    public CrystalSlot(Aspect aspect, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.requiredAspect = aspect;
    }
    public static boolean isValidCrystal(ItemStack stack, Aspect aspect) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof IEssentiaContainerItem crystalItem) {
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
        return isValidCrystal(stack, requiredAspect);
    }
    public Aspect getRequiredAspect() {
        return requiredAspect;
    }
}
