package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockPlantTC extends BushBlock {

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public BlockPlantTC(Properties properties) {
        super(properties);
    }

    public static BlockPlantTC createShimmerleaf(BlockBehaviour.Properties props) {
        return new BlockPlantTC(props
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 6)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public static BlockPlantTC createCinderpearl(BlockBehaviour.Properties props) {
        return new BlockPlantTC(props
                .mapColor(MapColor.COLOR_ORANGE)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 8)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    public static BlockPlantTC createVishroom(BlockBehaviour.Properties props) {
        return new BlockPlantTC(props
                .mapColor(MapColor.COLOR_PURPLE)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 3)
                .offsetType(BlockBehaviour.OffsetType.XZ)) {
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

}
