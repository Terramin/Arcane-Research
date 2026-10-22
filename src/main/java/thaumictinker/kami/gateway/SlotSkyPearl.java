package thaumictinker.kami.gateway;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

 

 
public class SlotSkyPearl extends Slot {

    public SlotSkyPearl(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
