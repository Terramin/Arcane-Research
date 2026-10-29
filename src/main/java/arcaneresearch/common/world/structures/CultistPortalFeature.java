package arcaneresearch.common.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;
import arcaneresearch.init.ModEntities;

public class CultistPortalFeature implements Feature {

    public static final MapCodec<CultistPortalFeature> CODEC =
            MapCodec.unit(new CultistPortalFeature());

    public CultistPortalFeature() {
    }

    @Override
    public MapCodec<CultistPortalFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        if (level == null || random == null || origin == null) {
            return false;
        }

        var entityType = ModEntities.CULTIST_PORTAL_LESSER.get();
        var serverLevel = level.getLevel();

        for (int dy = -4; dy <= 4; dy++) {
            BlockPos at = origin.offset(0, dy, 0);
            BlockState ground = level.getBlockState(at.below());
            if (!isValidGround(ground)) {
                continue;
            }
            BlockState state = level.getBlockState(at);
            if (!state.isAir() && !state.canBeReplaced()) {
                continue;
            }
            try {
                EntityCultistPortalLesser portal = new EntityCultistPortalLesser(
                        entityType, serverLevel);
                portal.snapTo(at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5,
                        random.nextFloat() * 360.0f, 0.0f);
                DifficultyInstance difficulty = level.getCurrentDifficultyAt(at);
                portal.finalizeSpawn(level, difficulty, EntitySpawnReason.CHUNK_GENERATION, null);
                level.addFreshEntityWithPassengers(portal);
                return true;
            } catch (Exception e) {
                arcaneresearch.Arcaneresearch.LOGGER.warn("CultistPortalFeature: failed to spawn portal at {}", at, e);
            }
        }
        return false;
    }

    private boolean isValidGround(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK);
    }
}