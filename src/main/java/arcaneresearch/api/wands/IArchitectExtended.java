package arcaneresearch.api.wands;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import arcaneresearch.api.items.IArchitect;
public interface IArchitectExtended extends IArchitect {
    @Override
    BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity entity);
    @Override
    boolean useBlockHighlight(ItemStack stack);
    default boolean showBlockHighlights(Level level, Player player, ItemStack stack, HitResult hit) {
        return useBlockHighlight(stack);
    }
}
