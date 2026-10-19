package arcaneresearch.api.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ILabelable {

    boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelStack);
}
