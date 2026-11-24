package arcaneresearch.common.blocks.crafting;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Consumer;

/** Block item that also renders the thaumometer standing on the table. */
public class DeconstructionTableItem extends BlockItem {
    public DeconstructionTableItem(Block block) {
        super(block, new Properties());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private arcaneresearch.client.renderers.item.DeconstructionTableItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new arcaneresearch.client.renderers.item.DeconstructionTableItemRenderer();
                }
                return renderer;
            }
        });
    }
}
