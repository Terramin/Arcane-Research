package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class BlockShimmerleaf extends BushBlock {

    protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);

    public BlockShimmerleaf(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 6)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) ||
                state.is(BlockTags.DIRT);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {

            float xr = (float) (pos.getX() + 0.5f + random.nextGaussian() * 0.1);
            float yr = (float) (pos.getY() + 0.4f + random.nextGaussian() * 0.1);
            float zr = (float) (pos.getZ() + 0.5f + random.nextGaussian() * 0.1);
            FXDispatcher.INSTANCE.drawWispyMotes(xr, yr, zr,
                    random.nextGaussian() * 0.01, random.nextGaussian() * 0.01, random.nextGaussian() * 0.01,
                    10, 0.3f + level.getRandom().nextFloat() * 0.3f,
                    0.7f + level.getRandom().nextFloat() * 0.3f, 0.7f + level.getRandom().nextFloat() * 0.3f, 0.0f);
        }
    }
}