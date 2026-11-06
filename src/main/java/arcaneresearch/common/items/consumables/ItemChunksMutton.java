package arcaneresearch.common.items.consumables;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemChunksMutton extends Item {
    public ItemChunksMutton() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationMod(0.3f)
                        .build()));
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 10;
    }
}
