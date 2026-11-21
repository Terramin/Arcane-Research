package arcaneresearch.common.lib.events;

import net.neoforged.api.distmarker.Dist;
@Deprecated
public class KeyHandler extends arcaneresearch.client.lib.events.KeyHandler {

    public static long lastPressH = 0L;

    public KeyHandler() {
        arcaneresearch.client.lib.events.KeyHandler.init();
    }
}
