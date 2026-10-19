package arcaneresearch.common.items.aura;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Предмет блока eldritch_portal. Отображается не текстурой cross-модели,
 * а самим порталом через EldritchPortalItemRenderer (пример — ItemAuraNode
 * + NodeItemRenderer). Лежит в arcaneresearch.arcaneresearch.common.items.aura по просьбе портирования.
 */
public class EldritchPortalItem extends BlockItem {
    public EldritchPortalItem(Block block, Properties properties) {
        super(block, properties);
    }

    // NEOFORGE_PORT: intentionally a no-op. Forge wired
    // EldritchPortalItemRenderer (BEWLR) here; in 26.x the same billboard
    // render is data-driven: assets/arcaneresearch/items/eldritch_portal.json
    // (display_context select + arcaneresearch:eldritch_portal special layer,
    // see EldritchPortalSpecialRenderer + SpecialItemModels).
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(
            java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
    }
}
