package arcaneresearch.common.container.slot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
@Deprecated
public class SlotGhost extends Slot {
    protected int limit;
    public SlotGhost(Container container, int slot, int x, int y, int limit) {
        super(container, slot, x, y);
        this.limit = Integer.MAX_VALUE;
        this.limit = limit;
    }
    public SlotGhost(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.limit = Integer.MAX_VALUE;
    }
    @Override
    public int getMaxStackSize() {
        return limit;
    }
    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
