package arcaneresearch.common.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.init.ModBlocks;






public class GrassAmbientFeature implements Feature {

    public static final MapCodec<GrassAmbientFeature> CODEC =
            MapCodec.unit(new GrassAmbientFeature());

    public GrassAmbientFeature() {
    }

    @Override
    public MapCodec<GrassAmbientFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {

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
