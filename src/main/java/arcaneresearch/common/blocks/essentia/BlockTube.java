package arcaneresearch.common.blocks.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.items.tools.ItemResonator;
import arcaneresearch.common.tiles.essentia.*;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nullable;
public class BlockTube extends Block implements EntityBlock {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    private static final VoxelShape CENTER = Block.box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);
    private static final VoxelShape BUFFER_CENTER = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
    private static final VoxelShape NORTH_AABB = Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 5.0);
    private static final VoxelShape SOUTH_AABB = Block.box(5.0, 5.0, 11.0, 11.0, 11.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(11.0, 5.0, 5.0, 16.0, 11.0, 11.0);
    private static final VoxelShape WEST_AABB = Block.box(0.0, 5.0, 5.0, 5.0, 11.0, 11.0);
    private static final VoxelShape UP_AABB = Block.box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0);
    private static final VoxelShape DOWN_AABB = Block.box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0);
    private final TubeType tubeType;
    public BlockTube(TubeType type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5f, 5.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.tubeType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }
    public static BlockState withConnections(LevelAccessor level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            state = state.setValue(getPropertyForDirection(dir), canConnectTo(level, pos, dir));
        }
        return state;
    }
    public static BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
    private static boolean canConnectTo(LevelAccessor level, BlockPos pos, Direction side) {
        BlockEntity self = level.getBlockEntity(pos);
        if (self instanceof IEssentiaTransport own && !own.isConnectable(side)) {
            return false;
        }
        BlockEntity te = level.getBlockEntity(pos.relative(side));
        if (te instanceof IEssentiaTransport transport) {
            return transport.isConnectable(side.getOpposite());
        }
        return false;
    }
    public static BlockTube createNormal() {
        return new BlockTube(TubeType.NORMAL);
    }
    public static BlockTube createRestricted() {
        return new BlockTube(TubeType.RESTRICTED);
    }
    public static BlockTube createFilter() {
        return new BlockTube(TubeType.FILTER);
    }
    public static BlockTube createValve() {
        return new BlockTube(TubeType.VALVE);
    }
    public static BlockTube createBuffer() {
        return new BlockTube(TubeType.BUFFER);
    }
    public static BlockTube createOneway() {
        return new BlockTube(TubeType.ONEWAY);
    }
    public TubeType getTubeType() {
        return tubeType;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = tubeType == TubeType.BUFFER ? BUFFER_CENTER : CENTER;
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_AABB);
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_AABB);
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_AABB);
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_AABB);
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_AABB);
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_AABB);
        return shape;
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return withConnections(level, pos, state);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileTube tube && placer != null) {
            net.minecraft.world.phys.Vec3 look = placer.getLookAngle();
            tube.facing = Direction.getNearest(look.x, look.y, look.z);
            tube.markDirtyAndSync();
        }
        if (!level.isClientSide) {
            level.setBlock(pos, withConnections(level, pos, state), 3);
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        BlockEntity te = level.getBlockEntity(pos);
        if (tubeType == TubeType.VALVE) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof ICaster || held.getItem() instanceof IWand
                    || held.getItem() instanceof ItemResonator
                    || held.getItem() == this.asItem()) {
                return InteractionResult.PASS;
            }
            if (te instanceof TileTubeValve valve) {
                if (level.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                valve.setOpen(!valve.isOpen());
                level.sendBlockUpdated(pos, state, state, 3);
                level.playSound(null, pos, ModSounds.SQUEEK.get(), SoundSource.BLOCKS, 0.7f,
                        0.9f + level.random.nextFloat() * 0.2f);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if (tubeType == TubeType.FILTER && te instanceof TileTubeFilter filter) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && filter.getFilter() != null) {
                if (level.isClientSide) {
                    level.playSound(player, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                }
                filter.clearFilter();
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
            if (filter.getFilter() == null && held.getItem() instanceof IEssentiaContainerItem container) {
                var aspects = container.getAspects(held);
                if (aspects != null && aspects.size() > 0) {
                    if (level.isClientSide) {
                        level.playSound(player, pos, ModSounds.KEY.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.SUCCESS;
                    }
                    filter.setFilter(aspects.getAspects()[0]);
                    level.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.CONSUME;
                }
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileTubeBuffer buffer) {
            float n = buffer.aspects.visSize();
            float r = n / 10.0f;
            return net.minecraft.util.Mth.floor(r * 14.0f) + (buffer.aspects.visSize() > 0 ? 1 : 0);
        }
        return 0;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (tubeType) {
            case VALVE -> new TileTubeValve(pos, state);
            case FILTER -> new TileTubeFilter(pos, state);
            case BUFFER -> new TileTubeBuffer(pos, state);
            case ONEWAY -> new TileTubeOneway(pos, state);
            case RESTRICTED -> new TileTubeRestrict(pos, state);
            default -> new TileTube(pos, state);
        };
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.TUBE_VALVE.get()) {
            if (level.isClientSide) {
                return (lvl, p, st, be) -> TileTubeValve.clientTickValve(lvl, p, st, (TileTubeValve) be);
            }
            return (lvl, p, st, be) -> TileTubeValve.serverTickValve(lvl, p, st, (TileTubeValve) be);
        }
        if (type == ModBlockEntities.TUBE_BUFFER.get()) {
            if (level.isClientSide) {
                return (lvl, p, st, be) -> TileTube.clientTick(lvl, p, st, (TileTube) be);
            }
            return (lvl, p, st, be) -> TileTubeBuffer.serverTickBuffer(lvl, p, st, (TileTubeBuffer) be);
        }
        if (type == ModBlockEntities.TUBE.get() || type == ModBlockEntities.TUBE_FILTER.get()
                || type == ModBlockEntities.TUBE_ONEWAY.get() || type == ModBlockEntities.TUBE_RESTRICT.get()) {
            if (level.isClientSide) {
                return (lvl, p, st, be) -> TileTube.clientTick(lvl, p, st, (TileTube) be);
            }
            return (lvl, p, st, be) -> TileTube.serverTick(lvl, p, st, (TileTube) be);
        }
        return null;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        BlockEntity te = level.getBlockEntity(pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (state.getBlock() == newState.getBlock()) {
            return;
        }
        if (te instanceof TileTube tube && tube.getEssentiaAmount(Direction.UP) > 0) {
            if (!level.isClientSide) {
                AuraHelper.polluteAura(level, pos, (float) tube.getEssentiaAmount(Direction.UP), true);
            } else {
                playVentFX(level, pos);
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    private void playVentFX(Level level, BlockPos pos) {
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1f,
                1.0f + level.random.nextFloat() * 0.1f, false);
        for (int a = 0; a < 5; ++a) {
            FXDispatcher.INSTANCE.drawVentParticles(pos.getX() + 0.33 + level.random.nextFloat() * 0.33,
                    pos.getY() + 0.33 + level.random.nextFloat() * 0.33,
                    pos.getZ() + 0.33 + level.random.nextFloat() * 0.33,
                    0.0, 0.0, 0.0, Aspect.FLUX.getColor());
        }
    }
    public enum TubeType {
        NORMAL,
        RESTRICTED,
        FILTER,
        VALVE,
        BUFFER,
        ONEWAY
    }
}
