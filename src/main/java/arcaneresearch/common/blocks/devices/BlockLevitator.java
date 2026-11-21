package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.EnumProperty; 
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.devices.TileLevitator;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import arcaneresearch.lib.raytracer.ExtendedMOP;
import arcaneresearch.lib.raytracer.IndexedCuboid6;
import arcaneresearch.lib.raytracer.RayTracer;
import arcaneresearch.lib.vec.BlockCoord;
import arcaneresearch.lib.vec.Cuboid6;
import arcaneresearch.lib.vec.Vector3;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
public class BlockLevitator extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final float INDENT = 0.125f;

    private static final VoxelShape SHAPE_DOWN = Block.box(0, 2, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_UP = Block.box(0, 0, 0, 16, 14, 16);
    private static final VoxelShape SHAPE_NORTH = Block.box(0, 0, 2, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 0, 0, 16, 16, 14);
    private static final VoxelShape SHAPE_WEST = Block.box(2, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(0, 0, 0, 14, 16, 16);

    public BlockLevitator(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.WOOD)
                .strength(1.5f)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(ENABLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(ENABLED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
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
    
    

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof TileLevitator levitator)) {
            return InteractionResult.PASS;
        }
        Direction facing = state.getValue(FACING);
        Cuboid6 buttonCuboid = switch (facing) {
            case DOWN -> new Cuboid6(0.375, 0.875, 0.375, 0.625, 0.9375, 0.625);
            case UP -> new Cuboid6(0.375, 0.0625, 0.375, 0.625, 0.125, 0.625);
            case NORTH -> new Cuboid6(0.375, 0.375, 0.875, 0.625, 0.625, 0.9375);
            case SOUTH -> new Cuboid6(0.375, 0.375, 0.0625, 0.625, 0.625, 0.125);
            case WEST -> new Cuboid6(0.875, 0.375, 0.375, 0.9375, 0.625, 0.625);
            case EAST -> new Cuboid6(0.0625, 0.375, 0.375, 0.125, 0.625, 0.625);
        };
        buttonCuboid.add(new Vector3(pos.getX(), pos.getY(), pos.getZ()));
        List<IndexedCuboid6> cuboids = new ArrayList<>();
        cuboids.add(new IndexedCuboid6(0, buttonCuboid));
        List<ExtendedMOP> hits = new ArrayList<>();
        RayTracer.instance().rayTraceCuboids(
                new Vector3(RayTracer.getStartVec(player)),
                new Vector3(RayTracer.getEndVec(player)),
                cuboids, new BlockCoord(pos), this, hits);
        if (hits.isEmpty()) {
            return InteractionResult.PASS;
        }
        levitator.increaseRange(player);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.KEY.get(), SoundSource.BLOCKS, 0.5f, 1.0f);
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        updateEnabled(level, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);
        updateEnabled(level, pos, state);
    }

    private void updateEnabled(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        boolean enabled = level.hasNeighborSignal(pos);
        if (state.hasProperty(ENABLED) && state.getValue(ENABLED) != enabled) {
            level.setBlock(pos, state.setValue(ENABLED, enabled), 3);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileLevitator(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (type == ModBlockEntities.LEVITATOR.get()) {
            if (level.isClientSide()) {
                return (lvl, pos, st, be) -> TileLevitator.clientTick(lvl, pos, st, (TileLevitator) be);
            } else {
                return (lvl, pos, st, be) -> TileLevitator.serverTick(lvl, pos, st, (TileLevitator) be);
            }
        }
        return null;
    }
}
