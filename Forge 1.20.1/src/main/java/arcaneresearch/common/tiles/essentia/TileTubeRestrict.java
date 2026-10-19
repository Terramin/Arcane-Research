package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModBlockEntities;
public class TileTubeRestrict extends TileTube {
    public TileTubeRestrict(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileTubeRestrict(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_RESTRICT.get(), pos, state);
    }
    @Override
    protected void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, true, directional);
    }
}
