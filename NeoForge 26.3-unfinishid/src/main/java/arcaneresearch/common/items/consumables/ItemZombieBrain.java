package arcaneresearch.common.items.consumables;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;

public class ItemZombieBrain extends Item {

    public ItemZombieBrain(Item.Properties props) {
        super(props
                // NEOFORGE_PORT: FoodProperties.Builder saturationMod -> saturationModifier;
                // Builder.effect(...) is GONE, so the effect moves to the Consumable component
                // (Consumable.builder() defaults = 1.6s EAT, verified via bytecode).
                .food(new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.2f)

                        .build(),
                        Consumable.builder()
                                .onConsume(new ApplyStatusEffectsConsumeEffect(
                                        new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8f))
                                .build()));
    }

    // TODO_PORT (items): Forge .meat() flag dropped - 26.x FoodProperties has no meat flag, so
    // there is no equivalent to restore (Forge ref: .meat() on the builder).

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide() && entity instanceof Player player) {

            if (level.getRandom().nextFloat() < 0.1f) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
            } else {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1 + level.getRandom().nextInt(3), IPlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
