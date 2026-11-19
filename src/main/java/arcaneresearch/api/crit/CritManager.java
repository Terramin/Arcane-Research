package arcaneresearch.api.crit;

import arcaneresearch.api.crit.events.CriticalHitAppliedEvent;
import arcaneresearch.api.crit.events.CriticalHitEvent;
import arcaneresearch.api.items.ICriticalGear;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * EN: Core engine: resolves stats, rolls the chance and applies the critical strike.
 * RU: Ядро механики: собирает статы, бросает шанс и наносит критический удар.
 */
public final class CritManager {
    // EN: Stable UUIDs keep modifiers from stacking when gear is re-equipped.
    // RU: Стабильные UUID не дают модификаторам накапливаться при повторной экипировке.
    private static final UUID CHANCE_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("arcaneresearch:crit_chance".getBytes(StandardCharsets.UTF_8));
    private static final UUID DAMAGE_MODIFIER_UUID =
            UUID.nameUUIDFromBytes("arcaneresearch:crit_damage".getBytes(StandardCharsets.UTF_8));

    private CritManager() {
    }

    /**
     * EN: Attribute values plus all registered global modifiers.
     * RU: Значения атрибутов плюс все зарегистрированные глобальные модификаторы.
     */
    public static CritStats resolveStats(Player player) {
        return CritModifiers.apply(player, CritStats.fromPlayer(player));
    }

    /**
     * EN: Adds the bonuses of a held {@link ICriticalGear} item to the player's crit attributes.
     * RU: Добавляет бонусы предмета {@link ICriticalGear} в атрибуты крита игрока.
     */
    public static void addGearModifiers(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
                                        EquipmentSlot slot, ItemStack stack) {
        // EN: Only the main hand counts, matching vanilla weapon attribute handling.
        // RU: Учитывается только основная рука — как и в ванильной системе атрибутов оружия.
        if (slot != EquipmentSlot.MAINHAND || !(stack.getItem() instanceof ICriticalGear gear)) {
            return;
        }
        double chance = gear.getCritChance(stack);
        double damage = gear.getCritDamage(stack);
        if (chance > 0.0D) {
            builder.put(CritAttributes.CRIT_CHANCE.get(), new AttributeModifier(CHANCE_MODIFIER_UUID,
                    "Critical chance", chance, AttributeModifier.Operation.ADDITION));
        }
        if (damage > 0.0D) {
            builder.put(CritAttributes.CRIT_DAMAGE.get(), new AttributeModifier(DAMAGE_MODIFIER_UUID,
                    "Critical damage", damage, AttributeModifier.Operation.ADDITION));
        }
    }

    /**
     * EN: Rolls the critical chance and, on success, deals an extra hit on top of the vanilla attack.
     * RU: Бросает шанс крита и при успехе наносит дополнительный удар поверх ванильной атаки.
     * EN: Fires {@link CriticalHitEvent} (cancelable) and {@link CriticalHitAppliedEvent}.
     * RU: Вызывает {@link CriticalHitEvent} (отменяемое) и {@link CriticalHitAppliedEvent}.
     * EN: True means a critical strike was applied.
     * RU: Истина означает, что критический удар был нанесён.
     */
    public static boolean tryCriticalHit(Player player, Entity target) {
        // EN: Master switch from config: skip the whole crit mechanic when disabled.
        // RU: Главный переключатель в конфиге: полностью отключаем механику крита.
        if (!CritConfig.isEnabled()) {
            return false;
        }
        // EN: Crits are server-side melee-only and ignore non-living entities.
        // RU: Криты рассчитываются на сервере только для ближнего боя и живых сущностей.
        if (player.level().isClientSide() || !(target instanceof LivingEntity)) {
            return false;
        }
        CritStats stats = resolveStats(player);
        // EN: Chance is stored in percent, so compare with a 0..1 random float.
        // RU: Шанс хранится в процентах, поэтому сравниваем его со случайным числом 0..1.
        if (!stats.canCrit() || player.getRandom().nextFloat() >= stats.getChanceMultiplier()) {
            return false;
        }

        // EN: ATTACK_DAMAGE already includes the equipped weapon's modifiers.
        // RU: ATTACK_DAMAGE уже включает модификаторы экипированного оружия.
        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float critDamage = (float) (attackDamage * stats.getDamageMultiplier());

        // EN: Give other mods a chance to cancel the crit or change its damage.
        // RU: Даём другим модам возможность отменить крит или изменить его урон.
        CriticalHitEvent event = new CriticalHitEvent(player, target, stats, critDamage);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        float finalDamage = event.getDamage();
        if (finalDamage <= 0.0F) {
            return false;
        }

        // EN: Sound and particles before the hit, like vanilla critical hits.
        // RU: Звук и частицы до удара — как у ванильных критических ударов.
        playFeedback(player, target);
        target.hurt(player.damageSources().playerAttack(player), finalDamage);
        // EN: Notify listeners that the crit has actually landed.
        // RU: Уведомляем слушателей, что крит действительно нанесён.
        MinecraftForge.EVENT_BUS.post(new CriticalHitAppliedEvent(player, target, finalDamage));
        return true;
    }

    /**
     * EN: Sound and particles of a critical strike.
     * RU: Звук и частицы критического удара.
     */
    public static void playFeedback(Player player, Entity target) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.0F);
        player.crit(target);
    }
}
