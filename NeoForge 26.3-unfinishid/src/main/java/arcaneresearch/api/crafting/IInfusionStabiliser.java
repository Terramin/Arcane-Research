package arcaneresearch.api.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Deprecated
public interface IInfusionStabiliser {

    boolean canStabaliseInfusion(Level level, BlockPos pos);
}
