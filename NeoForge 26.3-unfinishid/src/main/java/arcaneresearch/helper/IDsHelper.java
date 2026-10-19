package arcaneresearch.helper;

import net.minecraft.resources.Identifier;

/**
 * Единая точка конструирования идентификаторов (порт из Forge 1.20.1).
 */
public final class IDsHelper {

    private IDsHelper() {
    }

    /** {@code arcaneresearch:path}. */
    public static Identifier mod(String path) {
        return Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID, path);
    }

    /** {@code namespace:path}. */
    public static Identifier of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    /** Разбор {@code "namespace:path"} (и bare {@code "path"} — ванильный неймспейс). */
    public static Identifier parse(String id) {
        return Identifier.parse(id);
    }

    /** {@code minecraft:path}. */
    public static Identifier vanilla(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }
}
