package arcaneresearch.common.container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
@Deprecated
public class InventoryHandMirror extends SimpleContainer {
    public InventoryHandMirror(ContainerListener listener) {
        super(1);
        if (listener != null) {
            addListener(listener);
        }
    }
}
