package arcaneresearch.common.menu.slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.wands.IWand;
public class SlotLimitedByWand extends Slot {
    int limit = 64;
    public SlotLimitedByWand(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IWand wand && !wand.isStaff(stack);
    }
    @Override
    public int getMaxStackSize() {
        return this.limit;
    }
    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
    }
}
