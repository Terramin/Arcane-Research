package arcaneresearch.api.lifesteal;

import net.minecraft.world.entity.player.Player;

/**
 * EN: Public entry point of the lifesteal API.
 * RU: Публичная точка входа API вампиризма.
 */
public final class LifestealApi {
    private LifestealApi() {
    }

    /**
     * EN: Registers a global lifesteal source.
     * RU: Регистрирует глобальный источник вампиризма.
     */
    public static void registerSource(ILifestealSource source) {
        LifestealModifiers.register(source);
    }

    /**
     * EN: Removes a previously registered source.
     * RU: Удаляет ранее зарегистрированный источник.
     */
    public static void unregisterSource(ILifestealSource source) {
        LifestealModifiers.unregister(source);
    }

    /**
     * EN: Current lifesteal stats from global sources.
     * RU: Текущие статы вампиризма от глобальных источников.
     */
    public static LifestealStats getStats(Player player) {
        return LifestealManager.resolveStats(player);
    }
}
