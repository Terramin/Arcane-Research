package arcaneresearch.api.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IWarpingGear {

    int getWarp(ItemStack itemstack, Player player);
}
