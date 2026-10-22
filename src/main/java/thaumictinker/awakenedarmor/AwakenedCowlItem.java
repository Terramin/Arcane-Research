package thaumictinker.awakenedarmor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;

 



 
public class AwakenedCowlItem extends AwakenedArmorItem implements IRevealer, IGoggles {

    public AwakenedCowlItem(Properties properties, int visDiscount) {
        super(Type.HELMET, properties, visDiscount);
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity entity) {
        return true;
    }
}
