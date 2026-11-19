package arcaneresearch.api.items;

import arcaneresearch.api.crit.CritStats;
import net.minecraft.world.item.ItemStack;

/**
 * EN: Implement on weapons / tools that grant critical hit stats while in the main hand.
 * RU: Реализуйте на оружии/инструментах, которые дают статы крита в основной руке.
 * EN: Values are percentage points added on top of the player's base attributes.
 * RU: Значения — процентные пункты, добавляемые к базовым атрибутам игрока.
 */
public interface ICriticalGear {
    /**
     * EN: Extra critical chance in percent (25 = +25%, total 30% with the base 5%).
     * RU: Дополнительный шанс крита в процентах (25 = +25%, итого 30% с базовыми 5%).
     */
    double getCritChance(ItemStack stack);

    /**
     * EN: Extra critical damage in percent (50 = +50%, total x2.0 with the base 150%).
     * RU: Дополнительный урон крита в процентах (50 = +50%, итого x2.0 с базовыми 150%).
     */
    double getCritDamage(ItemStack stack);

    /**
     * EN: Convenience wrapper around the two values above.
     * RU: Удобная обёртка над двумя значениями выше.
     */
    default CritStats getCritStats(ItemStack stack) {
        return CritStats.of(getCritChance(stack), getCritDamage(stack));
    }
}
