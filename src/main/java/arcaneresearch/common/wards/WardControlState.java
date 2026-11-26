package arcaneresearch.common.wards;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side copy of the client's Ctrl key state. Vanilla only syncs sneak
 * (Shift), so the warding focus needs this to offer a Shift+Ctrl shortcut.
 */
public final class WardControlState {
    private static final Map<UUID, Boolean> CONTROL_DOWN = new ConcurrentHashMap<>();

    private WardControlState() {
    }

    public static void set(Player player, boolean down) {
        if (player != null) {
            CONTROL_DOWN.put(player.getUUID(), down);
        }
    }

    public static boolean isControlDown(Player player) {
        return player != null && Boolean.TRUE.equals(CONTROL_DOWN.get(player.getUUID()));
    }

    public static void clear(Player player) {
        if (player != null) {
            CONTROL_DOWN.remove(player.getUUID());
        }
    }
}
