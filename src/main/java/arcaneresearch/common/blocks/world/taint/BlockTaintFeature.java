package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty; 
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.monster.tainted.EntityTaintCrawler;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModSounds;

public class BlockTaintFeature extends DirectionalBlock implements ITaintBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_DOWN = Block.box(1, 6, 1, 15, 16, 15);
    private static final VoxelShape SHAPE_UP = Block.box(1, 0, 1, 15, 10, 15);
    private static final VoxelShape SHAPE_NORTH = Block.box(1, 1, 6, 15, 15, 15);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 1, 0, 15, 15, 10);
    private static final VoxelShape SHAPE_WEST = Block.box(6, 1, 1, 16, 15, 15);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 1, 1, 10, 15, 15);

    public BlockTaintFeature(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(0.1f)
                .sound(ModSounds.GORE_TYPE)
                .lightLevel(state -> 9)
                .noOcclusion()
                .randomTicks());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachPos = pos.relative(facing.getOpposite());
        BlockState attachState = level.getBlockState(attachPos);
        return attachState.isFaceSturdy(level, attachPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos pos,
                                Direction direction, BlockPos neighborPos, BlockState neighborState,
                                net.minecraft.util.RandomSource random) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
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

        if (!TaintHelper.isNearTaintSeed(level, pos) && random.nextInt(10) == 0) {
            die(level, pos, state);
            return;
        }

        TaintHelper.spreadFibres(level, pos);

        if (random.nextInt(100) == 0) {
            BlockState belowState = level.getBlockState(pos.below());
            if (ModBlocks.TAINT_LOG != null &&
                    belowState.is(ModBlocks.TAINT_LOG.get()) &&
                    belowState.getValue(BlockTaintLog.AXIS) == Direction.Axis.Y) {
                if (ModBlocks.TAINT_GEYSER != null) {
                    level.setBlockAndUpdate(pos, ModBlocks.TAINT_GEYSER.get().defaultBlockState());
                }
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        
        
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock()) && !level.isClientSide()) {
            RandomSource random = level.getRandom();
            if (random.nextFloat() < 0.333f) {

                Entity crawler = new EntityTaintCrawler(level);
                
                crawler.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, random.nextInt(360), 0.0f);
                level.addFreshEntity(crawler);
            } else {

                AuraHelper.polluteAura(level, pos, 1.0f, true);
            }
        }

    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {

        return player.hasCorrectToolForDrops(state);
    }

    
    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }
}
