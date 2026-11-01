package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.init.ModBlocks;

public class SilverwoodTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_HEIGHT = 8;
    private static final int RANDOM_HEIGHT = 5;
    private static final int SET_BLOCK_FLAGS = 2;

    public SilverwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return generateTree(context.level(), context.random(), context.origin(), true);
    }

    public boolean generateTree(WorldGenLevel level, RandomSource random, BlockPos pos, boolean worldGen) {
        int height = random.nextInt(RANDOM_HEIGHT) + MIN_HEIGHT;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (y < 1 || y + height + 1 > level.getMaxBuildHeight()) return false;
        if (!canSustainTree(level.getBlockState(new BlockPos(x, y - 1, z)))) return false;
        if (!hasSpaceForTree(level, x, y, z, height)) return false;

        BlockState logState  = ModBlocks.SILVERWOOD_LOG.get().defaultBlockState();
        BlockState leafState = ModBlocks.SILVERWOOD_LEAVES.get().defaultBlockState();

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), SET_BLOCK_FLAGS);
        generateLeafCanopy(level, random, x, y, z, height, leafState);
        generateTrunk(level, random, x, y, z, height, logState);
        if (worldGen) generateShimmerleaf(level, random, pos);
        return true;
    }

    private boolean hasSpaceForTree(WorldGenLevel level, int x, int y, int z, int height) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i1 = y; i1 <= y + 1 + height; ++i1) {
            if (i1 < minY || i1 >= maxY) return false;

            int spread = 1;
            if (i1 == y) spread = 0;
            if (i1 >= y + 1 + height - 2) spread = 3;

            for (int j1 = x - spread; j1 <= x + spread; ++j1) {
                for (int k1 = z - spread; k1 <= z + spread; ++k1) {
                    pos.set(j1, i1, k1);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced()) continue;
                    if (i1 > y) return false;
                }
            }
        }
        return true;
    }

    private static boolean canSustainTree(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.DIRT);
    }

    private void generateLeafCanopy(WorldGenLevel level, RandomSource random,
                                    int x, int y, int z, int height, BlockState leafState) {
        int leafStart  = y + height - 5;
        int leafEnd    = y + height + 3 + random.nextInt(3);
        int centerMin  = y + height - 3;
        int centerMax  = y + height;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int ly = leafStart; ly <= leafEnd; ly++) {
            int centerY = Mth.clamp(ly, centerMin, centerMax);
            double dy = ly - centerY;
            double dySq = dy * dy;

            for (int lx = x - 5; lx <= x + 5; lx++) {
                double dx = lx - x;
                double dxSq = dx * dx;

                for (int lz = z - 5; lz <= z + 5; lz++) {
                    double dz = lz - z;
                    double dist = dxSq + dySq + dz * dz;

                    if (dist >= 10 + random.nextInt(8)) continue;

                    pos.set(lx, ly, lz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced()) {
                        level.setBlock(pos, leafState, SET_BLOCK_FLAGS);
                    }
                }
            }
        }
    }

    private void generateTrunk(WorldGenLevel level, RandomSource random,
                               int x, int y, int z, int height, BlockState logState) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int ty = 0; ty < height; ty++) {
            pos.set(x, y + ty, z);
            if (!isReplaceable(level, pos)) continue;

            setLog(level, pos, logState);
            pos.set(x - 1, y + ty, z);     setLog(level, pos, logState);
            pos.set(x + 1, y + ty, z);     setLog(level, pos, logState);
            pos.set(x,     y + ty, z - 1); setLog(level, pos, logState);
            pos.set(x,     y + ty, z + 1); setLog(level, pos, logState);
        }

        placeLog(level, pos, x,     y + height, z,     logState);
        placeLog(level, pos, x - 1, y,          z - 1, logState);
        placeLog(level, pos, x + 1, y,          z + 1, logState);
        placeLog(level, pos, x - 1, y,          z + 1, logState);
        placeLog(level, pos, x + 1, y,          z - 1, logState);

        if (random.nextInt(3) != 0) placeLog(level, pos, x - 1, y + 1, z - 1, logState);
        if (random.nextInt(3) != 0) placeLog(level, pos, x + 1, y + 1, z + 1, logState);
        if (random.nextInt(3) != 0) placeLog(level, pos, x - 1, y + 1, z + 1, logState);
        if (random.nextInt(3) != 0) placeLog(level, pos, x + 1, y + 1, z - 1, logState);

        placeLog(level, pos, x - 2, y,     z,     logState);
        placeLog(level, pos, x + 2, y,     z,     logState);
        placeLog(level, pos, x,     y,     z - 2, logState);
        placeLog(level, pos, x,     y,     z + 2, logState);
        placeLog(level, pos, x - 2, y - 1, z,     logState);
        placeLog(level, pos, x + 2, y - 1, z,     logState);
        placeLog(level, pos, x,     y - 1, z - 2, logState);
        placeLog(level, pos, x,     y - 1, z + 2, logState);

        int flareY = y + (height - 4);
        placeLog(level, pos, x - 1, flareY, z - 1, logState);
        placeLog(level, pos, x + 1, flareY, z + 1, logState);
        placeLog(level, pos, x - 1, flareY, z + 1, logState);
        placeLog(level, pos, x + 1, flareY, z - 1, logState);

        int lowerFlareY = y + (height - 5);
        if (random.nextInt(3) == 0) placeLog(level, pos, x - 1, lowerFlareY, z - 1, logState);
        if (random.nextInt(3) == 0) placeLog(level, pos, x + 1, lowerFlareY, z + 1, logState);
        if (random.nextInt(3) == 0) placeLog(level, pos, x - 1, lowerFlareY, z + 1, logState);
        if (random.nextInt(3) == 0) placeLog(level, pos, x + 1, lowerFlareY, z - 1, logState);

        placeLog(level, pos, x - 2, flareY, z,     logState);
        placeLog(level, pos, x + 2, flareY, z,     logState);
        placeLog(level, pos, x,     flareY, z - 2, logState);
        placeLog(level, pos, x,     flareY, z + 2, logState);
    }

    private void generateShimmerleaf(WorldGenLevel level, RandomSource random, BlockPos treePos) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState shimmerState = ModBlocks.SHIMMERLEAF.get().defaultBlockState();
        int tx = treePos.getX(), ty = treePos.getY(), tz = treePos.getZ();

        for (int i = 0; i < 18; i++) {
            int fx = tx + random.nextInt(8) - random.nextInt(8);
            int fy = ty + random.nextInt(4) - random.nextInt(4);
            int fz = tz + random.nextInt(8) - random.nextInt(8);

            pos.set(fx, fy, fz);
            if (!level.getBlockState(pos).isAir()) continue;

            pos.set(fx, fy - 1, fz);
            BlockState ground = level.getBlockState(pos);
            if (ground.is(Blocks.GRASS_BLOCK) || ground.is(BlockTags.DIRT)) {
                pos.set(fx, fy, fz);
                level.setBlock(pos, shimmerState, SET_BLOCK_FLAGS);
            }
        }
    }

    private static void placeLog(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                                 int x, int y, int z, BlockState logState) {
        pos.set(x, y, z);
        setLog(level, pos, logState);
    }

    private static void setLog(WorldGenLevel level, BlockPos pos, BlockState logState) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(BlockTags.LEAVES) || existing.canBeReplaced()) {
            level.setBlock(pos, logState, SET_BLOCK_FLAGS);
        }
    }

    private static boolean isReplaceable(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }
}