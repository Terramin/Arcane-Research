package arcaneresearch.common.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.init.ModBlocks;

import java.util.function.Supplier;

// NEOFORGE_PORT: Feature is now an interface (no generics, no FeaturePlaceContext /
// NoneFeatureConfiguration): implements Feature with MapCodec codec() + place(
// WorldGenLevel, ChunkGenerator, RandomSource, BlockPos). Codec is MapCodec.unit(...):
// features are placed from code with fixed params (never deserialized from datapacks),
// registry-id dispatch resolves the registered instance on load.
public class ArcaneresearchPlantFeature implements Feature {

    public static final MapCodec<ArcaneresearchPlantFeature> CODEC =
            MapCodec.unit(new ArcaneresearchPlantFeature(PlantType.CINDERPEARL));

    private final PlantType plantType;

    public ArcaneresearchPlantFeature(PlantType plantType) {
        this.plantType = plantType;
    }

    public static boolean generatePlantCluster(WorldGenLevel level, RandomSource random,
                                               BlockPos origin, PlantType type) {

        int placed = 0;

        for (int i = 0; i < type.tries; i++) {
            int x = origin.getX() + random.nextInt(8) - random.nextInt(8);
            int y = origin.getY() + random.nextInt(4) - random.nextInt(4);
            int z = origin.getZ() + random.nextInt(8) - random.nextInt(8);

            BlockPos pos = new BlockPos(x, y, z);
            BlockPos groundPos = pos.below();

            if (!level.getBlockState(pos).isAir()) {
                continue;
            }

            BlockState groundState = level.getBlockState(groundPos);

            if (type.requiresSand()) {

                if (!groundState.is(Blocks.SAND) && !groundState.is(Blocks.RED_SAND)
                        && !groundState.is(Blocks.GRASS_BLOCK)) {
                    continue;
                }
            } else if (type.requiresCave()) {

                boolean caveOk = (groundState.is(BlockTags.BASE_STONE_OVERWORLD)
                        || groundState.is(BlockTags.DIRT)
                        || groundState.is(Blocks.GRAVEL)) && hasCeiling(level, pos);
                boolean surfaceOk = (groundState.is(Blocks.GRASS_BLOCK) || groundState.is(BlockTags.DIRT))
                        && isAdjacentToLog(level, pos);
                if (!caveOk && !surfaceOk) {
                    continue;
                }
            } else {

                if (!groundState.is(Blocks.GRASS_BLOCK) && !groundState.is(BlockTags.DIRT)) {
                    continue;
                }
            }

            level.setBlock(pos, type.getBlock().defaultBlockState(), type.updateFlag);
            placed++;
        }

        return placed > 0;
    }

    private static boolean isAdjacentToLog(WorldGenLevel level, BlockPos pos) {
        for (int xx = -1; xx <= 1; ++xx) {
            for (int yy = -1; yy <= 1; ++yy) {
                for (int zz = -1; zz <= 1; ++zz) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    if (level.getBlockState(pos.offset(xx, yy, zz)).is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasCeiling(WorldGenLevel level, BlockPos pos) {
        for (int y = pos.getY() + 1; y < pos.getY() + 32 && y < level.getMaxY(); y++) {
            BlockState state = level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ()));
            if (!state.isAir() && !state.canBeReplaced()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MapCodec<ArcaneresearchPlantFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        return generatePlantCluster(level, random, origin, plantType);
    }

    public enum PlantType {
        CINDERPEARL(() -> ModBlocks.CINDERPEARL.get(), true, false, 18, 3),
        SHIMMERLEAF(() -> ModBlocks.SHIMMERLEAF.get(), false, false, 18, 2),
        VISHROOM(() -> ModBlocks.VISHROOM.get(), false, true, 36, 2);

        private final Supplier<Block> blockSupplier;
        private final boolean requiresSand;
        private final boolean requiresCave;
        private final int tries;
        private final int updateFlag;

        PlantType(Supplier<Block> blockSupplier, boolean requiresSand, boolean requiresCave, int tries, int updateFlag) {
            this.blockSupplier = blockSupplier;
            this.requiresSand = requiresSand;
            this.requiresCave = requiresCave;
            this.tries = tries;
            this.updateFlag = updateFlag;
        }

        public Block getBlock() {
            return blockSupplier.get();
        }

        public boolean requiresSand() {
            return requiresSand;
        }

        public boolean requiresCave() {
            return requiresCave;
        }
    }
}
