package arcaneresearch.api.crit;

import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EN: Registry of global critical hit modifiers. Thread-safe for registration from any thread.
 * RU: Реестр глобальных модификаторов критического удара. Потокобезопасен для регистрации из любого потока.
 */
public final class CritModifiers {
    // EN: Copy-on-write keeps iteration safe while mods register during runtime.
    // RU: Copy-on-write обеспечивает безопасную итерацию, пока моды регистрируются во время работы.
    private static final List<ICriticalModifier> MODIFIERS = new CopyOnWriteArrayList<>();

    private CritModifiers() {
    }

    /**
     * EN: Registers a modifier (duplicates are ignored).
     * RU: Регистрирует модификатор (дубликаты игнорируются).
     */
    public static void register(ICriticalModifier modifier) {
        if (modifier != null && !MODIFIERS.contains(modifier)) {
            MODIFIERS.add(modifier);
        }
    }

    /**
     * EN: Removes a previously registered modifier.
     * RU: Удаляет ранее зарегистрированный модификатор.
     */
    public static void unregister(ICriticalModifier modifier) {
        MODIFIERS.remove(modifier);
    }

    /**
     * EN: Immutable snapshot of all registered modifiers.
     * RU: Неизменяемый снимок всех зарегистрированных модификаторов.
     */
    public static List<ICriticalModifier> getModifiers() {
        return List.copyOf(MODIFIERS);
    }

    /**
     * EN: Applies every modifier in registration order.
     * RU: Применяет все модификаторы в порядке регистрации.
     */
    public static CritStats apply(Player player, CritStats stats) {
        CritStats result = stats;
        for (ICriticalModifier modifier : MODIFIERS) {
            result = modifier.modify(player, result);
        }
        return result;
    }
}
