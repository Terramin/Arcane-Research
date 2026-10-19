package arcaneresearch.common.blocks.devices;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;
public class BlockHungryChestItem extends BlockItem {
    public BlockHungryChestItem(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private arcaneresearch.client.renderers.item.HungryChestItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new arcaneresearch.client.renderers.item.HungryChestItemRenderer();
                }
                return renderer;
            }
        });
    }
}
