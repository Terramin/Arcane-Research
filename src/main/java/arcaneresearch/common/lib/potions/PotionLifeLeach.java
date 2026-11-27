package arcaneresearch.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * EN: Life Leech effect. The actual heal is applied on attacks in EntityEvents through the
 *     shared lifesteal API, so other mods can cancel or adjust it via LifestealEvent.
 * RU: Эффект «Похищение жизни». Лечение применяется при атаке в EntityEvents через общий
 *     API вампиризма, поэтому другие моды могут отменить или изменить его через LifestealEvent.
 */
public class PotionLifeLeach extends MobEffect {
    /** EN: Blood-red colour matching the effect icon. RU: Кровь-красный цвет под иконку эффекта. */
    public static final int COLOUR = 0x9E1B2F;

    /**
     * EN: Registers a beneficial effect with a blood-red icon colour.
     * RU: Регистрирует полезный эффект с цветом иконки под цвет крови.
     */
    public PotionLifeLeach() {
        super(MobEffectCategory.BENEFICIAL, COLOUR);
    }

    /**
     * EN: Health restored by one hit: 1 HP (half a heart) per effect level, with no cap.
     *     Applied through LifestealManager.applyFlatHeal so the enchantment limits of
     *     LifestealStats do not cut off high amplifier values.
     * RU: Лечение за один удар: 1 HP (полсердца) за уровень эффекта, без ограничения.
     *     Применяется через LifestealManager.applyFlatHeal, чтобы лимиты LifestealStats
     *     от зачарований не обрезали высокие уровни эффекта.
     *
     * @param amplifier effect level (0 = first level) / уровень эффекта (0 = первый уровень)
     * @return health to restore on one hit / количество здоровья за один удар
     */
    public static float healFor(int amplifier) {
        return (float) (1 + Math.max(0, amplifier));
    }

    /**
     * EN: No passive ticking — life is stolen only when the affected entity attacks.
     * RU: Пассивного тика нет — жизнь похищается только при атаке сущности с эффектом.
     *
     * @param target    entity carrying the effect / сущность с эффектом
     * @param amplifier effect level / уровень эффекта
     */
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
    }

    /**
     * {@inheritDoc}
     *
     * @param duration  remaining duration in ticks / оставшаяся длительность в тиках
     * @param amplifier effect level / уровень эффекта
     * @return always false, healing happens on attacks / всегда false, лечение происходит при атаке
     */
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
