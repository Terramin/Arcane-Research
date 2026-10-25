package arcaneresearch.init;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.monster.EntityFireBat;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.common.entities.monster.EntityThaumicSlime;
import arcaneresearch.common.entities.monster.EntityWisp;
import java.util.function.BooleanSupplier;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSpawnPlacements {
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        Arcaneresearch.LOGGER.info("Registering Arcaneresearch spawn placements");
        event.register(
                ModEntities.BRAINY_ZOMBIE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnAngryZombie, Monster::checkMonsterSpawnRules),
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.WISP.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnWisp, EntityWisp::checkWispSpawnRules),
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.FIRE_BAT.get(),
                SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnFireBat, EntityFireBat::checkFireBatSpawnRules),
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.PECH.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnPech, EntityPech::checkPechSpawnRules),
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.TAINT_CRAWLER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.THAUMIC_SLIME.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityThaumicSlime::checkThaumicSlimeSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.MIND_SPIDER.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.ELDRITCH_CRAB.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.ELDRITCH_GUARDIAN.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.CULTIST.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.CULTIST_KNIGHT.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.CULTIST_CLERIC.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        event.register(
                ModEntities.INHABITED_ZOMBIE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.AND
        );
        Arcaneresearch.LOGGER.info("Registered Arcaneresearch spawn placements");
    }
    private static <T extends Mob> boolean allowNaturalSpawn(EntityType<T> type, ServerLevelAccessor level,
                                                             MobSpawnType spawnType, BlockPos pos, RandomSource random,
                                                             BooleanSupplier toggle, SpawnPlacements.SpawnPredicate<T> inner) {
        if (spawnType == MobSpawnType.NATURAL && !toggle.getAsBoolean()) {
            return false;
        }
        return inner.test(type, level, spawnType, pos, random);
    }
}
