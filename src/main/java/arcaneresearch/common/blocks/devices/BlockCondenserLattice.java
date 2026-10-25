package arcaneresearch.common.blocks.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
public class BlockCondenserLattice extends Block {
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    private static final VoxelShape CORE = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    public BlockCondenserLattice() {
        this(4);
    }
    protected BlockCondenserLattice(int lightLevel) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5f, 5.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> lightLevel));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }
    private static boolean connectsTo(BlockState neighbor, Direction face) {
        if (neighbor.getBlock() instanceof BlockCondenserLattice) {
            return true;
        }
        return face == Direction.DOWN && neighbor.is(ModBlocks.CONDENSER.get());
    }
    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos neighborPos = pos.relative(face);
            boolean connects = connectsTo(level.getBlockState(neighborPos), face);
            state = switch (face) {
                case NORTH -> state.setValue(NORTH, connects);
                case EAST -> state.setValue(EAST, connects);
                case SOUTH -> state.setValue(SOUTH, connects);
                case WEST -> state.setValue(WEST, connects);
                case UP -> state.setValue(UP, connects);
                case DOWN -> state.setValue(DOWN, connects);
            };
        }
        return state;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
    @Override
    public BlockState updateShape(BlockState state, Direction face, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return withConnections(state, level, pos);
    }
    protected boolean isDirty() {
        return false;
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            level.setBlock(pos, withConnections(state, level, pos), 3);
            triggerUpdate(level, pos);
        }
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockState updated = withConnections(state, level, pos);
            if (!updated.equals(state)) {
                level.setBlock(pos, updated, 3);
            }
            Block nb = level.getBlockState(fromPos).getBlock();
            if (nb instanceof BlockCondenserLattice || level.getBlockState(fromPos).is(ModBlocks.CONDENSER.get())) {
                triggerUpdate(level, pos);
            }
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, level, pos, newState, isMoving);
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            markCondenserDirty(level, pos);
        }
    }
    public void triggerUpdate(Level level, BlockPos pos) {
        BlockPos anchor = findCondenser(level, pos, new java.util.HashSet<>());
        if (anchor == null || anchor.distSqr(pos) > 74.0) {
            level.destroyBlock(pos, true);
        }
    }
    private void markCondenserDirty(Level level, BlockPos pos) {
        findCondenser(level, pos, new java.util.HashSet<>());
    }
    private BlockPos findCondenser(Level level, BlockPos pos, java.util.Set<Long> history) {
        history.add(pos.asLong());
        for (Direction face : Direction.values()) {
            BlockPos p2 = pos.relative(face);
            if (history.contains(p2.asLong())) {
                continue;
            }
            Block b = level.getBlockState(p2).getBlock();
            if (b instanceof BlockCondenserLattice) {
                BlockPos found = findCondenser(level, p2, history);
                if (found != null) {
                    return found;
                }
            }
            if (face == Direction.DOWN && level.getBlockState(p2).is(ModBlocks.CONDENSER.get())) {
                if (level.getBlockEntity(p2) instanceof arcaneresearch.common.tiles.devices.TileCondenser condenser) {
                    condenser.latticeCount = -1.0f;
                }
                return p2;
            }
        }
        return null;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (isDirty() && player.getItemInHand(hand).is(ModItems.FILTER.get())) {
            player.getItemInHand(hand).shrink(1);
            if (level.random.nextBoolean()) {
                ItemEntity drop = new ItemEntity(level,
                        pos.getX() + 0.5 + hit.getDirection().getStepX() / 3.0,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + hit.getDirection().getStepZ() / 3.0,
                        ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX));
                level.addFreshEntity(drop);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS,
                    0.2f, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 1.6f);
            BlockState clean = withConnections(
                    ModBlocks.CONDENSER_LATTICE.get().defaultBlockState(), level, pos);
            level.setBlock(pos, clean, 3);
            triggerUpdate(level, pos);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        if (state.getValue(DOWN)) shape = Shapes.or(shape, Block.box(5, 0, 5, 11, 5, 11));
        if (state.getValue(UP)) shape = Shapes.or(shape, Block.box(5, 11, 5, 11, 16, 11));
        if (state.getValue(NORTH)) shape = Shapes.or(shape, Block.box(5, 5, 0, 11, 11, 5));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, Block.box(5, 5, 11, 11, 11, 16));
        if (state.getValue(WEST)) shape = Shapes.or(shape, Block.box(0, 5, 5, 5, 11, 11));
        if (state.getValue(EAST)) shape = Shapes.or(shape, Block.box(11, 5, 5, 16, 11, 11));
        return shape;
    }
    public static class Dirty extends BlockCondenserLattice {
        public Dirty() {
            super(0);
        }
        @Override
        protected boolean isDirty() {
            return true;
        }
    }
}
