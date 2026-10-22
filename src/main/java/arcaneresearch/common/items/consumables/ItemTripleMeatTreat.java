package arcaneresearch.common.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ItemTripleMeatTreat extends Item {

    public ItemTripleMeatTreat(Item.Properties props) {
        super(props
                
                
                
                
                
                .food(new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationModifier(0.8f)
                        .alwaysEdible()
                        .build(),
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 0.66f))
                                .build()));
    }

    
    
}
