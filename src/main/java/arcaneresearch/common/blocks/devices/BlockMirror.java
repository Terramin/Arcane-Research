package arcaneresearch.common.blocks.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.devices.TileMirror;
import arcaneresearch.common.tiles.devices.TileMirrorEssentia;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nullable;
public class BlockMirror extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LINKED = BooleanProperty.create("linked");
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
    private final MirrorType mirrorType;
    public BlockMirror(MirrorType type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0f)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .lightLevel(state -> state.getValue(LINKED) ? 7 : 0));
        this.mirrorType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(LINKED, false));
    }
    public static BlockMirror createItem() {
        return new BlockMirror(MirrorType.ITEM);
    }
    public static BlockMirror createPlayer() {
        return new BlockMirror(MirrorType.PLAYER);
    }
    public MirrorType getMirrorType() {
        return mirrorType;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LINKED);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(LINKED, false);
    }
    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && mirrorType == MirrorType.ITEM
                && entity instanceof ItemEntity itemEntity && !itemEntity.isRemoved()
                && itemEntity.getPortalCooldown() == 0) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileMirror mirror) {
                mirror.transport(itemEntity);
            }
        }
        super.entityInside(state, level, pos, entity);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing)) {
            level.destroyBlock(pos, true);
        }
    }
    @Override
    public java.util.List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        ItemStack drop = new ItemStack(this);
        if (be instanceof TileMirror mirror && mirror.linked) {
            drop.getOrCreateTag().putInt("linkX", mirror.linkX);
            drop.getOrCreateTag().putInt("linkY", mirror.linkY);
            drop.getOrCreateTag().putInt("linkZ", mirror.linkZ);
            drop.getOrCreateTag().putString("linkDim", mirror.linkDimension.location().toString());
            mirror.invalidateLink();
        }
        if (be instanceof TileMirrorEssentia mirrorEssentia && mirrorEssentia.linked) {
            drop.getOrCreateTag().putInt("linkX", mirrorEssentia.linkX);
            drop.getOrCreateTag().putInt("linkY", mirrorEssentia.linkY);
            drop.getOrCreateTag().putInt("linkZ", mirrorEssentia.linkZ);
            drop.getOrCreateTag().putString("linkDim", mirrorEssentia.linkDimension.location().toString());
            mirrorEssentia.invalidateLink();
        }
        return java.util.Collections.singletonList(drop);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return mirrorType == MirrorType.PLAYER
                ? new TileMirrorEssentia(pos, state)
                : new TileMirror(pos, state);
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        if (mirrorType == MirrorType.PLAYER && type == ModBlockEntities.MIRROR_ESSENTIA.get()) {
            return (lvl, p, st, be) -> TileMirrorEssentia.serverTick(lvl, p, st, (TileMirrorEssentia) be);
        }
        if (mirrorType != MirrorType.PLAYER && type == ModBlockEntities.MIRROR_ITEM.get()) {
            return (lvl, p, st, be) -> TileMirror.serverTick(lvl, p, st, (TileMirror) be);
        }
        return null;
    }
    public enum MirrorType {
        ITEM,
        PLAYER,
        HAND
    }
}
