package arcaneresearch.discovered.table;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import arcaneresearch.api.items.IScribeTools;

 












 
public class DiscoveredTableBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE_Z = makeShape(false);
    private static final VoxelShape SHAPE_X = makeShape(true);

    public DiscoveredTableBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getAxis());
    }

     


 
    private static VoxelShape makeShape(boolean rot) {
        double[][] boxes = {
                {0, 12, 0, 16, 16, 16},
                {10, 0, 6, 14, 12, 10},
                {2, 0, 6, 6, 12, 10},
                {0, 0, 4, 16, 4, 12}};
        VoxelShape shape = Shapes.empty();
        for (double[] b : boxes) {
            double x1 = b[0], y1 = b[1], z1 = b[2], x2 = b[3], y2 = b[4], z2 = b[5];
            shape = Shapes.or(shape, rot
                    ? Block.box(16.0 - z2, y1, x1, 16.0 - z1, y2, x2)
                    : Block.box(x1, y1, z1, x2, y2, z2));
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    
    

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof IScribeTools)) {
            return InteractionResult.PASS;
        }
        Direction side = findNeighbourTable(level, pos);
        if (side == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos sidePos = pos.relative(side);
        Block noteTable = DiscoveredTableSetup.getBlock();
        if (noteTable == null || noteTable == net.minecraft.world.level.block.Blocks.AIR) {
            return InteractionResult.PASS;
        }
        level.setBlock(pos, noteTable.defaultBlockState()
                .setValue(CognitionNoteTableBlock.FACING, side)
                .setValue(CognitionNoteTableBlock.PART, CognitionNoteTableBlock.Part.MAIN), 3);
        level.setBlock(sidePos, noteTable.defaultBlockState()
                .setValue(CognitionNoteTableBlock.FACING, side.getOpposite())
                .setValue(CognitionNoteTableBlock.PART, CognitionNoteTableBlock.Part.SIDE), 3);
        if (!player.getAbilities().instabuild) {
            held.hurtAndBreak(1, player, hand);
        }
        level.playSound(null, pos, SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
        return InteractionResult.CONSUME;
    }

      
    private static Direction findNeighbourTable(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(dir);
            if (level.getBlockState(neighbour).getBlock() instanceof DiscoveredTableBlock) {
                return dir;
            }
        }
        return null;
    }
}
