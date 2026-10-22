package arcaneresearch.common.container;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.entities.monster.EntityPech;

@Deprecated
public class InventoryPech extends SimpleContainer {
    private final EntityPech merchant;
    private final Player thePlayer;

    
    public InventoryPech(Player player, EntityPech merchant) {
        super(5);
        this.thePlayer = player;
        this.merchant = merchant;
    }

    @Override
    public boolean stillValid(Player player) {
        return merchant != null && merchant.isTamed();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0;
    }
}
