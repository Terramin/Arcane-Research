package arcaneresearch.common.items.consumables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemChunksPork extends Item {

    public ItemChunksPork(Item.Properties props) {

        super(props
                
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.3f)
                        .build()));
    }

    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {

        return 10;
    }
}
