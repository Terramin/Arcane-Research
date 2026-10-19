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

        // NEOFORGE_PORT: PickaxeItem class removed (tools are tag/components now).
        // The tag check below already covered modded pickaxes; kept as the test.
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
