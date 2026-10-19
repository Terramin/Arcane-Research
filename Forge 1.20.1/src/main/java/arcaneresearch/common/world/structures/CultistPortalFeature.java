package arcaneresearch.common.world.structures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;
import arcaneresearch.init.ModEntities;
public class CultistPortalFeature extends Feature<NoneFeatureConfiguration> {
    public CultistPortalFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
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
            EntityCultistPortalLesser portal = new EntityCultistPortalLesser(
                    ModEntities.CULTIST_PORTAL_LESSER.get(), level.getLevel());
            portal.moveTo(at.getX() + 0.5, at.getY() + 1.0, at.getZ() + 0.5,
                    random.nextFloat() * 360.0f, 0.0f);
            DifficultyInstance difficulty = level.getCurrentDifficultyAt(at);
            portal.finalizeSpawn(level, difficulty, MobSpawnType.CHUNK_GENERATION, null, null);
            level.addFreshEntityWithPassengers(portal);
            return true;
        }
        return false;
    }
    private boolean isValidGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(Blocks.SAND)
                || state.is(Blocks.RED_SAND)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK);
    }
}
