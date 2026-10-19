package arcaneresearch.common.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.world.biomes.BiomeHandler;
import arcaneresearch.init.ModBlocks;

// NEOFORGE_PORT: Feature is now an interface (no generics, no FeaturePlaceContext /
// NoneFeatureConfiguration): implements Feature with MapCodec codec() + place(
// WorldGenLevel, ChunkGenerator, RandomSource, BlockPos). Codec is MapCodec.unit(...):
// features are placed from code with fixed params (never deserialized from datapacks),
// registry-id dispatch resolves the registered instance on load.
public class ArcaneresearchOreFeature implements Feature {

    public static final MapCodec<ArcaneresearchOreFeature> CODEC =
            MapCodec.unit(new ArcaneresearchOreFeature(OreType.CINNABAR));

    private static final int DEEP_MIN_Y = -60;
    private final OreType oreType;

    public ArcaneresearchOreFeature(OreType oreType) {
        this.oreType = oreType;
    }

    @Override
    public MapCodec<ArcaneresearchOreFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!isEnabled()) {
            return false;
        }
        // NEOFORGE_PORT: ChunkPos(BlockPos) ctor removed (record) -> containing().
        ChunkPos chunkPos = ChunkPos.containing(origin);
        // NEOFORGE_PORT: ChunkPos record accessors x()/z().
        int chunkX = chunkPos.x();
        int chunkZ = chunkPos.z();

        if (BiomeHandler.isBiomeBlacklisted(
                level.getBiome(new BlockPos(chunkX * 16 + 8, 50, chunkZ * 16 + 8)))) {
            return false;
        }

        float density = ModConfig.oreDensity / 100.0f;
        boolean placedAny = false;

        int attempts = switch (oreType) {
            case CINNABAR -> Math.round(18.0f * density);
            case QUARTZ -> Math.round(18.0f * density);
            case AMBER -> Math.round(20.0f * density);
        };
        for (int i = 0; i < attempts; i++) {

            int randPosX = chunkX * 16 + 8 + random.nextInt(13) - 6;
            int randPosZ = chunkZ * 16 + 8 + random.nextInt(13) - 6;
            int randPosY = switch (oreType) {

                case CINNABAR -> DEEP_MIN_Y + random.nextInt(51 - DEEP_MIN_Y);
                case QUARTZ -> DEEP_MIN_Y + random.nextInt(64 - DEEP_MIN_Y);
                case AMBER -> level.getHeight(Heightmap.Types.WORLD_SURFACE, randPosX, randPosZ)
                        - random.nextInt(25);
            };
            randPosY = Math.max(level.getMinY(),
                    Math.min(randPosY, level.getMaxY() - 1));
            BlockPos pos = new BlockPos(randPosX, randPosY, randPosZ);

            BlockState current = level.getBlockState(pos);
            Block oreBlock = null;
            if (current.is(BlockTags.STONE_ORE_REPLACEABLES)) {
                oreBlock = stoneVariant();
            } else if (current.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
                oreBlock = deepslateVariant();
            }
            if (oreBlock != null) {

                level.setBlock(pos, oreBlock.defaultBlockState(), 2);
                placedAny = true;
            }
        }
        return placedAny;
    }

    private boolean isEnabled() {
        return switch (oreType) {
            case CINNABAR -> ModConfig.generateCinnabar;
            case QUARTZ -> ModConfig.generateQuartz;
            case AMBER -> ModConfig.generateAmber;
        };
    }

    private Block stoneVariant() {
        return switch (oreType) {
            case CINNABAR -> ModBlocks.CINNABAR_ORE.get();
            case QUARTZ -> ModBlocks.QUARTZ_ORE.get();
            case AMBER -> ModBlocks.AMBER_ORE.get();
        };
    }

    private Block deepslateVariant() {
        return switch (oreType) {
            case CINNABAR -> ModBlocks.DEEPSLATE_CINNABAR_ORE.get();
            case QUARTZ -> ModBlocks.DEEPSLATE_QUARTZ_ORE.get();
            case AMBER -> ModBlocks.DEEPSLATE_AMBER_ORE.get();
        };
    }

    public enum OreType {

        CINNABAR,

        QUARTZ,

        AMBER
    }
}
