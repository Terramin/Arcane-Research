package arcaneresearch.discovered.table;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class CognitionNoteTableBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private static final double[][] BOXES_MAIN = {
            {0, 12, 0, 16, 16, 16},
            {2, 0, 2, 6, 12, 6},
            {2, 0, 10, 6, 12, 14},
            {4, 2, 6, 16, 6, 10},
            {2, 16, 2, 5, 18, 5}};

    private static final double[][] BOXES_SIDE = {
            {0, 12, 0, 16, 16, 16},
            {10, 0, 2, 14, 12, 6},
            {10, 0, 10, 14, 12, 14},
            {0, 2, 6, 12, 6, 10}};

    private static VoxelShape shapeFor(Part part, Direction facing) {
        Direction eff = part == Part.SIDE ? facing.getOpposite() : facing;
        double[][] boxes = part == Part.MAIN ? BOXES_MAIN : BOXES_SIDE;
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            shape = Shapes.or(shape, rotBox(b, eff));
        }
        return shape;
    }

    private static VoxelShape rotBox(double[] b, Direction facing) {
        double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
        return switch (facing) {
            case EAST -> Block.box(x1, y1, z1, x2, y2, z2);
            case SOUTH -> Block.box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2);
            case WEST -> Block.box(16.0 - x2, y1, 16.0 - z2, 16.0 - x1, y2, 16.0 - z1);
            case NORTH -> Block.box(z1, y1, 16.0 - x2, z2, y2, 16.0 - x1);
            default -> Block.box(x1, y1, z1, x2, y2, z2);
        };
    }

    public CognitionNoteTableBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, Part.MAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    public static BlockPos mainPos(BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? pos : pos.relative(state.getValue(FACING));
    }

    private static BlockPos partnerPos(BlockPos pos, BlockState state) {
        return pos.relative(state.getValue(FACING));
    }

    private static boolean isPartner(BlockState state, BlockState neighbour) {
        if (!(neighbour.getBlock() instanceof CognitionNoteTableBlock)) {
            return false;
        }
        return neighbour.getValue(PART) != state.getValue(PART)
                && neighbour.getValue(FACING) == state.getValue(FACING).getOpposite();
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state,
                            @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        Direction facing = state.getValue(FACING);
        BlockPos sidePos = pos.relative(facing);
        if (!level.getBlockState(sidePos).canBeReplaced()) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
            }
            return;
        }
        level.setBlock(sidePos, state.setValue(PART, Part.SIDE)
                .setValue(FACING, facing.getOpposite()), 3);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(@NotNull BlockState state, LevelReader level, @NotNull BlockPos pos) {
        BlockState neighbour = level.getBlockState(partnerPos(pos, state));
        return isPartner(state, neighbour);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState updateShape(BlockState state, @NotNull Direction direction, @NotNull BlockState neighbourState,
                                           @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighbourPos) {
        if (direction == state.getValue(FACING) && !isPartner(state, neighbourState)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public void playerWillDestroy(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide) {
            Block single = DiscoveredTableSetup.getSingleTable();
            if (single != null) {
                BlockPos partner = partnerPos(pos, state);
                if (level.getBlockState(partner).getBlock() instanceof CognitionNoteTableBlock) {
                    level.setBlock(partner, single.defaultBlockState(), 3);
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return shapeFor(state.getValue(PART), state.getValue(FACING));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        BlockPos main = mainPos(pos, state);
        BlockEntity be = level.getBlockEntity(main);
        if (be instanceof CognitionNoteTableTile tile) {
            NetworkHooks.openScreen(serverPlayer, tile, main);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(PART) == Part.MAIN
                    && level.getBlockEntity(pos) instanceof CognitionNoteTableTile tile) {
                tile.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, BlockState state) {
        return state.getValue(PART) == Part.MAIN ? new CognitionNoteTableTile(pos, state) : null;
    }

    public enum Part implements StringRepresentable {
        MAIN("main"),
        SIDE("side");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}