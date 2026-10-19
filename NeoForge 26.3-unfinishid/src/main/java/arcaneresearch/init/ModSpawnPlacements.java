package arcaneresearch.init;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.monster.EntityFireBat;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.common.entities.monster.EntityThaumicSlime;
import arcaneresearch.common.entities.monster.EntityWisp;

import java.util.function.BooleanSupplier;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModSpawnPlacements.java (same order, same placements).
// REQUIRED changes only:
//   SpawnPlacementRegisterEvent (forge) -> RegisterSpawnPlacementsEvent (neo, MOD bus)
//   SpawnPlacements.Type.ON_GROUND/NO_RESTRICTIONS -> SpawnPlacementTypes.ON_GROUND/NO_RESTRICTIONS
//   MobSpawnType -> EntitySpawnReason (MobSpawnType.NATURAL -> EntitySpawnReason.NATURAL)
// Entity types, heightmaps, config toggles and inner predicates preserved verbatim.
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class ModSpawnPlacements {

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        Arcaneresearch.LOGGER.info("Registering Arcaneresearch spawn placements");

        event.register(
                ModEntities.BRAINY_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnAngryZombie, Monster::checkMonsterSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.GIANT_BRAINY_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.WISP.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnWisp, EntityWisp::checkWispSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.FIRE_BAT.get(),
                SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnFireBat, EntityFireBat::checkFireBatSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.PECH.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> allowNaturalSpawn(type, level, spawnType, pos, random,
                        () -> ModConfig.CONFIG_WORLD.allowSpawnPech, EntityPech::checkPechSpawnRules),
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.TAINT_CRAWLER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.THAUMIC_SLIME.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                EntityThaumicSlime::checkThaumicSlimeSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.MIND_SPIDER.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.ELDRITCH_CRAB.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.ELDRITCH_GUARDIAN.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.CULTIST.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.CULTIST_KNIGHT.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.CULTIST_CLERIC.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        event.register(
                ModEntities.INHABITED_ZOMBIE.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.AND
        );

        Arcaneresearch.LOGGER.info("Registered Arcaneresearch spawn placements");
    }

    private static <T extends Mob> boolean allowNaturalSpawn(EntityType<T> type, ServerLevelAccessor level,
                                                             EntitySpawnReason spawnType, BlockPos pos, RandomSource random,
                                                             BooleanSupplier toggle, SpawnPlacements.SpawnPredicate<T> inner) {
        if (spawnType == EntitySpawnReason.NATURAL && !toggle.getAsBoolean()) {
            return false;
        }
        return inner.test(type, level, spawnType, pos, random);
    }
}
