package arcaneresearch.common.menu.slot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.tiles.crafting.DeconstructTile;
public class DeconstructSlot extends Slot {
    public DeconstructSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return DeconstructTile.hasAspects(stack);
    }
}
