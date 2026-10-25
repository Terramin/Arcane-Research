package arcaneresearch.common.container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
@Deprecated
public class InventoryLogistics extends SimpleContainer {
    public InventoryLogistics(ContainerListener listener) {
        super(81);
        if (listener != null) {
            addListener(listener);
        }
    }
    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }
}
