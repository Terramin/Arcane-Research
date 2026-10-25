package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
                && stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }
    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
