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
    public SilverwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        return generateTree(level, random, pos, true);
    }
    public boolean generateTree(WorldGenLevel level, RandomSource random, BlockPos pos, boolean worldGen) {
        int height = random.nextInt(RANDOM_HEIGHT) + MIN_HEIGHT;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (y < 1 || y + height + 1 > level.getMaxBuildHeight()) {
            return false;
        }
        BlockState groundState = level.getBlockState(new BlockPos(x, y - 1, z));
        if (!canSustainTree(groundState)) {
            return false;
        }
        if (!hasSpaceForTree(level, x, y, z, height)) {
            return false;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        generateLeafCanopy(level, random, x, y, z, height);
        generateTrunk(level, random, x, y, z, height);
        if (worldGen) {
            generateShimmerleaf(level, random, pos);
        }
        return true;
    }
    private boolean hasSpaceForTree(WorldGenLevel level, int x, int y, int z, int height) {
        for (int i1 = y; i1 <= y + 1 + height; ++i1) {
            byte spread = 1;
            if (i1 == y) {
                spread = 0;
            }
            if (i1 >= y + 1 + height - 2) {
                spread = 3;
            }
            for (int j1 = x - spread; j1 <= x + spread; ++j1) {
                for (int k1 = z - spread; k1 <= z + spread; ++k1) {
                    if (i1 >= level.getMinBuildHeight() && i1 < level.getMaxBuildHeight()) {
                        BlockState state = level.getBlockState(new BlockPos(j1, i1, k1));
                        if (state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced()) {
                            continue;
                        }
                        if (i1 > y) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private boolean canSustainTree(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(BlockTags.DIRT);
    }
    private void generateLeafCanopy(WorldGenLevel level, RandomSource random, int x, int y, int z, int height) {
        int leafStart = y + height - 5;
        int leafEnd = y + height + 3 + random.nextInt(3);
        for (int ly = leafStart; ly <= leafEnd; ly++) {
            int centerY = Mth.clamp(ly, y + height - 3, y + height);
            for (int lx = x - 5; lx <= x + 5; lx++) {
                for (int lz = z - 5; lz <= z + 5; lz++) {
                    double dx = lx - x;
                    double dy = ly - centerY;
                    double dz = lz - z;
                    double dist = dx * dx + dy * dy + dz * dz;
                    BlockPos leafPos = new BlockPos(lx, ly, lz);
                    BlockState state = level.getBlockState(leafPos);
                    if (dist < 10 + random.nextInt(8) && canPlaceLeaf(level, state, leafPos)) {
                        setBlockSafe(level, leafPos, ModBlocks.SILVERWOOD_LEAVES.get().defaultBlockState());
                    }
                }
            }
        }
    }
    private void generateTrunk(WorldGenLevel level, RandomSource random, int x, int y, int z, int height) {
        for (int ty = 0; ty < height; ty++) {
            BlockPos pos = new BlockPos(x, y + ty, z);
            BlockState state = level.getBlockState(pos);
            if (isReplaceable(level, state, pos)) {
                setBlockSafe(level, pos, ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
                setBlockSafe(level, new BlockPos(x - 1, y + ty, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
                setBlockSafe(level, new BlockPos(x + 1, y + ty, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
                setBlockSafe(level, new BlockPos(x, y + ty, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
                setBlockSafe(level, new BlockPos(x, y + ty, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
            }
        }
        setBlockSafe(level, new BlockPos(x, y + height, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x - 1, y, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 1, y, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x - 1, y, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 1, y, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        if (random.nextInt(3) != 0) {
            setBlockSafe(level, new BlockPos(x - 1, y + 1, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) != 0) {
            setBlockSafe(level, new BlockPos(x + 1, y + 1, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) != 0) {
            setBlockSafe(level, new BlockPos(x - 1, y + 1, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) != 0) {
            setBlockSafe(level, new BlockPos(x + 1, y + 1, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        setBlockSafe(level, new BlockPos(x - 2, y, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 2, y, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, y, z - 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, y, z + 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x - 2, y - 1, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 2, y - 1, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, y - 1, z - 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, y - 1, z + 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        int flareY = y + (height - 4);
        setBlockSafe(level, new BlockPos(x - 1, flareY, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 1, flareY, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x - 1, flareY, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 1, flareY, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        int lowerFlareY = y + (height - 5);
        if (random.nextInt(3) == 0) {
            setBlockSafe(level, new BlockPos(x - 1, lowerFlareY, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) == 0) {
            setBlockSafe(level, new BlockPos(x + 1, lowerFlareY, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) == 0) {
            setBlockSafe(level, new BlockPos(x - 1, lowerFlareY, z + 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        if (random.nextInt(3) == 0) {
            setBlockSafe(level, new BlockPos(x + 1, lowerFlareY, z - 1), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        }
        setBlockSafe(level, new BlockPos(x - 2, flareY, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x + 2, flareY, z), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, flareY, z - 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
        setBlockSafe(level, new BlockPos(x, flareY, z + 2), ModBlocks.SILVERWOOD_LOG.get().defaultBlockState());
    }
    private void generateShimmerleaf(WorldGenLevel level, RandomSource random, BlockPos treePos) {
        for (int i = 0; i < 18; i++) {
            int fx = treePos.getX() + random.nextInt(8) - random.nextInt(8);
            int fy = treePos.getY() + random.nextInt(4) - random.nextInt(4);
            int fz = treePos.getZ() + random.nextInt(8) - random.nextInt(8);
            BlockPos flowerPos = new BlockPos(fx, fy, fz);
            BlockPos groundPos = flowerPos.below();
            if (level.getBlockState(flowerPos).isAir()) {
                BlockState ground = level.getBlockState(groundPos);
                if (ground.is(Blocks.GRASS_BLOCK) || ground.is(BlockTags.DIRT)) {
                    level.setBlock(flowerPos, ModBlocks.SHIMMERLEAF.get().defaultBlockState(), 2);
                }
            }
        }
    }
    private boolean canPlaceLeaf(LevelAccessor level, BlockState state, BlockPos pos) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }
    private boolean isReplaceable(LevelAccessor level, BlockState state, BlockPos pos) {
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }
    private void setBlockSafe(WorldGenLevel level, BlockPos pos, BlockState state) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(BlockTags.LEAVES) || existing.canBeReplaced()) {
            level.setBlock(pos, state, 2);
        }
    }
}
