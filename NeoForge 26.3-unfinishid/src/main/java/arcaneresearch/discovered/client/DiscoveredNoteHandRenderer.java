package arcaneresearch.discovered.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;

/**
 * Записка в руке «как карта», порт в паритете с:
 * - TC3 {@code HandheldItemRenderer.renderResearchNotes} (пергамент, заголовок
 *   галактическим при прогрессе <0.2, сетка иконок {@code ss_tags_2.png},
 *   проценты при >=0.333332, longText шифром при <0.5);
 * - reforged {@code HandheldItemRenderer.renderNotes} (та же раскладка:
 *   фон -7..135, имя (10,10), иконки 15+(i%4)*30 / 28+(i/4)*13,
 *   пороги 0.2/0.3/0.333332/0.5).
 *
 * NEOFORGE_PORT (26.3 render-state): RenderHandEvent полностью другой
 * (PoseStack + SubmitNodeCollector + PlayerRenderState; MultiBufferSource,
 * VertexConsumer-quads и Font.drawInBatch-пути удалены — см. клиентскую
 * render-фазу). Вся геометрия/текст оригинала сохранены в Forge-файле;
 * рендер восстанавливается submit-нодами отдельным этапом.
 * Рука НЕ отменяется: ванильный предмет рисуется как обычно.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class DiscoveredNoteHandRenderer {

    private static final Identifier PARCHMENT =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/parchment.png");
    private static final Identifier UNKNOWN_TAG =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_unknown.png");
    private static final int TEXT_COLOR = 0xFF685E4A;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof ItemDiscoveredResearchNote)) {
            return;
        }
        // TODO_PORT (26.x render-state): записка-как-карта через SubmitNodeCollector
        // (пергамент -7..135, имя (10,10), сетка иконок, проценты, longText).
        // Геометрия 1:1 — в Forge-версии файла. Руку не отменяем.
    }
}
