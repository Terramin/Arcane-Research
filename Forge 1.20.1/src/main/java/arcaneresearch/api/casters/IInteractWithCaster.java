package arcaneresearch.api.casters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
public interface IInteractWithCaster {
    boolean onCasterRightClick(Level level, ItemStack casterStack, Player player,
                               BlockPos pos, Direction side, InteractionHand hand);
}
