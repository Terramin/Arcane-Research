package arcaneresearch.api.lifesteal;

/**
 * EN: Immutable lifesteal stats: chance in percent and flat heal amount in health points.
 * RU: Неизменяемые статы вампиризма: шанс в процентах и фиксированное лечение в очках здоровья.
 * EN: Chance by level: 75%, 60%, 60%, 60%, 60%. Heal is half a heart (1 HP) per level.
 * RU: Шанс по уровням: 75%, 60%, 60%, 60%, 60%. Лечение — пол сердца (1 HP) за уровень.
 */
public final class LifestealStats {
    /**
     * EN: Chance in percent indexed by enchantment level (index 0 is unused).
     * RU: Шанс в процентах по индексу уровня зачарования (индекс 0 не используется).
     */
    public static final double[] CHANCE_BY_LEVEL = {0.0D, 75.0D, 60.0D, 60.0D, 60.0D, 60.0D};
    /** EN: Health points healed per level (half a heart = 1 HP). RU: Очки здоровья за уровень (пол сердца = 1 HP). */
    public static final double HEAL_PER_LEVEL = 1.0D;
    /** EN: Maximum chance (100% = always). RU: Максимальный шанс (100% = всегда). */
    public static final double MAX_CHANCE = 100.0D;
    /** EN: Maximum heal (5 levels x 1 HP). RU: Максимальное лечение (5 уровней x 1 HP). */
    public static final double MAX_HEAL = 5.0D;

    /** EN: Empty stats. RU: Пустые статы. */
    public static final LifestealStats NONE = new LifestealStats(0.0D, 0.0D);

    private final double chance;
    private final double heal;

    public LifestealStats(double chance, double heal) {
        this.chance = Math.max(0.0D, Math.min(MAX_CHANCE, chance));
        this.heal = Math.max(0.0D, Math.min(MAX_HEAL, heal));
    }

    /**
     * EN: Factory for explicit stats, used by items that implement lifesteal themselves.
     * RU: Фабрика для явно заданных статов — используется предметами, реализующими вампиризм сами.
     */
    public static LifestealStats of(double chance, double heal) {
        return new LifestealStats(chance, heal);
    }

    /**
     * EN: Builds stats from an enchantment level using the per-level chance table.
     * RU: Строит статы из уровня зачарования, используя таблицу шансов по уровням.
     */
    public static LifestealStats fromLevel(int level) {
        if (level <= 0 || level >= CHANCE_BY_LEVEL.length) {
            return NONE;
        }
        return new LifestealStats(CHANCE_BY_LEVEL[level], HEAL_PER_LEVEL * level);
    }

    /** EN: Chance in percent. RU: Шанс в процентах. */
    public double getChance() {
        return chance;
    }

    /** EN: Flat heal amount in health points. RU: Фиксированное лечение в очках здоровья. */
    public double getHeal() {
        return heal;
    }

    /** EN: Chance as a 0..1 multiplier. RU: Шанс в виде множителя 0..1. */
    public double getChanceMultiplier() {
        return chance / 100.0D;
    }

    /** EN: True when lifesteal is possible. RU: Истина, когда вампиризм возможен. */
    public boolean canLifesteal() {
        return chance > 0.0D && heal > 0.0D;
    }

    /**
     * EN: Returns a copy with bonuses added; caps are re-applied.
     * RU: Возвращает копию с добавленными бонусами; ограничения применяются заново.
     */
    public LifestealStats plus(double extraChance, double extraHeal) {
        return new LifestealStats(chance + extraChance, heal + extraHeal);
    }

    /** EN: Adds another stats object. RU: Добавляет другой объект статов. */
    public LifestealStats plus(LifestealStats other) {
        return plus(other.chance, other.heal);
    }

    @Override
    public String toString() {
        // EN: Debug representation of the stats.
        // RU: Отладочное представление статов.
        return "LifestealStats{chance=" + chance + "%, heal=" + heal + " HP}";
    }
}
