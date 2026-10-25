package arcaneresearch.common.items.consumables;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
public class ItemChunksBeef extends Item {
    public ItemChunksBeef() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationMod(0.3f)
                        .build()));
    }
    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }
}
