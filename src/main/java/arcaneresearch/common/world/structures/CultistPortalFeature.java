package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.event.ForgeEventFactory; // <- Новый импорт
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;
import arcaneresearch.init.ModEntities;
import org.jetbrains.annotations.NotNull;

public class CultistPortalFeature extends Feature<NoneFeatureConfiguration> {
    public CultistPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        EntityType<EntityCultistPortalLesser> type = ModEntities.CULTIST_PORTAL_LESSER.get();
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
                EntityCultistPortalLesser portal = new EntityCultistPortalLesser(type, serverLevel);
                portal.moveTo(at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5,
                        random.nextFloat() * 360.0f, 0.0f);
                DifficultyInstance difficulty = level.getCurrentDifficultyAt(at);

                // 🟢 Безопасная замена прямого вызова finalizeSpawn
                ForgeEventFactory.onFinalizeSpawn(portal, level, difficulty,
                        MobSpawnType.CHUNK_GENERATION, null, null);

                level.addFreshEntityWithPassengers(portal);
                return true;
            } catch (Exception e) {
                Arcaneresearch.LOGGER.warn("CultistPortalFeature: failed to spawn portal at {}", at, e);
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