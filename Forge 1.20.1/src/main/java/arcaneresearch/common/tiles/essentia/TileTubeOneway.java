package arcaneresearch.common.tiles.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModBlockEntities;
public class TileTubeOneway extends TileTube {
    public TileTubeOneway(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileTubeOneway(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TUBE_ONEWAY.get(), pos, state);
    }
    @Override
    protected void calculateSuction(Aspect filter, boolean restrict, boolean directional) {
        super.calculateSuction(filter, restrict, true);
    }
    @Override
    protected void equalizeWithNeighbours(boolean directional) {
        super.equalizeWithNeighbours(true);
    }
}
