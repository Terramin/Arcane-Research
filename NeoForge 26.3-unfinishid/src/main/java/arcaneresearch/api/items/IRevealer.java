package arcaneresearch.api.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Deprecated
public interface IRevealer {

    boolean showNodes(ItemStack itemstack, LivingEntity player);
}
