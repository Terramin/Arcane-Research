package thaumictinker.enchanting;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

 

 
public class SpellClothItem extends Item {

    public static final int USES = 35;

    public SpellClothItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
