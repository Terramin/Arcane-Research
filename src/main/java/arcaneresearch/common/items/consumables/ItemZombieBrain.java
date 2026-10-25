package arcaneresearch.common.items.consumables;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
public class ItemZombieBrain extends Item {
    public ItemZombieBrain() {
        super(new Properties()
                .food(new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationMod(0.2f)
                        .meat()
                        .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 30, 0), 0.8f)
                        .build()));
    }
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (level.random.nextFloat() < 0.1f) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.NORMAL);
            } else {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(player, 1 + level.random.nextInt(3), IPlayerWarp.EnumWarpType.TEMPORARY);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }
}
