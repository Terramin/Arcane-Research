package arcaneresearch.common.blocks.devices;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class BlockHungryChestItem extends BlockItem {

    public BlockHungryChestItem(Block block, Properties properties) {
        super(block, properties);
    }

    // NEOFORGE_PORT (26.3): Item.initializeClient(Consumer<IClientItemExtensions>) was
    // removed in 26.x. The Forge BEWLR (HungryChestItemRenderer) has no direct 26.3
    // equivalent; the closed-chest model is drawn by HungryChestSpecialRenderer,
    // registered in SpecialItemModels and wired through the item model JSON.
}
