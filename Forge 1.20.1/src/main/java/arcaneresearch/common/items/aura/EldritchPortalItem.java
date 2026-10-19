package arcaneresearch.common.items.aura;
import arcaneresearch.client.renderers.item.EldritchPortalItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
public class EldritchPortalItem extends BlockItem {
    public EldritchPortalItem(Block block, Properties properties) {
        super(block, properties);
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(
            java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private EldritchPortalItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EldritchPortalItemRenderer();
                }
                return renderer;
            }
        });
    }
}
