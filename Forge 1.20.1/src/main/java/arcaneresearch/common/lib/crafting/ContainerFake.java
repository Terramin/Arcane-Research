package arcaneresearch.common.lib.crafting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
public class ContainerFake extends AbstractContainerMenu {
    public ContainerFake(int windowId, Inventory playerInventory) {
        super(null, windowId);
    }
    public ContainerFake(int windowId) {
        super(null, windowId);
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
