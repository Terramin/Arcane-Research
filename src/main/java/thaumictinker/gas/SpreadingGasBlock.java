package thaumictinker.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

 


 
public abstract class SpreadingGasBlock extends GasBlock {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 15);
    public static final int SPREAD_DELAY = 10;

    private static final Direction[] SPREAD_ORDER = {
            Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH
    };

    protected SpreadingGasBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int gasLevel = state.getValue(LEVEL);
        if (gasLevel == 0) {
            return;
        }
        for (Direction direction : SPREAD_ORDER) {
            spread(level, pos.relative(direction), gasLevel - 1);
        }
        level.setBlock(pos, state.setValue(LEVEL, 0), Block.UPDATE_CLIENTS);
    }
    public void release(ServerLevel level, BlockPos pos, int gasLevel) {
        level.setBlock(pos, defaultBlockState().setValue(LEVEL, gasLevel), Block.UPDATE_CLIENTS);
        level.scheduleTick(pos, this, SPREAD_DELAY);
    }

    private void spread(ServerLevel level, BlockPos target, int gasLevel) {
        if (!level.isLoaded(target)) {
            return;
        }
        BlockState existing = level.getBlockState(target);
        if (canHoldGas(existing) && !existing.is(this)) {
            release(level, target, gasLevel);
        }
    }
}
