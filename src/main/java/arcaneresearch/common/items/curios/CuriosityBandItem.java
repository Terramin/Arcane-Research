package arcaneresearch.common.items.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.init.ModItems;

public class CuriosityBandItem extends EquippableCurioItem {
    public CuriosityBandItem() {
        super(new Item.Properties()
                .rarity(Rarity.RARE), CurioSlot.HEAD);
    }

    public static boolean isWearingBand(Player player) {
        if (CuriosCompat.isCuriosLoaded() && !CuriosCompat.findCuriosItems(player,
                s -> !s.isEmpty() && s.getItem() == ModItems.CURIOSITY_BAND.get()).isEmpty()) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof CuriosityBandItem) {
                return true;
            }
        }
        return false;
    }

}
