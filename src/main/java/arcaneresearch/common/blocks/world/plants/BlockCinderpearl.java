package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// NEOFORGE_PORT (26.3): 1.20.1 BushBlock == 26.x VegetationBlock; 26.x BushBlock is the bonemeal-spreading bush.
public class BlockCinderpearl extends VegetationBlock {

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public BlockCinderpearl(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_ORANGE)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 8)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND) ||
                state.is(Blocks.RED_SAND) ||
                state.is(Blocks.DIRT) ||
                state.is(Blocks.TERRACOTTA) ||
                state.is(BlockTags.TERRACOTTA) ||
                state.is(BlockTags.SAND) ||
                // NEOFORGE_PORT (26.3): Forge PlantType.DESERT (getPlantType, gone in 26.x) also let
                // the reference survive on glazed terracotta via IForgeBlock.canSustainPlant.
                state.getBlock() instanceof net.minecraft.world.level.block.GlazedTerracottaBlock;
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()) {

            double x = pos.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;
            double y = pos.getY() + 0.6 + (random.nextFloat() - random.nextFloat()) * 0.1;
            double z = pos.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.1;

            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
        }
    }
}
