package arcaneresearch.message.api;

import arcaneresearch.Arcaneresearch;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StoryTellingServerEvents {

    private StoryTellingServerEvents() {
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        StoryTellingAPI.cleanupForPlayer(event.getEntity().getUUID());
    }
}
