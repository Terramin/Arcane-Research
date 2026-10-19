package arcaneresearch.api.items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import java.util.ArrayList;
public interface IArchitect {
    BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity player);
    boolean useBlockHighlight(ItemStack stack);
    ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level,
            BlockPos pos, Direction side, Player player);
    boolean showAxis(ItemStack stack, Level level, Player player, Direction side,
            EnumAxis axis);
    enum EnumAxis {
        X,
        Y,
        Z;
    }
}
