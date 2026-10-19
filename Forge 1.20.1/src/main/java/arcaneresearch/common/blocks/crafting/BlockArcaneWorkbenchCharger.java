package arcaneresearch.common.blocks.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import arcaneresearch.init.ModBlocks;
public class BlockArcaneWorkbenchCharger extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 6, 14);
    public BlockArcaneWorkbenchCharger() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.25f, 10.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }
    public static BlockArcaneWorkbenchCharger create() {
        return new BlockArcaneWorkbenchCharger();
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return isValidBase(below);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos below = context.getClickedPos().below();
        BlockState belowState = context.getLevel().getBlockState(below);
        if (!isValidBase(belowState)) {
            return null;
        }
        return defaultBlockState();
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        BlockEntity be = level.getBlockEntity(pos.below());
        if (be instanceof TileArcaneWorkbench workbench) {
            workbench.syncTile(true);
        }
        if (be instanceof TileFocalManipulator focal) {
            focal.syncTile(true);
        }
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !isValidBase(neighborState)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!canSurvive(state, level, pos)) {
                level.destroyBlock(pos, true);
            }
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.is(ModBlocks.ARCANE_WORKBENCH.get())) {
            BlockEntity be = level.getBlockEntity(belowPos);
            if (be instanceof TileArcaneWorkbench workbench) {
                player.openMenu(workbench);
                return InteractionResult.CONSUME;
            }
        } else if (belowState.is(ModBlocks.FOCAL_MANIPULATOR.get())) {
            BlockEntity be = level.getBlockEntity(belowPos);
            if (be instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }
    private boolean isValidBase(BlockState state) {
        return state.is(ModBlocks.ARCANE_WORKBENCH.get()) || state.is(ModBlocks.FOCAL_MANIPULATOR.get());
    }
}
