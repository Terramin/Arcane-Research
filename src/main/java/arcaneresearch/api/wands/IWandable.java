package arcaneresearch.api.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IWandable {
    boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction facing);

    void onUsingWandTick(ItemStack wand, Player player, int count);

    void onWandStoppedUsing(ItemStack wand, Level level, Player player, int count);
}
