package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.block.state.properties.EnumProperty; // NEOFORGE_PORT: EnumProperty<Direction> folded into EnumProperty<Direction>
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.tiles.devices.TileVisGenerator;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockVisGenerator extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape SHAPE_UP = Block.box(4.0, 0.0, 4.0, 12.0, 14.0, 12.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(4.0, 2.0, 4.0, 12.0, 16.0, 12.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(4.0, 4.0, 2.0, 12.0, 12.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 14.0);
    private static final VoxelShape SHAPE_EAST = Block.box(2.0, 4.0, 4.0, 16.0, 12.0, 12.0);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0, 4.0, 4.0, 14.0, 12.0, 12.0);

    public BlockVisGenerator(BlockBehaviour.Properties props) {
        super(props

                .mapColor(MapColor.WOOD)
                .strength(2.0f, 20.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        for (Direction face : Direction.values()) {
            BlockEntity te = level.getBlockEntity(pos.relative(face));
            if (te != null) {
                // NEOFORGE_PORT: ForgeCapabilities.ENERGY LazyOptional -> level.getCapability
                // (Capabilities.Energy.BLOCK, pos, side), @Nullable EnergyHandler (no canReceive()).
                var storage = level.getCapability(
                        net.neoforged.neoforge.capabilities.Capabilities.Energy.BLOCK,
                        pos.relative(face), face.getOpposite());
                if (storage != null && canReceive(storage)) {
                    return this.defaultBlockState()
                            .setValue(FACING, face)
                            .setValue(ENABLED, true);
                }
            }
        }

        Direction face = context.getClickedFace();
        if (context.getPlayer() != null) {
            face = Direction.getApproximateNearest(
                    (float) context.getPlayer().getLookAngle().x,
                    (float) context.getPlayer().getLookAngle().y,
                    (float) context.getPlayer().getLookAngle().z);
            if (context.getPlayer().isShiftKeyDown()) {
                face = face.getOpposite();
            }
        }
        return this.defaultBlockState()
                .setValue(FACING, face)
                .setValue(ENABLED, true);
    }

    // NEOFORGE_PORT (26.3): Forge IEnergyStorage.canReceive() ("accepts energy at all", independent
    // of fill level) probed with an aborted transaction. The former "spare capacity" test also
    // matched non-receiving sources (other generators) and skipped full receivers.
    private static boolean canReceive(net.neoforged.neoforge.transfer.energy.EnergyHandler storage) {
        try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
            if (storage.insert(1, tx) > 0) {
                return true;
            }
            // full receiver: make room for the probe, still never committed
            return storage.extract(1, tx) > 0 && storage.insert(1, tx) > 0;
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);

        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            updateEnabled(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        if (!level.isClientSide()) {
            updateEnabled(level, pos, state);
        }
    }

    private void updateEnabled(Level level, BlockPos pos, BlockState state) {
        boolean flag = !level.hasNeighborSignal(pos);
        if (flag != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, flag), 3);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_UP;
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // NEOFORGE_PORT: 1:1 from Forge (vanilla-only + client FXDispatcher, unchanged in 26.x).
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TileVisGenerator generator) {
            // NEOFORGE_PORT: IEnergyStorage.getEnergyStored() -> EnergyHandler.getAmountAsLong().
            if (generator.getAmountAsLong() > 0) {
                Direction face = state.getValue(FACING);
                double x = pos.getX() + 0.5 + (face.getStepX() == 0 ? random.nextGaussian() * 0.1 : face.getStepX() * 0.1);
                double y = pos.getY() + 0.5 + (face.getStepY() == 0 ? random.nextGaussian() * 0.1 : face.getStepY() * 0.1);
                double z = pos.getZ() + 0.5 + (face.getStepZ() == 0 ? random.nextGaussian() * 0.1 : face.getStepZ() * 0.1);
                FXDispatcher.INSTANCE.spark(x, y, z, 0.66f + random.nextFloat(),
                        0.65f + random.nextFloat() * 0.1f, 1.0f, 1.0f, 0.8f);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileVisGenerator(ModBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntities.VIS_GENERATOR.get()
                ? (lvl, pos, st, te) -> TileVisGenerator.serverTick(lvl, pos, st, (TileVisGenerator) te)
                : null;
    }
}