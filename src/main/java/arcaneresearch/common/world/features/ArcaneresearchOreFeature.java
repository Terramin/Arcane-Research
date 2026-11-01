package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.world.biomes.BiomeHandler;
import arcaneresearch.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

public class ArcaneresearchOreFeature extends Feature<NoneFeatureConfiguration> {

    private static final int DEEP_MIN_Y      = -60;
    private static final int BIOME_CHECK_Y   = 50;
    private static final int SET_BLOCK_FLAGS = 2;
    private static final int BIOME_BIAS      = 6;
    private static final int BIOME_RANGE     = 13;

    private final OreType oreType;
    private final BlockState stoneState;
    private final BlockState deepslateState;
    private final int baseAttempts;

    public ArcaneresearchOreFeature(Codec<NoneFeatureConfiguration> codec, OreType oreType) {
        super(codec);
        this.oreType = oreType;
        Block stoneBlock = switch (oreType) {
            case CINNABAR -> ModBlocks.CINNABAR_ORE.get();
            case QUARTZ -> ModBlocks.QUARTZ_ORE.get();
            case AMBER -> ModBlocks.AMBER_ORE.get();
        };

        Block deepslateBlock = switch (oreType) {
            case CINNABAR -> ModBlocks.DEEPSLATE_CINNABAR_ORE.get();
            case QUARTZ -> ModBlocks.DEEPSLATE_QUARTZ_ORE.get();
            case AMBER -> ModBlocks.DEEPSLATE_AMBER_ORE.get();
        };

        this.stoneState     = stoneBlock.defaultBlockState();
        this.deepslateState = deepslateBlock.defaultBlockState();
        this.baseAttempts   = switch (oreType) {
            case CINNABAR, QUARTZ -> 18;
            case AMBER            -> 20;
        };
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!isEnabled()) return false;

        WorldGenLevel level  = context.level();
        RandomSource  random = context.random();
        ChunkPos      chunk  = new ChunkPos(context.origin());

        int baseX = chunk.getMinBlockX() + 8;
        int baseZ = chunk.getMinBlockZ() + 8;

        if (BiomeHandler.isBiomeBlacklisted(
                level.getBiome(new BlockPos(baseX, BIOME_CHECK_Y, baseZ)))) {
            return false;
        }

        int attempts = Math.round(baseAttempts * (ModConfig.oreDensity / 100.0f));
        if (attempts <= 0) return false;

        int minBuild = level.getMinBuildHeight();
        int maxY     = level.getMaxBuildHeight() - 1;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int i = 0; i < attempts; i++) {
            int px = baseX + random.nextInt(BIOME_RANGE) - BIOME_BIAS;
            int pz = baseZ + random.nextInt(BIOME_RANGE) - BIOME_BIAS;

            int py = switch (oreType) {
                case CINNABAR -> DEEP_MIN_Y + random.nextInt(51 - DEEP_MIN_Y);
                case QUARTZ   -> DEEP_MIN_Y + random.nextInt(64 - DEEP_MIN_Y);
                case AMBER    -> level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, px, pz)
                        - random.nextInt(25);
            };
            py = Mth.clamp(py, minBuild, maxY);

            cursor.set(px, py, pz);
            BlockState current = level.getBlockState(cursor);

            BlockState toPlace = null;
            if (current.is(BlockTags.STONE_ORE_REPLACEABLES)) {
                toPlace = stoneState;
            } else if (current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                toPlace = deepslateState;
            }
            if (toPlace != null) {
                level.setBlock(cursor, toPlace, SET_BLOCK_FLAGS);
                placedAny = true;
            }
        }
        return placedAny;
    }

    private boolean isEnabled() {
        return switch (oreType) {
            case CINNABAR -> ModConfig.generateCinnabar;
            case QUARTZ   -> ModConfig.generateQuartz;
            case AMBER    -> ModConfig.generateAmber;
        };
    }

    public enum OreType {
        CINNABAR,
        QUARTZ,
        AMBER
    }
}