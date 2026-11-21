package arcaneresearch.api.crit;

import net.minecraft.world.entity.player.Player;

/**
 * EN: Immutable snapshot of a player's critical hit stats.
 * RU: Неизменяемый снимок параметров критического удара игрока.
 * EN: Values are percentages: chance 0-100, damage 100 = x1.0 (base 150 = x1.5).
 * RU: Значения указаны в процентах: шанс 0-100, урон 100 = x1.0 (база 150 = x1.5).
 */
public final class CritStats {
    /**
     * EN: Vanilla-like baseline: 5% chance, x1.5 damage.
     * RU: Базовые значения: 5% шанса, x1.5 урона.
     */
    public static final CritStats BASE = new CritStats(5.0D, 150.0D);
    /**
     * EN: Zero value, useful for early returns.
     * RU: Нулевое значение, удобно для досрочного выхода из методов.
     */
    public static final CritStats NONE = new CritStats(0.0D, 0.0D);

    private final double chance;
    private final double damage;

    /**
     * EN: Creates stats; negative values are clamped to zero.
     * RU: Создаёт статы; отрицательные значения ограничиваются нулём.
     */
    public CritStats(double chance, double damage) {
        this.chance = Math.max(0.0D, chance);
        this.damage = Math.max(0.0D, damage);
    }

    /**
     * EN: Factory method for stats.
     * RU: Фабричный метод для создания статов.
     */
    public static CritStats of(double chance, double damage) {
        return new CritStats(chance, damage);
    }

    /**
     * EN: Reads the current attribute values of the player.
     * RU: Читает текущие значения атрибутов игрока.
     */
    public static CritStats fromPlayer(Player player) {
        return new CritStats(
                player.getAttributeValue(CritAttributes.CRIT_CHANCE),
                player.getAttributeValue(CritAttributes.CRIT_DAMAGE));
    }

    /**
     * EN: Percentage chance, e.g. 30 means 30%.
     * RU: Шанс в процентах, например 30 означает 30%.
     */
    public double getChance() {
        return chance;
    }

    /**
     * EN: Percentage damage multiplier, e.g. 150 means x1.5.
     * RU: Множитель урона в процентах, например 150 означает x1.5.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * EN: Chance as a 0..1 multiplier for random checks.
     * RU: Шанс в виде множителя 0..1 для проверки случайного числа.
     */
    public double getChanceMultiplier() {
        return chance / 100.0D;
    }

    /**
     * EN: Damage as a 0..N multiplier.
     * RU: Урон в виде множителя 0..N.
     */
    public double getDamageMultiplier() {
        return damage / 100.0D;
    }

    /**
     * EN: True when a crit is possible (both chance and damage are positive).
     * RU: Истина, когда критический удар возможен (шанс и урон больше нуля).
     */
    public boolean canCrit() {
        return chance > 0.0D && damage > 0.0D;
    }

    /**
     * EN: Returns a copy of the stats with the given bonuses added.
     * RU: Возвращает копию статов с добавленными бонусами.
     */
    public CritStats plus(double extraChance, double extraDamage) {
        return new CritStats(chance + extraChance, damage + extraDamage);
    }

    /**
     * EN: Adds another stats object to this one.
     * RU: Добавляет другой объект статов к этому.
     */
    public CritStats plus(CritStats other) {
        return plus(other.chance, other.damage);
    }

    @Override
    public String toString() {
        // EN: Debug representation of the stats.
        // RU: Отладочное представление статов.
        return "CritStats{chance=" + chance + "%, damage=" + damage + "%}";
    }
}
