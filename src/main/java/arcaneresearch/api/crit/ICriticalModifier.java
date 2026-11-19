package arcaneresearch.api.crit;

import net.minecraft.world.entity.player.Player;

/**
 * EN: Extension point for non-item sources of critical stats (curios, effects, modules, ...).
 * RU: Точка расширения для не-предметных источников статов крита (куриосы, эффекты, модули и т.д.).
 * EN: Registered modifiers are applied on top of the player's attributes for every crit roll.
 * RU: Зарегистрированные модификаторы применяются поверх атрибутов игрока при каждом броске крита.
 */
@FunctionalInterface
public interface ICriticalModifier {
    /**
     * EN: Returns stats with this source's bonuses applied.
     * RU: Возвращает статы с применёнными бонусами этого источника.
     */
    CritStats modify(Player player, CritStats current);
}
