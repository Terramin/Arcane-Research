package arcaneresearch.common.golems.client.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.common.menu.SealMenu;

@Deprecated
public class SealBaseContainer extends SealMenu {
    public SealBaseContainer(Inventory playerInventory, Level level, ISealEntity seal) {
        super(0, playerInventory, seal);
    }

    public int getCategory() {
        return getCurrentCategory();
    }
}
