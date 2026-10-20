package arcaneresearch.common.items.aura;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Предмет нод-устройства (стабилизатор/advanced/трансдьюсер/конвертер):
 * инвентарная модель рисуется NodeDeviceItemRenderer (OBJ-девайс),
 * а не кубом-заглушкой.
 */
public class ItemNodeDevice extends BlockItem {
    public enum Kind {
        STABILIZER,
        ADVANCED,
        TRANSDUCER,
        CONVERTER
    }

    private final Kind kind;

    public ItemNodeDevice(Block block, Properties properties, Kind kind) {
        super(block, properties);
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
    }

    // NEOFORGE_PORT (26.3): Item.initializeClient(Consumer<IClientItemExtensions>) was removed
    // in 26.x (the old no-op kept a client-only type in this common class). The Forge BEWLR is
    // done via client/renderers/item/NodeDeviceSpecialRenderer (arcaneresearch:node_device),
    // registered in SpecialItemModels and wired in assets/arcaneresearch/items/node_*.json.
}
