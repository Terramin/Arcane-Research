package arcaneresearch.common.container.slot;

import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;

@Deprecated
public class SlotTurretBasic extends SlotMobEquipment {
    public SlotTurretBasic(EntityTurretCrossbow turret, int slot, int x, int y) {
        super(turret, slot, x, y);
    }

    public static boolean isValidArrow(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ArrowItem;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isValidArrow(stack);
    }

    @Override
    public void setChanged() {
    }
}
