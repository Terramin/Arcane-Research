package arcaneresearch.common.world.features;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.init.ModBlocks;
public class GrassAmbientFeature extends Feature<NoneFeatureConfiguration> {
    public GrassAmbientFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int cornerX = (origin.getX() >> 4) << 4;
        int cornerZ = (origin.getZ() >> 4) << 4;
        for (int a = 0; a < 3; ++a) {
            BlockPos pp = new BlockPos(
                    cornerX + 4 + random.nextInt(8), origin.getY(), cornerZ + 4 + random.nextInt(8));
            pp = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, pp);
            while (pp.getY() > 30 && !level.getBlockState(pp).is(Blocks.GRASS_BLOCK)) {
                pp = pp.below();
            }
            if (level.getBlockState(pp).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(pp, ModBlocks.GRASS_AMBIENT.get().defaultBlockState(), 2);
                return true;
            }
        }
        return false;
    }
}
