package arcaneresearch.common.blocks.essentia;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty; 
import net.minecraft.world.level.material.MapColor;

public class BlockSmelterAux extends Block {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockSmelterAux(BlockBehaviour.Properties props) {
        super(props
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
}
