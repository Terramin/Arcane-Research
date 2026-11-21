package arcaneresearch.common.items.tools;

import arcaneresearch.api.crit.CritTooltips;
import arcaneresearch.api.items.ICriticalGear;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.api.lifesteal.LifestealManager;
import arcaneresearch.api.lifesteal.LifestealStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * EN: Blade of the Crimson Cult: netherite-tier sword with warp, sapping, crits and lifesteal.
 * RU: Клинок багряного культа: меч незеритового уровня с искажением, истощением, критами и вампиризмом.
 */
public class ItemCrimsonCultBlade extends Item implements IWarpingGear, ICriticalGear {
    private static final LifestealStats LIFESTEAL_STATS = LifestealStats.of(75.0D, 1.0D);

    public ItemCrimsonCultBlade(Item.Properties props) {
        super(props
                .rarity(Rarity.EPIC)
                .sword(ToolMaterial.NETHERITE, 3, -2.4F));
    }

    @Override
    public double getCritChance(ItemStack stack) {
        return 25.0D;
    }

    @Override
    public double getCritDamage(ItemStack stack) {
        return 50.0D;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            boolean canApplyEffects = true;
            if (target instanceof Player && attacker instanceof Player
                    && target.level() instanceof ServerLevel serverLevel && !serverLevel.isPvpAllowed()) {
                canApplyEffects = false;
            }
            if (canApplyEffects) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
                if (attacker instanceof Player player) {
                    float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    LifestealManager.apply(player, target, LIFESTEAL_STATS, damage);
                }
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 3;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("enchantment.special.sapgreat")
                .withStyle(style -> style.withColor(0x8B0000)));
        tooltip.accept(Component.translatable("enchantment.special.lifesteal")
                .withStyle(style -> style.withColor(0x8B0000)));
        // CritTooltips works with a list; bridge it to the new consumer-based tooltip API.
        java.util.List<Component> lines = new java.util.ArrayList<>();
        CritTooltips.addGearTooltip(stack, lines);
        lines.forEach(tooltip);
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
