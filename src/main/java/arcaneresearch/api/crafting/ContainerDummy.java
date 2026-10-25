package arcaneresearch.api.crafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public class ContainerDummy extends AbstractContainerMenu {
    public ContainerDummy(int containerId) {
        super(null, containerId);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
