package arcaneresearch.common.items.consumables;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
public class ItemTripleMeatTreat extends Item {
    public ItemTripleMeatTreat() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationMod(0.8f)
                        .meat()
                        .alwaysEat()
                        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66f)
                        .build()));
    }
}
