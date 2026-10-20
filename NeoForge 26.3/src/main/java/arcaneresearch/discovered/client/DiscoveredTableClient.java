package arcaneresearch.discovered.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.table.DiscoveredTableSetup;

/**
 * Клиентская привязка стола записок (экран + cutout-слои блоков).
 * Отображение блоков — JSON-модели как в reforged
 * (models/block/note_table.json + note_table_side.json + table.json),
 * BER не используется.
 *
 * NEOFORGE_PORT: MenuScreens.register -> RegisterMenuScreensEvent (как все
 * экраны в главном ClientModEvents); ItemBlockRenderTypes.setRenderLayer
 * удалён в 26.x -> render_type в JSON-моделях (TODO вместе с остальными
 * ~50 слоями, см. главный onClientSetup TODO).
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class DiscoveredTableClient {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        if (DiscoveredTableSetup.getMenuType() != null) {
            event.register(DiscoveredTableSetup.getMenuType(), CognitionNoteTableScreen::new);
        }
    }
}
