package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;
import arcaneresearch.common.blocks.world.ore.ShardType;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.world.biomes.BiomeHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalClusterFeature extends Feature<NoneFeatureConfiguration> {

    private static final int BIOME_CHECK_Y     = 50;
    private static final int BIOME_BIAS        = 6;
    private static final int BIOME_RANGE       = 13;
    private static final int BASE_ATTEMPTS     = 8;
    private static final int BASE_MAX_CRYSTALS = 64;
    private static final int SET_BLOCK_FLAGS   = 0;
    private static final int GENERATION_VALUE  = 1;

    private static final Map<ShardType, BlockState[]> CRYSTAL_STATES = new ConcurrentHashMap<>();

    public CrystalClusterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static BlockState[] getStates(ShardType type) {
        return CRYSTAL_STATES.computeIfAbsent(type, t -> {
            BlockState base = t.getOre().defaultBlockState();
            BlockState[] arr = new BlockState[4];
            for (int i = 1; i <= 3; i++) {
                arr[i] = base.setValue(BlockCrystalTC.SIZE, i)
                        .setValue(BlockCrystalTC.GENERATION, GENERATION_VALUE);
            }
            return arr;
        });
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!ModConfig.generateCrystals) return false;

        WorldGenLevel level  = context.level();
        RandomSource  random = context.random();
        ChunkPos      chunk  = new ChunkPos(context.origin());

        int baseX = chunk.getMinBlockX() + 8;
        int baseZ = chunk.getMinBlockZ() + 8;

        if (BiomeHandler.isBiomeBlacklisted(
                level.getBiome(new BlockPos(baseX, BIOME_CHECK_Y, baseZ)))) {
            return false;
        }

        float density = ModConfig.oreDensity / 100.0f;
        int maxCrystals = Math.round(BASE_MAX_CRYSTALS * density);
        int attempts    = Math.round(BASE_ATTEMPTS * density);
        if (attempts <= 0) return false;

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int cc = 0;
        int placed = 0;

        for (int j = 0; j < attempts; j++) {
            int px = baseX + random.nextInt(BIOME_RANGE) - BIOME_BIAS;
            int pz = baseZ + random.nextInt(BIOME_RANGE) - BIOME_BIAS;

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, px, pz);
            int yRange   = Math.max(5, surfaceY - 5 - minY);
            int py       = minY + random.nextInt(yRange);

            int md = random.nextInt(6);
            if (random.nextInt(3) == 0) {
                cursor.set(px, py, pz);
                Aspect tag = BiomeHandler.getRandomBiomeTag(level.getBiome(cursor), random);
                md = tag == null ? random.nextInt(6) : ShardType.getMetaByAspect(tag);
            }

            ShardType shard = ShardType.byMetadata(md);
            if (!(shard.getOre() instanceof BlockCrystalTC)) continue;

            BlockState[] states = getStates(shard);

            for (int xx = -1; xx <= 1; xx++) {
                for (int yy = -1; yy <= 1; yy++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        if (random.nextInt(3) == 0) continue;

                        int ty = py + yy;
                        if (ty < minY || ty >= maxY) continue;

                        cursor.set(px + xx, ty, pz + zz);

                        BlockState bs = level.getBlockState(cursor);
                        if (!bs.getFluidState().isEmpty()) continue;
                        if ((bs.isAir() || bs.canBeReplaced())
                                && BlockCrystalTC.isTouchingRock(level, cursor)) {
                            int amt = 1 + random.nextInt(3);
                            level.setBlock(cursor, states[amt], SET_BLOCK_FLAGS);
                            cc += amt;
                            placed++;
                        }
                    }
                }
            }
            if (cc > maxCrystals) break;
        }
        return placed > 0;
    }
}