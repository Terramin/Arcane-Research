package arcaneresearch.common.items.aura;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Предмет блока eldritch_portal. Отображается не текстурой cross-модели,
 * а самим порталом через EldritchPortalItemRenderer (пример — ItemAuraNode
 * + NodeItemRenderer). Лежит в arcaneresearch.arcaneresearch.common.items.aura по просьбе портирования.
 */
public class EldritchPortalItem extends BlockItem {
    public EldritchPortalItem(Block block, Properties properties) {
        super(block, properties);
    }

    // NEOFORGE_PORT (26.3): Item.initializeClient(Consumer<IClientItemExtensions>) was removed
    // in 26.x (the old no-op kept a client-only type in this common class). Forge wired
    // EldritchPortalItemRenderer (BEWLR) there; in 26.x the same billboard render is
    // data-driven: assets/arcaneresearch/items/eldritch_portal.json (display_context select +
    // arcaneresearch:eldritch_portal special layer, see EldritchPortalSpecialRenderer +
    // SpecialItemModels).
}
