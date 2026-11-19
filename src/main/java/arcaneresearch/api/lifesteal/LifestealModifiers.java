package arcaneresearch.api.lifesteal;

import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EN: Thread-safe registry of global lifesteal sources.
 * RU: Потокобезопасный реестр глобальных источников вампиризма.
 */
public final class LifestealModifiers {
    // EN: Copy-on-write allows registration while other threads iterate the list.
    // RU: Copy-on-write позволяет регистрировать источники, пока другие потоки перебирают список.
    private static final List<ILifestealSource> SOURCES = new CopyOnWriteArrayList<>();

    private LifestealModifiers() {
    }

    /** EN: Registers a source (duplicates ignored). RU: Регистрирует источник (дубликаты игнорируются). */
    public static void register(ILifestealSource source) {
        if (source != null && !SOURCES.contains(source)) {
            SOURCES.add(source);
        }
    }

    /** EN: Removes a source. RU: Удаляет источник. */
    public static void unregister(ILifestealSource source) {
        SOURCES.remove(source);
    }

    /** EN: Immutable snapshot of registered sources. RU: Неизменяемый снимок зарегистрированных источников. */
    public static List<ILifestealSource> getSources() {
        return List.copyOf(SOURCES);
    }

    /** EN: Applies every source in registration order. RU: Применяет все источники в порядке регистрации. */
    public static LifestealStats apply(Player player, LifestealStats stats) {
        LifestealStats result = stats;
        for (ILifestealSource source : SOURCES) {
            result = source.modify(player, result);
        }
        return result;
    }
}
