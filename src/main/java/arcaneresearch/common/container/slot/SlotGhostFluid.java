package arcaneresearch.common.container.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;

@Deprecated
public class SlotGhostFluid extends SlotGhost {
    public SlotGhostFluid(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        
        
        return !stack.isEmpty()
                && net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack)
                        .getCapability(Capabilities.Fluid.ITEM) != null;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
