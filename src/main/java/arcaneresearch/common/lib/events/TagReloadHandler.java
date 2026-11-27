package arcaneresearch.common.lib.events;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applies string-based aspect registrations once item tags are bound by Forge. */
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TagReloadHandler {

    private TagReloadHandler() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ArcaneresearchApi.flushPendingTags();
    }
}
