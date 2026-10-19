package arcaneresearch.common.blocks.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.devices.TileLampArcane;
import arcaneresearch.common.tiles.devices.TileLampFertility;
import arcaneresearch.common.tiles.devices.TileLampGrowth;
import javax.annotation.Nullable;
public class BlockLamp extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    private static final VoxelShape SHAPE = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
    private final LampType lampType;
    public BlockLamp(LampType type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .lightLevel(state -> state.getValue(ENABLED) ? 15 : 0));
        this.lampType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(ENABLED, false));
    }
    public static BlockLamp createArcane() {
        return new BlockLamp(LampType.ARCANE);
    }
    public static BlockLamp createGrowth() {
        return new BlockLamp(LampType.GROWTH);
    }
    public static BlockLamp createFertility() {
        return new BlockLamp(LampType.FERTILITY);
    }
    public LampType getLampType() {
        return lampType;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace().getOpposite())
                .setValue(ENABLED, false);
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(facing);
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, facing.getOpposite());
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!canSurvive(state, level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
                return;
            }
            boolean powered = level.hasNeighborSignal(pos);
            boolean enabled = state.getValue(ENABLED);
            if (powered && enabled) {
                level.setBlock(pos, state.setValue(ENABLED, false), 3);
                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof TileLampArcane arcane) {
                    arcane.removeLights();
                }
            } else if (!powered && !enabled) {
                BlockEntity te = level.getBlockEntity(pos);
                if (te instanceof TileLampGrowth growth && growth.charges <= 0) {
                    return;
                }
                if (te instanceof TileLampFertility fertility && fertility.charges <= 0) {
                    return;
                }
                level.setBlock(pos, state.setValue(ENABLED, true), 3);
            }
        }
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock()) && lampType == LampType.ARCANE) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof TileLampArcane arcane) {
                arcane.removeLights();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (lampType) {
            case ARCANE -> new arcaneresearch.common.tiles.devices.TileLampArcane(pos, state);
            case GROWTH -> new arcaneresearch.common.tiles.devices.TileLampGrowth(pos, state);
            case FERTILITY -> new arcaneresearch.common.tiles.devices.TileLampFertility(pos, state);
        };
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return switch (lampType) {
            case ARCANE -> type == arcaneresearch.init.ModBlockEntities.LAMP_ARCANE.get()
                    ? (lvl, p, st, be) -> arcaneresearch.common.tiles.devices.TileLampArcane.serverTick(
                    lvl, p, st, (arcaneresearch.common.tiles.devices.TileLampArcane) be)
                    : null;
            case GROWTH -> type == arcaneresearch.init.ModBlockEntities.LAMP_GROWTH.get()
                    ? (lvl, p, st, be) -> arcaneresearch.common.tiles.devices.TileLampGrowth.serverTick(
                    lvl, p, st, (arcaneresearch.common.tiles.devices.TileLampGrowth) be)
                    : null;
            case FERTILITY -> type == arcaneresearch.init.ModBlockEntities.LAMP_FERTILITY.get()
                    ? (lvl, p, st, be) -> arcaneresearch.common.tiles.devices.TileLampFertility.serverTick(
                    lvl, p, st, (arcaneresearch.common.tiles.devices.TileLampFertility) be)
                    : null;
        };
    }
    public enum LampType {
        ARCANE,
        GROWTH,
        FERTILITY
    }
}
