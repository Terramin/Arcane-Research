package arcaneresearch.api.items;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public interface IRechargable {
    int getMaxCharge(ItemStack stack, LivingEntity entity);
    EnumChargeDisplay showInHud(ItemStack stack, LivingEntity entity);
    enum EnumChargeDisplay {
        NEVER,
        NORMAL,
        PERIODIC
    }
}
