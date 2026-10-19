package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty; // NEOFORGE_PORT: EnumProperty<Direction> folded into EnumProperty<Direction>
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;

public class BlockThaumatorium extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    public BlockThaumatorium(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.METAL)
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        BlockPos above = context.getClickedPos().above();
        Level level = context.getLevel();
        if (!level.getBlockState(above).canBeReplaced(context)) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide() && !isMoving
                && !level.getBlockState(pos.below()).is(ModBlocks.CRUCIBLE.get())) {
            level.setBlock(pos, ModBlocks.ALCHEMICAL_BRASS_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockPos above = pos.above();
        if (level.getBlockState(above).canBeReplaced()) {
            level.setBlock(above, ModBlocks.THAUMATORIUM_TOP.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos,
                                Direction facing, BlockPos facingPos, BlockState facingState,
                                net.minecraft.util.RandomSource random) {

        if (facing == Direction.UP && !facingState.is(ModBlocks.THAUMATORIUM_TOP.get())) {
            return Blocks.AIR.defaultBlockState();
        }

        if (facing == Direction.DOWN && !facingState.is(ModBlocks.CRUCIBLE.get())) {
            return ModBlocks.ALCHEMICAL_BRASS_BLOCK.get().defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.MODEL;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useWithoutItem (hand unused).

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {

        if (player.isCrouching()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileThaumatorium thaumatorium && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(thaumatorium, buf -> buf.writeBlockPos(pos));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side, old state + isMoving; new state already swapped: level.getBlockState(pos)).
        Block brass = ModBlocks.ALCHEMICAL_BRASS_BLOCK.get();
        BlockState newState = level.getBlockState(pos);
        if (isMoving || state.is(newState.getBlock()) || newState.is(brass)) {
            super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileThaumatorium thaumatorium) {
            // NEOFORGE_PORT: inventory dropped by preRemoveSideEffects default; clear slots kept
            // (prevents stale client sync of a removed tile).
            for (int i = 0; i < thaumatorium.getContainerSize(); i++) {
                thaumatorium.setItem(i, ItemStack.EMPTY);
            }
        }

        BlockPos above = pos.above();
        if (level.getBlockState(above).is(ModBlocks.THAUMATORIUM_TOP.get())) {
            level.setBlock(above, brass.defaultBlockState(), Block.UPDATE_ALL);
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                new ItemStack(brass));
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!level.isClientSide()) {
            level.setBlock(pos, brass.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileThaumatorium thaumatorium) {

            int slots = thaumatorium.getContainerSize();
            if (slots <= 0) {
                return 0;
            }
            int filled = 0;
            float fill = 0.0f;
            for (int i = 0; i < slots; i++) {
                ItemStack stack = thaumatorium.getItem(i);
                if (!stack.isEmpty()) {
                    fill += (float) stack.getCount() / stack.getMaxStackSize();
                    filled++;
                }
            }
            return filled > 0 ? net.minecraft.util.Mth.floor(fill / slots * 14.0f) + 1 : 0;
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileThaumatorium(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.THAUMATORIUM.get()) {
            if (level.isClientSide()) {
                return (lvl, pos, st, be) -> TileThaumatorium.clientTick(lvl, pos, st, (TileThaumatorium) be);
            } else {
                return (lvl, pos, st, be) -> TileThaumatorium.serverTick(lvl, pos, st, (TileThaumatorium) be);
            }
        }
        return null;
    }
}
