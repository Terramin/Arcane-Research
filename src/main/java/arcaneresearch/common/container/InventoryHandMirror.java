package arcaneresearch.common.container;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import java.util.function.Consumer;

@Deprecated
public class InventoryHandMirror extends SimpleContainer {
    private final Consumer<Container> onChanged;

    public InventoryHandMirror() {
        this(null);
    }

    // NEOFORGE_PORT: ContainerListener removed in 26.x; Forge addListener parity
    // via setChanged callback (slots call it).
    public InventoryHandMirror(Consumer<Container> onChanged) {
        super(1);
        this.onChanged = onChanged;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (onChanged != null) {
            onChanged.accept(this);
        }
    }
}
