package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.init.ModBlocks;

public class GrassAmbientFeature extends Feature<NoneFeatureConfiguration> {

    private static final int ATTEMPTS        = 3;
    private static final int CHUNK_OFFSET    = 4;
    private static final int CHUNK_RANGE     = 8;
    private static final int MIN_SEARCH_Y    = 30;
    private static final int SET_BLOCK_FLAGS = 2;

    public GrassAmbientFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level  = context.level();
        RandomSource  random = context.random();
        BlockPos      origin = context.origin();

        int cornerX = origin.getX() & ~15;
        int cornerZ = origin.getZ() & ~15;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int a = 0; a < ATTEMPTS; a++) {
            int x = cornerX + CHUNK_OFFSET + random.nextInt(CHUNK_RANGE);
            int z = cornerZ + CHUNK_OFFSET + random.nextInt(CHUNK_RANGE);

            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            cursor.set(x, y, z);

            BlockState state = level.getBlockState(cursor);
            while (cursor.getY() > MIN_SEARCH_Y && !state.is(Blocks.GRASS_BLOCK)) {
                cursor.setY(cursor.getY() - 1);
                state = level.getBlockState(cursor);
            }

            if (state.is(Blocks.GRASS_BLOCK)) {
                level.setBlock(cursor,
                        ModBlocks.GRASS_AMBIENT.get().defaultBlockState(),
                        SET_BLOCK_FLAGS);
                return true;
            }
        }
        return false;
    }
}