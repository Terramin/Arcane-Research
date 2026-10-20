package arcaneresearch.common.items.consumables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemChunksBeef extends Item {

    public ItemChunksBeef(Item.Properties props) {

        super(props
                // NEOFORGE_PORT: FoodProperties.Builder saturationMod -> saturationModifier.
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.3f)
                        .build()));
    }

    // NEOFORGE_PORT: getUseDuration now takes (ItemStack, LivingEntity).
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {

        return 10;
    }
}
