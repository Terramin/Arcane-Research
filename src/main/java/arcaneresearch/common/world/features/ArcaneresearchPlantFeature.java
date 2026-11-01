package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.init.ModBlocks;

import java.util.function.Supplier;

public class ArcaneresearchPlantFeature extends Feature<NoneFeatureConfiguration> {

    private static final int HORIZONTAL_RANGE = 8;
    private static final int VERTICAL_RANGE   = 4;
    private static final int CEILING_SEARCH   = 32;

    private final PlantType plantType;

    public ArcaneresearchPlantFeature(Codec<NoneFeatureConfiguration> codec, PlantType plantType) {
        super(codec);
        this.plantType = plantType;
    }

    public static boolean generatePlantCluster(WorldGenLevel level, RandomSource random,
                                               BlockPos origin, PlantType type) {
        BlockState plantState = type.getState();

        int ox = origin.getX();
        int oy = origin.getY();
        int oz = origin.getZ();
        int updateFlag = type.updateFlag;
        int tries      = type.tries;

        BlockPos.MutableBlockPos cursor  = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        int placed = 0;

        for (int i = 0; i < tries; i++) {
            int x = ox + random.nextInt(HORIZONTAL_RANGE) - random.nextInt(HORIZONTAL_RANGE);
            int y = oy + random.nextInt(VERTICAL_RANGE)   - random.nextInt(VERTICAL_RANGE);
            int z = oz + random.nextInt(HORIZONTAL_RANGE) - random.nextInt(HORIZONTAL_RANGE);

            cursor.set(x, y, z);
            if (!level.getBlockState(cursor).isAir()) continue;

            cursor.set(x, y - 1, z);
            BlockState ground = level.getBlockState(cursor);

            if (!isValidGround(level, ground, type, x, y, z, scratch)) continue;

            cursor.set(x, y, z);
            level.setBlock(cursor, plantState, updateFlag);
            placed++;
        }
        return placed > 0;
    }

    private static boolean isValidGround(WorldGenLevel level, BlockState ground,
                                         PlantType type, int x, int y, int z,
                                         BlockPos.MutableBlockPos scratch) {
        if (type.requiresSand()) {
            return ground.is(Blocks.SAND)
                    || ground.is(Blocks.RED_SAND)
                    || ground.is(Blocks.GRASS_BLOCK);
        }
        if (type.requiresCave()) {
            boolean caveOk = (ground.is(BlockTags.BASE_STONE_OVERWORLD)
                    || ground.is(BlockTags.DIRT)
                    || ground.is(Blocks.GRAVEL))
                    && hasCeiling(level, x, y, z, scratch);
            if (caveOk) return true;
            return (ground.is(Blocks.GRASS_BLOCK) || ground.is(BlockTags.DIRT))
                    && isAdjacentToLog(level, x, y, z, scratch);
        }
        return ground.is(Blocks.GRASS_BLOCK) || ground.is(BlockTags.DIRT);
    }

    private static boolean isAdjacentToLog(WorldGenLevel level, int x, int y, int z,
                                           BlockPos.MutableBlockPos scratch) {
        for (int xx = -1; xx <= 1; xx++) {
            for (int yy = -1; yy <= 1; yy++) {
                for (int zz = -1; zz <= 1; zz++) {
                    if (xx == 0 && yy == 0 && zz == 0) continue;
                    scratch.set(x + xx, y + yy, z + zz);
                    if (level.getBlockState(scratch).is(BlockTags.LOGS)) return true;
                }
            }
        }
        return false;
    }

    private static boolean hasCeiling(WorldGenLevel level, int x, int y, int z,
                                      BlockPos.MutableBlockPos scratch) {
        int endY = Math.min(y + CEILING_SEARCH, level.getMaxBuildHeight());
        for (int cy = y + 1; cy < endY; cy++) {
            scratch.set(x, cy, z);
            BlockState state = level.getBlockState(scratch);
            if (!state.isAir() && !state.canBeReplaced()) return true;
        }
        return false;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return generatePlantCluster(context.level(), context.random(),
                context.origin(), plantType);
    }

    public enum PlantType {
        CINDERPEARL(ModBlocks.CINDERPEARL, true,  false, 18, 3),
        SHIMMERLEAF(ModBlocks.SHIMMERLEAF, false, false, 18, 2),
        VISHROOM   (ModBlocks.VISHROOM,    false, true,  36, 2);

        private final Supplier<Block> blockSupplier;
        private final boolean requiresSand;
        private final boolean requiresCave;
        private final int tries;
        private final int updateFlag;

        private volatile BlockState cachedState;

        PlantType(Supplier<Block> blockSupplier, boolean requiresSand, boolean requiresCave,
                  int tries, int updateFlag) {
            this.blockSupplier = blockSupplier;
            this.requiresSand = requiresSand;
            this.requiresCave = requiresCave;
            this.tries = tries;
            this.updateFlag = updateFlag;
        }

        public BlockState getState() {
            BlockState s = cachedState;
            if (s == null) {
                s = blockSupplier.get().defaultBlockState();
                cachedState = s;
            }
            return s;
        }

        public Block getBlock() { return blockSupplier.get(); }

        public boolean requiresSand() { return requiresSand; }
        public boolean requiresCave() { return requiresCave; }
    }
}