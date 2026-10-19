package arcaneresearch.api.wands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public interface IWandTriggerManager {
    boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side, int event);
}
