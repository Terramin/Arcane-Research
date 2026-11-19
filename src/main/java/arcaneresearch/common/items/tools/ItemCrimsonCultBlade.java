package arcaneresearch.common.items.tools;

import arcaneresearch.api.crit.CritManager;
import arcaneresearch.api.crit.CritTooltips;
import arcaneresearch.api.items.ICriticalGear;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.api.lifesteal.LifestealManager;
import arcaneresearch.api.lifesteal.LifestealStats;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * EN: Blade of the Crimson Cult: netherite-tier sword with warp, sapping, crits and lifesteal.
 * RU: Клинок багряного культа: меч незеритового уровня с искажением, истощением, критами и вампиризмом.
 * EN: Lifesteal is implemented directly on the item, like Crimson Blade's "Greater Sapping".
 * RU: Вампиризм реализован прямо в предмете, как «Сильное истощение» у Багрового клинка.
 */
public class ItemCrimsonCultBlade extends SwordItem implements IWarpingGear, ICriticalGear {
    // EN: The blade's own lifesteal stats: 75% chance, half a heart (1 HP) healed.
    // RU: Собственные статы вампиризма клинка: 75% шанс, пол сердца (1 HP) лечения.
    private static final LifestealStats LIFESTEAL_STATS = LifestealStats.of(75.0D, 1.0D);

    /**
     * EN: Netherite tier (2031 durability, 9.0 speed, x4.0 damage bonus, level 4) and epic rarity.
     * RU: Тир незерита (2031 прочность, 9.0 скорость, +4.0 урона, уровень 4) и эпическая редкость.
     */
    public ItemCrimsonCultBlade() {
        super(Tiers.NETHERITE,
                3,
                -2.4f,
                new Item.Properties()
                        .rarity(Rarity.EPIC));
    }

    /**
     * EN: Adds critical stats to the player while this blade is in the main hand.
     * RU: Добавляет статы крита игроку, пока клинок находится в основной руке.
     */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        // EN: Keep vanilla attack damage/speed modifiers.
        // RU: Сохраняем ванильные модификаторы урона и скорости атаки.
        builder.putAll(super.getAttributeModifiers(slot, stack));
        CritManager.addGearModifiers(builder, slot, stack);
        return builder.build();
    }

    /**
     * EN: +25% critical chance on top of the 5% base (30% total).
     * RU: +25% шанса критического удара к базовым 5% (итого 30%).
     */
    @Override
    public double getCritChance(ItemStack stack) {
        return 25.0D;
    }

    /**
     * EN: +50% critical damage on top of the 150% base (x2.0 total).
     * RU: +50% урона критического удара к базовым 150% (итого x2.0).
     */
    @Override
    public double getCritDamage(ItemStack stack) {
        return 50.0D;
    }

    /**
     * EN: "Greater Sapping" plus lifesteal: weakness, hunger and self-healing on hit.
     * RU: «Сильное истощение» плюс вампиризм: слабость, голод и самолечение при ударе.
     */
    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, LivingEntity target, @NotNull LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            boolean canApplyEffects = true;
            // EN: Do not apply debuffs in PvP when PvP is disabled on the server.
            // RU: Не накладывать дебаффы в PvP, если он запрещён на сервере.
            if (target instanceof Player && attacker instanceof Player) {
                if (target.level().getServer() != null && !Objects.requireNonNull(target.level().getServer()).isPvpAllowed()) {
                    canApplyEffects = false;
                }
            }
            if (canApplyEffects) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
                // EN: Same pattern as Crimson Blade: extra effect resolved right in hurtEnemy.
                // RU: тот же паттерн, что у Багрового клинка: эффект решается прямо в hurtEnemy.
                if (attacker instanceof Player player) {
                    float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    LifestealManager.apply(player, target, LIFESTEAL_STATS, damage);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /**
     * EN: Warping gear value: 3 points of warp while carried.
     * RU: Значение искажения: 3 очка, пока предмет у игрока.
     */
    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 3;
    }

    /**
     * EN: Client-only tooltip: sapping, lifesteal label and crit bonus lines.
     * RU: Клиентская подсказка: истощение, метка вампиризма и строки бонусов крита.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapgreat").withStyle(style -> style.withColor(0x8B0000)));
        tooltip.add(Component.translatable("enchantment.special.lifesteal").withStyle(style -> style.withColor(0x8B0000)));
        CritTooltips.addGearTooltip(stack, tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
