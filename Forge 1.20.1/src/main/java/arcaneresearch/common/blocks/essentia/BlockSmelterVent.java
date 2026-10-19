package arcaneresearch.common.blocks.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
public class BlockSmelterVent extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 8.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 2.0, 8.0, 14.0, 14.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0, 2.0, 2.0, 8.0, 14.0, 14.0);
    private static final VoxelShape SHAPE_EAST = Block.box(8.0, 2.0, 2.0, 16.0, 14.0, 14.0);
    public BlockSmelterVent() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.0f, 10.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (!clicked.getAxis().isHorizontal()) {
            clicked = Direction.NORTH;
        }
        return this.defaultBlockState()
                .setValue(FACING, clicked.getOpposite());
    }
    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level,
                              net.minecraft.core.BlockPos pos) {
        Direction facing = state.getValue(FACING);
        net.minecraft.core.BlockPos supportPos = pos.relative(facing);
        BlockState support = level.getBlockState(supportPos);
        if (!(support.getBlock() instanceof BlockSmelter)) {
            return false;
        }
        return support.getValue(BlockSmelter.FACING) != facing.getOpposite();
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }
}
