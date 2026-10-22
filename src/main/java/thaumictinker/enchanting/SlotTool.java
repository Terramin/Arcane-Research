package thaumictinker.enchanting;

import javax.annotation.Nullable;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

 

 
public class SlotTool extends Slot {

    private final @Nullable EnchanterBlockEntity enchanter;

    public SlotTool(Container container, @Nullable EnchanterBlockEntity enchanter, int slot, int x, int y) {
        super(container, slot, x, y);
        this.enchanter = enchanter;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return (enchanter == null || !enchanter.isWorking()) && EnchantmentManager.isTool(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPickup(Player player) {
        return enchanter == null || !enchanter.isWorking();
    }
}
