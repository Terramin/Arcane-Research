package arcaneresearch.common.container;

import net.minecraft.world.SimpleContainer;

@Deprecated
public class InventoryLogistics extends SimpleContainer {
    public InventoryLogistics() {
        super(81);
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }
}
