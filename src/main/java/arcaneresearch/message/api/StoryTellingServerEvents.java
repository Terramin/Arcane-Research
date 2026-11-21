package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class StoryTellingServerEvents {

    private StoryTellingServerEvents() {
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        StoryTellingAPI.cleanupForPlayer(event.getEntity().getUUID());
    }
}
