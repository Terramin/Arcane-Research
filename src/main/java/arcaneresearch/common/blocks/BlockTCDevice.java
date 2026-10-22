package arcaneresearch.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty; 

import javax.annotation.Nullable;

public abstract class BlockTCDevice extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public BlockTCDevice(Properties properties) {
        super(properties);

        BlockState defaultState = this.stateDefinition.any();
        if (hasFacing()) {
            defaultState = defaultState.setValue(FACING, Direction.NORTH);
        }
        if (hasEnabled()) {
            defaultState = defaultState.setValue(ENABLED, true);
        }
        this.registerDefaultState(defaultState);
    }

    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
            BlockEntityType<A> givenType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == givenType ? (BlockEntityTicker<A>) ticker : null;
    }

    protected boolean hasFacing() {
        return true;
    }

    protected boolean hasEnabled() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (hasFacing()) {
            builder.add(FACING);
        }
        if (hasEnabled()) {
            builder.add(ENABLED);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState();

        if (hasFacing()) {
            Direction facing = context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                    ? context.getHorizontalDirection()
                    : context.getHorizontalDirection().getOpposite();
            state = state.setValue(FACING, facing);
        }

        if (hasEnabled()) {
            state = state.setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
        }

        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        if (hasEnabled() && !level.isClientSide()) {
            boolean shouldBeEnabled = !level.hasNeighborSignal(pos);
            if (state.getValue(ENABLED) != shouldBeEnabled) {
                level.setBlock(pos, state.setValue(ENABLED, shouldBeEnabled), 3);
            }
        }
    }

    public void updateFacing(Level level, BlockPos pos, Direction face) {
        if (!hasFacing() || !face.getAxis().isHorizontal()) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || state.getValue(FACING) == face) {
            return;
        }
        level.setBlock(pos, state.setValue(FACING, face), 3);
    }

    @Nullable
    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
