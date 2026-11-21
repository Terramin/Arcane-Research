package arcaneresearch.api.lifesteal;

import net.minecraft.world.entity.player.Player;

/**
 * EN: Extension point for non-enchantment lifesteal sources (curios, effects, other mods).
 * RU: Точка расширения для источников вампиризма помимо зачарований (куриосы, эффекты, другие моды).
 */
@FunctionalInterface
public interface ILifestealSource {
    /**
     * EN: Returns stats with this source's bonuses applied.
     * RU: Возвращает статы с применёнными бонусами этого источника.
     */
    LifestealStats modify(Player player, LifestealStats current);
}
