package arcaneresearch.api.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.wands.WandManager;

public class WandHelper {
    public static boolean consumeVisFromWand(ItemStack wand, Player player, AspectList cost, boolean doit, boolean crafting) {
        if (!(wand.getItem() instanceof IWand w)) {
            return false;
        }
        return w.consumeAllVis(wand, player, cost, !doit, crafting);
    }

    public static boolean consumeVisFromInventory(Player player, AspectList cost) {
        return WandManager.consumeVisFromInventory(player, cost);
    }
}
