package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.Arcaneresearch;

/**
 * TEMP-VERIFY ONLY - not part of the port, deleted after the world-creation fix is confirmed.
 * Loads the existing test world as soon as the title screen appears (same call a player's click
 * makes), so the integrated-server save path (ChunkMap.saveAllChunks) runs without manual input.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class TempVerifyAutoWorld {

    private static final Logger LOGGER = LoggerFactory.getLogger("TempVerify");
    private static boolean triggered = false;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        LOGGER.info("TEMP-VERIFY screen init: {}", event.getScreen().getClass().getName());
        tryOpen(event.getScreen());
    }

    @SubscribeEvent
    public static void onScreenRenderBackground(ScreenEvent.Render.Background event) {
        tryOpen(event.getScreen());
    }

    private static void tryOpen(Screen screen) {
        if (triggered || !(screen instanceof TitleScreen)) {
            return;
        }
        triggered = true;
        LOGGER.info("TEMP-VERIFY title screen seen - loading world 'New World'");
        Minecraft mc = Minecraft.getInstance();
        mc.createWorldOpenFlows().openWorld("New World", () -> LOGGER.info("TEMP-VERIFY openWorld was cancelled"));
    }
}
