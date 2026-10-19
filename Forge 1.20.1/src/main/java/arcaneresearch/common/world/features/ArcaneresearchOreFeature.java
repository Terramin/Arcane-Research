package arcaneresearch.common.world.features;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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
public class ArcaneresearchOreFeature extends Feature<NoneFeatureConfiguration> {
    private static final int DEEP_MIN_Y = -60;
    private final OreType oreType;
    public ArcaneresearchOreFeature(Codec<NoneFeatureConfiguration> codec, OreType oreType) {
        super(codec);
        this.oreType = oreType;
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (!isEnabled()) {
            return false;
        }
        ChunkPos chunkPos = new ChunkPos(context.origin());
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
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
            randPosY = Math.max(level.getMinBuildHeight(),
                    Math.min(randPosY, level.getMaxBuildHeight() - 1));
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
