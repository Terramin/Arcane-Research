package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;

public class BlockTaintLog extends RotatedPillarBlock implements ITaintBlock {

    public BlockTaintLog(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f, 100.0f)
                .sound(ModSounds.GORE_TYPE)
                .requiresCorrectToolForDrops()
                .randomTicks());
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 4;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 4;
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        if (ModBlocks.FLUX_GOO != null) {
            level.setBlockAndUpdate(pos, ModBlocks.FLUX_GOO.get().defaultBlockState());
        } else {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(level, pos)) {
            die(level, pos, state);
        } else {
            TaintHelper.spreadFibres(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)). Inventory drops are automatic
        // now (BlockEntity.preRemoveSideEffects default drops Container contents).
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock())) {

            int radius = 4;
            int checkRadius = radius + 1;

            if (level.isAreaLoaded(pos, checkRadius)) {
                for (BlockPos checkPos : BlockPos.betweenClosed(
                        pos.offset(-radius, -radius, -radius),
                        pos.offset(radius, radius, radius))) {
                    BlockState checkState = level.getBlockState(checkPos);

                    if (checkState.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        level.scheduleTick(checkPos.immutable(), checkState.getBlock(), 1);
                    }
                }
            }


        }
    }
}
