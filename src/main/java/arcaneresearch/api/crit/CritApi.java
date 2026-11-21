package arcaneresearch.api.crit;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

/**
 * EN: Public entry point of the critical hit API.
 * RU: Публичная точка входа API критического удара.
 */
public final class CritApi {
    private CritApi() {
    }

    /**
     * EN: Call once from the mod constructor with the mod event bus.
     * RU: Вызвать один раз в конструкторе мода, передав шину событий мода.
     */
    public static void init(IEventBus modEventBus) {
        CritAttributes.ATTRIBUTES.register(modEventBus);
    }

    /**
     * EN: Adds crit attributes to the player. Call from the mod's EntityAttributeModificationEvent listener.
     * RU: Добавляет атрибуты крита игроку. Вызвать в слушателе EntityAttributeModificationEvent мода.
     */
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        CritAttributes.addPlayerAttributes(event);
    }

    /**
     * EN: Registers a global modifier that contributes to every player's crit stats.
     * RU: Регистрирует глобальный модификатор, влияющий на статы крита каждого игрока.
     */
    public static void registerModifier(ICriticalModifier modifier) {
        CritModifiers.register(modifier);
    }

    /**
     * EN: Removes a previously registered modifier.
     * RU: Удаляет ранее зарегистрированный модификатор.
     */
    public static void unregisterModifier(ICriticalModifier modifier) {
        CritModifiers.unregister(modifier);
    }

    /**
     * EN: Current crit stats of a player (attributes + registered modifiers).
     * RU: Текущие статы крита игрока (атрибуты + зарегистрированные модификаторы).
     */
    public static CritStats getStats(Player player) {
        return CritManager.resolveStats(player);
    }
}
