package thaumictinker.gear;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.items.IScribeTools;

 



 
public class InfusedInkwellItem extends Item implements IScribeTools {

    public static final int USES = 800;

    public InfusedInkwellItem(Properties properties) {
        super(properties.durability(USES));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
