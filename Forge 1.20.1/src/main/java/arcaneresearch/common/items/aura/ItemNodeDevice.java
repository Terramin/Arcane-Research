package arcaneresearch.common.items.aura;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(
            java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private arcaneresearch.client.renderers.item.NodeDeviceItemRenderer renderer;
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new arcaneresearch.client.renderers.item.NodeDeviceItemRenderer(kind);
                }
                return renderer;
            }
        });
    }
}
