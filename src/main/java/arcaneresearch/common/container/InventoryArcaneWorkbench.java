package arcaneresearch.common.container;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import java.util.List;
@Deprecated
public class InventoryArcaneWorkbench extends SimpleContainer implements CraftingContainer, IArcaneWorkbench {
    private final BlockEntity workbench;
    private final AbstractContainerMenu menu;
    public InventoryArcaneWorkbench(BlockEntity tile, AbstractContainerMenu menu) {
        super(15);
        this.workbench = tile;
        this.menu = menu;
    }
    @Override
    public int getWidth() {
        return 5;
    }
    @Override
    public int getHeight() {
        return 3;
    }
    @Override
    public List<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < getContainerSize(); i++) {
            items.set(i, getItem(i));
        }
        return items;
    }
    @Override
    public void setChanged() {
        super.setChanged();
        workbench.setChanged();
        if (menu != null) {
            menu.slotsChanged(this);
        }
    }
}
