package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty; // NEOFORGE_PORT: EnumProperty<Direction> folded into EnumProperty<Direction>
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.init.ModBlocks;

public class BlockBrainBox extends Block {

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.Plane.HORIZONTAL);

    private static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);

    public BlockBrainBox(BlockBehaviour.Properties props) {
        super(props);

        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static BlockBrainBox create(BlockBehaviour.Properties props) {
        return new BlockBrainBox(props
                .mapColor(MapColor.METAL)
                .strength(1.0f, 10.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos targetPos = context.getClickedPos().relative(facing);
        Level level = context.getLevel();

        if (isValidHost(level.getBlockState(targetPos), facing.getOpposite())) {
            return defaultBlockState().setValue(FACING, facing);
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos checkPos = context.getClickedPos().relative(dir);
            if (isValidHost(level.getBlockState(checkPos), dir.getOpposite())) {
                return defaultBlockState().setValue(FACING, dir);
            }
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos,
                                Direction direction, BlockPos neighborPos, BlockState neighborState,
                                net.minecraft.util.RandomSource random) {

        return super.updateShape(state, level, tickAccess, currentPos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        Direction facing = state.getValue(FACING);
        BlockPos thaumatoriumPos = pos.relative(facing);

        if (!isValidHost(level.getBlockState(thaumatoriumPos), facing.getOpposite())) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos thaumatoriumPos = pos.relative(facing);
        return isValidHost(level.getBlockState(thaumatoriumPos), facing.getOpposite());
    }

    private boolean isValidHost(BlockState hostState, Direction hostFrontForbidden) {
        if (!isThaumatoriumBlock(hostState)) {
            return false;
        }
        if (hostState.hasProperty(
                arcaneresearch.common.blocks.crafting.BlockThaumatorium.FACING)
                && hostState.getValue(
                arcaneresearch.common.blocks.crafting.BlockThaumatorium.FACING)
                == hostFrontForbidden) {
            return false;
        }
        return true;
    }

    private boolean isThaumatoriumBlock(BlockState state) {
        return state.is(ModBlocks.THAUMATORIUM.get()) || state.is(ModBlocks.THAUMATORIUM_TOP.get());
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return true;
    }
}
