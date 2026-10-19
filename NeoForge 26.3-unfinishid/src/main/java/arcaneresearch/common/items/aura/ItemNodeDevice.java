package arcaneresearch.common.items.aura;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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

    // NEOFORGE_PORT: Item.initializeClient removed in 26.x; kept as a no-op hook.
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(
            java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        // NEOFORGE_PORT: BlockEntityWithoutLevelRenderer removed in 26.x; done via
        // client/renderers/item/NodeDeviceSpecialRenderer (arcaneresearch:node_device),
        // registered in SpecialItemModels and wired in assets/arcaneresearch/items/node_*.json.
    }
}
