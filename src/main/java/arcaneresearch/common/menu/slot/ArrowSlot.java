package arcaneresearch.common.menu.slot;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;

public class ArrowSlot extends MobEquipmentSlot {

    public ArrowSlot(Mob entity, int slotIndex, int x, int y) {
        super(entity, slotIndex, x, y);
    }

    public static boolean isValidArrow(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ArrowItem;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidArrow(stack);
    }
}
