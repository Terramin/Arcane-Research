package arcaneresearch.common.world.features;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.blocks.world.ore.BlockCrystalTC;
import arcaneresearch.common.blocks.world.ore.ShardType;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.world.biomes.BiomeHandler;

// NEOFORGE_PORT: Feature is now an interface (no generics, no FeaturePlaceContext /
// NoneFeatureConfiguration): implements Feature with MapCodec codec() + place(
// WorldGenLevel, ChunkGenerator, RandomSource, BlockPos). Codec is MapCodec.unit(...):
// features are placed from code with fixed params (never deserialized from datapacks),
// registry-id dispatch resolves the registered instance on load.
public class CrystalClusterFeature implements Feature {

    public static final MapCodec<CrystalClusterFeature> CODEC =
            MapCodec.unit(new CrystalClusterFeature());

    public CrystalClusterFeature() {
    }

    @Override
    public MapCodec<CrystalClusterFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (!ModConfig.generateCrystals) {
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
        int maxCrystals = Math.round(64.0f * density);
        int cc = 0;
        int placed = 0;

        int attempts = Math.round(8 * density);
        for (int j = 0; j < attempts; j++) {

            int randPosX = chunkX * 16 + 8 + random.nextInt(13) - 6;
            int randPosZ = chunkZ * 16 + 8 + random.nextInt(13) - 6;
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, randPosX, randPosZ);

            int minY = level.getMinY();
            int randPosY = minY + random.nextInt(Math.max(5, surfaceY - 5 - minY));
            BlockPos bp = new BlockPos(randPosX, randPosY, randPosZ);

            int md = random.nextInt(6);
            if (random.nextInt(3) == 0) {
                Aspect tag = BiomeHandler.getRandomBiomeTag(level.getBiome(bp), random);
                md = tag == null ? random.nextInt(6) : ShardType.getMetaByAspect(tag);
            }
            Block oreBlock = ShardType.byMetadata(md).getOre();
            if (!(oreBlock instanceof BlockCrystalTC)) {
                continue;
            }

            for (int xx = -1; xx <= 1; xx++) {
                for (int yy = -1; yy <= 1; yy++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        if (random.nextInt(3) == 0) {
                            continue;
                        }
                        BlockPos target = bp.offset(xx, yy, zz);
                        if (target.getY() < level.getMinY()
                                || target.getY() >= level.getMaxY()) {
                            continue;
                        }
                        BlockState bs = level.getBlockState(target);
                        if (!level.getFluidState(target).isEmpty()) {
                            continue;
                        }
                        if ((level.isEmptyBlock(target) || bs.canBeReplaced())
                                && BlockCrystalTC.isTouchingRock(level, target)) {

                            int amt = 1 + random.nextInt(3);
                            BlockState crystalState = oreBlock.defaultBlockState()
                                    .setValue(BlockCrystalTC.SIZE, amt)
                                    .setValue(BlockCrystalTC.GENERATION, 1);
                            level.setBlock(target, crystalState, 0);
                            cc += amt;
                            placed++;
                        }
                    }
                }
            }
            if (cc > maxCrystals) {
                break;
            }
        }
        return placed > 0;
    }
}
