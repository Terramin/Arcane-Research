package arcaneresearch.common.items.tools.voidmetal;

import arcaneresearch.init.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface VoidTool {

    static Item.Properties createProperties(Item.Properties props) {
        return props;
    }

    static boolean canRepair(ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get());
    }

    static void repairOverTime(ItemStack stack, Entity entity) {
        if (stack.isDamaged() && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    static boolean isPvPEnabled(Level level) {
        return !(level instanceof net.minecraft.server.level.ServerLevel serverLevel) || serverLevel.isPvpAllowed();
    }
}
