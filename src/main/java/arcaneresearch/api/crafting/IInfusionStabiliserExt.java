package arcaneresearch.api.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IInfusionStabiliserExt extends IInfusionStabiliser {

    float getStabilizationAmount(Level level, BlockPos pos);

    default boolean hasSymmetryPenalty(Level level, BlockPos pos1, BlockPos pos2) {
        return false;
    }

    default float getSymmetryPenalty(Level level, BlockPos pos) {
        return 0;
    }
}
