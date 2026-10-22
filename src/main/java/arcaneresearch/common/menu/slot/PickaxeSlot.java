package arcaneresearch.common.menu.slot;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public class PickaxeSlot extends MobEquipmentSlot {

    public PickaxeSlot(Mob entity, int slotIndex, int x, int y) {
        super(entity, slotIndex, x, y);
    }

    public static boolean isValidPickaxe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        
        
        if (stack.is(ItemTags.PICKAXES)) {
            return true;
        }

        return stack.isCorrectToolForDrops(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidPickaxe(stack);
    }
}
