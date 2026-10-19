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
                // NEOFORGE_PORT: FoodProperties.Builder saturationMod -> saturationModifier,
                // alwaysEat -> alwaysEdible; MobEffectInstance now takes Holder<MobEffect> and
                // MobEffects fields already are holders, so the effect itself is unchanged - but
                // Builder.effect(...) is GONE, so the effect moves to the Consumable component
                // (Consumable.builder() defaults = 1.6s EAT, verified via bytecode).
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

    // TODO_PORT (items): Forge .meat() flag dropped - 26.x FoodProperties has no meat flag, so
    // there is no equivalent to restore (Forge ref: .meat() on the builder).
}
