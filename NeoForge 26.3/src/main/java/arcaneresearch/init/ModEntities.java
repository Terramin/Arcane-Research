package arcaneresearch.init;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.EntityFallingTaint;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.entities.EntityFollowingItem;
import arcaneresearch.common.entities.EntitySpecialItem;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;
import arcaneresearch.common.entities.monster.*;
import arcaneresearch.common.entities.monster.boss.*;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;
import arcaneresearch.common.entities.monster.cult.EntityCultistKnight;
import arcaneresearch.common.entities.monster.cult.EntityCultistPortalLesser;
import arcaneresearch.common.entities.monster.tainted.*;
import arcaneresearch.common.entities.projectile.*;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModEntities.java (entries + order preserved).
// REQUIRED changes: RegistryObject -> DeferredHolder; Builder.build(String id)
//   -> build(ResourceKey) (26.x signature); Identifier -> Identifier.
// Mob.shouldDespawnInPeaceful() is gone in 26.x: peaceful despawn is the EntityType
// notInPeaceful() flag. Set on every type that despawned in peaceful in Forge (Monster
// subclasses, Slime, Wisp); fire/spell bats keep Forge's per-instance rule in checkDespawn.
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Arcaneresearch.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFocusProjectile>> FOCUS_PROJECTILE =
            ENTITY_TYPES.register("focus_projectile", () ->
                    EntityType.Builder.<EntityFocusProjectile>of(EntityFocusProjectile::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_projectile"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFocusCloud>> FOCUS_CLOUD =
            ENTITY_TYPES.register("focus_cloud", () ->
                    EntityType.Builder.<EntityFocusCloud>of(EntityFocusCloud::new, MobCategory.MISC)
                            .sized(1.0f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_cloud"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFocusMine>> FOCUS_MINE =
            ENTITY_TYPES.register("focus_mine", () ->
                    EntityType.Builder.<EntityFocusMine>of(EntityFocusMine::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_mine"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAlumentum>> ALUMENTUM =
            ENTITY_TYPES.register("alumentum", () ->
                    EntityType.Builder.<EntityAlumentum>of(EntityAlumentum::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "alumentum"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGrapple>> GRAPPLE =
            ENTITY_TYPES.register("grapple", () ->
                    EntityType.Builder.<EntityGrapple>of(EntityGrapple::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "grapple"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBottleTaint>> BOTTLE_TAINT =
            ENTITY_TYPES.register("bottle_taint", () ->
                    EntityType.Builder.<EntityBottleTaint>of(EntityBottleTaint::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "bottle_taint"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHomingShard>> HOMING_SHARD =
            ENTITY_TYPES.register("homing_shard", () ->
                    EntityType.Builder.<EntityHomingShard>of(EntityHomingShard::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "homing_shard"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemDart>> GOLEM_DART =
            ENTITY_TYPES.register("golem_dart", () ->
                    EntityType.Builder.<EntityGolemDart>of(EntityGolemDart::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            // NEOFORGE_PORT (26.3): Forge AbstractArrow.getEyeHeight -> 0.13f (vanilla ARROW type).
                            .eyeHeight(0.13f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "golem_dart"))));

    // NEOFORGE_PORT: порт Forge ModEntities.PRIMAL_ARROW (TC4 parity).
    public static final DeferredHolder<EntityType<?>, EntityType<arcaneresearch.common.entities.projectile.EntityPrimalArrow>> PRIMAL_ARROW =
            ENTITY_TYPES.register("primal_arrow", () ->
                    EntityType.Builder.<arcaneresearch.common.entities.projectile.EntityPrimalArrow>of(
                                    arcaneresearch.common.entities.projectile.EntityPrimalArrow::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            // NEOFORGE_PORT (26.3): Forge AbstractArrow.getEyeHeight -> 0.13f (vanilla ARROW type).
                            .eyeHeight(0.13f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "primal_arrow"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGolemOrb>> GOLEM_ORB =
            ENTITY_TYPES.register("golem_orb", () ->
                    EntityType.Builder.<EntityGolemOrb>of(EntityGolemOrb::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "golem_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchOrb>> ELDRITCH_ORB =
            ENTITY_TYPES.register("eldritch_orb", () ->
                    EntityType.Builder.<EntityEldritchOrb>of(EntityEldritchOrb::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCausalityCollapser>> CAUSALITY_COLLAPSER =
            ENTITY_TYPES.register("causality_collapser", () ->
                    EntityType.Builder.<EntityCausalityCollapser>of(EntityCausalityCollapser::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "causality_collapser"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityRiftBlast>> RIFT_BLAST =
            ENTITY_TYPES.register("rift_blast", () ->
                    EntityType.Builder.<EntityRiftBlast>of(EntityRiftBlast::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "rift_blast"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWisp>> WISP =
            ENTITY_TYPES.register("wisp", () ->
                    EntityType.Builder.<EntityWisp>of(EntityWisp::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.9f, 0.9f)

                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wisp"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFireBat>> FIRE_BAT =
            ENTITY_TYPES.register("fire_bat", () ->
                    EntityType.Builder.<EntityFireBat>of(EntityFireBat::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.9f)

                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fire_bat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityBrainyZombie>> BRAINY_ZOMBIE =
            ENTITY_TYPES.register("brainy_zombie", () ->
                    EntityType.Builder.<EntityBrainyZombie>of(EntityBrainyZombie::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.95f)
                            // NEOFORGE_PORT (26.3): Forge Zombie.getStandingEyeHeight -> 1.74f (adult).
                            .eyeHeight(1.74f)

                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "brainy_zombie"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMindSpider>> MIND_SPIDER =
            ENTITY_TYPES.register("mind_spider", () ->
                    EntityType.Builder.<EntityMindSpider>of(EntityMindSpider::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.7f, 0.5f)
                            // NEOFORGE_PORT (26.3): Forge EntityMindSpider.getStandingEyeHeight -> 0.45f.
                            .eyeHeight(0.45f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "mind_spider"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityThaumicSlime>> THAUMIC_SLIME =
            ENTITY_TYPES.register("thaumic_slime", () ->
                    EntityType.Builder.<EntityThaumicSlime>of(EntityThaumicSlime::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            // NEOFORGE_PORT (26.3): Forge 1.20.1 Slime.getDimensions scaled the 2.04 type
                            // box by 0.255 * size and eye = 0.625 * height; 26.x AbstractCubeMob scales the
                            // type box by size alone (vanilla SLIME: 0.52 / eye 0.325), so the base box is
                            // pre-multiplied here to keep the Forge hitbox and eye height.
                            .sized(2.04f * 0.255f, 2.04f * 0.255f)
                            .eyeHeight(0.625f * 2.04f * 0.255f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "thaumic_slime"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityGiantBrainyZombie>> GIANT_BRAINY_ZOMBIE =
            ENTITY_TYPES.register("giant_brainy_zombie", () ->
                    EntityType.Builder.<EntityGiantBrainyZombie>of(EntityGiantBrainyZombie::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.95f)
                            .eyeHeight(1.74f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "giant_brainy_zombie"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityInhabitedZombie>> INHABITED_ZOMBIE =
            ENTITY_TYPES.register("inhabited_zombie", () ->
                    EntityType.Builder.<EntityInhabitedZombie>of(EntityInhabitedZombie::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.95f)
                            // NEOFORGE_PORT (26.3): Forge Zombie.getStandingEyeHeight -> 1.74f (adult).
                            .eyeHeight(1.74f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "inhabited_zombie"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchCrab>> ELDRITCH_CRAB =
            ENTITY_TYPES.register("eldritch_crab", () ->
                    EntityType.Builder.<EntityEldritchCrab>of(EntityEldritchCrab::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.8f, 0.6f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_crab"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpellBat>> SPELL_BAT =
            ENTITY_TYPES.register("spell_bat", () ->
                    EntityType.Builder.<EntitySpellBat>of(EntitySpellBat::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.9f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "spell_bat"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchGuardian>> ELDRITCH_GUARDIAN =
            ENTITY_TYPES.register("eldritch_guardian", () ->
                    EntityType.Builder.<EntityEldritchGuardian>of(EntityEldritchGuardian::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.8f, 2.25f)
                            // NEOFORGE_PORT (26.3): no type eyeHeight - 1.20.1 overrode only the 1-arg
                            // getEyeHeight(Pose), which never fed the eyeHeight field (effective 0.85 x height).
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_guardian"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultist>> CULTIST =
            ENTITY_TYPES.register("cultist", () ->
                    EntityType.Builder.<EntityCultist>of(EntityCultist::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistKnight>> CULTIST_KNIGHT =
            ENTITY_TYPES.register("cultist_knight", () ->
                    EntityType.Builder.<EntityCultistKnight>of(EntityCultistKnight::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_knight"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistCleric>> CULTIST_CLERIC =
            ENTITY_TYPES.register("cultist_cleric", () ->
                    EntityType.Builder.<EntityCultistCleric>of(EntityCultistCleric::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_cleric"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistPortalLesser>> CULTIST_PORTAL_LESSER =
            ENTITY_TYPES.register("cultist_portal_lesser", () ->
                    EntityType.Builder.<EntityCultistPortalLesser>of(EntityCultistPortalLesser::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.5f, 3.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_portal_lesser"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityPech>> PECH =
            ENTITY_TYPES.register("pech", () ->
                    EntityType.Builder.<EntityPech>of(EntityPech::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.6f, 1.2f)
                            // NEOFORGE_PORT: getEyeHeight final in 26.x; Forge returned
                            // getBbHeight() * 0.66f = 0.792f.
                            // NEOFORGE_PORT (26.3): no type eyeHeight - 1.20.1 overrode only the 1-arg
                            // getEyeHeight(Pose), which never fed the eyeHeight field (effective 0.85 x height).

                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "pech"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintCrawler>> TAINT_CRAWLER =
            ENTITY_TYPES.register("taint_crawler", () ->
                    EntityType.Builder.<EntityTaintCrawler>of(EntityTaintCrawler::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.5f, 0.4f)
                            // NEOFORGE_PORT (26.3): Forge EntityTaintCrawler.getStandingEyeHeight -> 0.1f.
                            .eyeHeight(0.1f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taint_crawler"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSwarm>> TAINT_SWARM =
            ENTITY_TYPES.register("taint_swarm", () ->
                    EntityType.Builder.<EntityTaintSwarm>of(EntityTaintSwarm::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taint_swarm"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintacle>> TAINTACLE =
            ENTITY_TYPES.register("taintacle", () ->
                    EntityType.Builder.<EntityTaintacle>of(EntityTaintacle::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.8f, 3.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taintacle"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintacleSmall>> TAINTACLE_SMALL =
            ENTITY_TYPES.register("taintacle_small", () ->
                    EntityType.Builder.<EntityTaintacleSmall>of(EntityTaintacleSmall::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.22f, 1.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taintacle_small"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSeed>> TAINT_SEED =
            ENTITY_TYPES.register("taint_seed", () ->
                    EntityType.Builder.<EntityTaintSeed>of(EntityTaintSeed::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.5f, 1.25f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taint_seed"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintSeedPrime>> TAINT_SEED_PRIME =
            ENTITY_TYPES.register("taint_seed_prime", () ->
                    EntityType.Builder.<EntityTaintSeedPrime>of(EntityTaintSeedPrime::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taint_seed_prime"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistLeader>> CULTIST_LEADER =
            ENTITY_TYPES.register("cultist_leader", () ->
                    EntityType.Builder.<EntityCultistLeader>of(EntityCultistLeader::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(0.75f, 2.25f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_leader"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTaintacleGiant>> TAINTACLE_GIANT =
            ENTITY_TYPES.register("taintacle_giant", () ->
                    EntityType.Builder.<EntityTaintacleGiant>of(EntityTaintacleGiant::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.1f, 6.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taintacle_giant"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityCultistPortalGreater>> CULTIST_PORTAL_GREATER =
            ENTITY_TYPES.register("cultist_portal_greater", () ->
                    EntityType.Builder.<EntityCultistPortalGreater>of(EntityCultistPortalGreater::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.5f, 3.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_portal_greater"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchGolem>> ELDRITCH_GOLEM =
            ENTITY_TYPES.register("eldritch_golem", () ->
                    EntityType.Builder.<EntityEldritchGolem>of(EntityEldritchGolem::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.75f, 3.5f)
                            // NEOFORGE_PORT (26.3): no type eyeHeight - 1.20.1 overrode only the 1-arg
                            // getEyeHeight(Pose), which never fed the eyeHeight field (effective 0.85 x height).
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_golem"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEldritchWarden>> ELDRITCH_WARDEN =
            ENTITY_TYPES.register("eldritch_warden", () ->
                    EntityType.Builder.<EntityEldritchWarden>of(EntityEldritchWarden::new, MobCategory.MONSTER)
                            .notInPeaceful()
                            .sized(1.5f, 3.5f)
                            // NEOFORGE_PORT (26.3): no type eyeHeight - 1.20.1 overrode only the 1-arg
                            // getEyeHeight(Pose), which never fed the eyeHeight field (effective 0.85 x height).
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_warden"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTurretCrossbow>> TURRET_CROSSBOW =
            ENTITY_TYPES.register("turret_crossbow", () ->
                    EntityType.Builder.<EntityTurretCrossbow>of(EntityTurretCrossbow::new, MobCategory.MISC)
                            .sized(0.95f, 1.25f)
                            // NEOFORGE_PORT (26.3): Forge EntityTurretCrossbow.getStandingEyeHeight -> height * 0.66f.
                            .eyeHeight(1.25f * 0.66f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "turret_crossbow"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityTurretCrossbowAdvanced>> TURRET_CROSSBOW_ADVANCED =
            ENTITY_TYPES.register("turret_crossbow_advanced", () ->
                    EntityType.Builder.<EntityTurretCrossbowAdvanced>of(EntityTurretCrossbowAdvanced::new, MobCategory.MISC)
                            .sized(0.95f, 1.5f)
                            // NEOFORGE_PORT (26.3): Forge EntityTurretCrossbowAdvanced.getStandingEyeHeight -> 1.0f.
                            .eyeHeight(1.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "turret_crossbow_advanced"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityArcaneresearchGolem>> ARCANERESEARCH_GOLEM =
            ENTITY_TYPES.register("arcaneresearch_golem", () ->
                    EntityType.Builder.<EntityArcaneresearchGolem>of(EntityArcaneresearchGolem::new, MobCategory.MISC)
                            // NEOFORGE_PORT (26.3): Forge size 0.4 x 0.9 (port had 0.7 x 1.0).
                            .sized(0.4f, 0.9f)
                            // NEOFORGE_PORT: getStandingEyeHeight override removed in 26.x ->
                            // type-level eyeHeight (was 0.7f).
                            .eyeHeight(0.7f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcaneresearch_golem"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityArcaneBore>> ARCANE_BORE =
            ENTITY_TYPES.register("arcane_bore", () ->
                    EntityType.Builder.<EntityArcaneBore>of(EntityArcaneBore::new, MobCategory.MISC)
                            .sized(0.9f, 0.9f)
                            // NEOFORGE_PORT (26.3): Forge EntityArcaneBore.getStandingEyeHeight -> 0.8125f.
                            .eyeHeight(0.8125f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_bore"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySpecialItem>> SPECIAL_ITEM =
            ENTITY_TYPES.register("special_item", () ->
                    EntityType.Builder.<EntitySpecialItem>of(EntitySpecialItem::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(6)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "special_item"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFollowingItem>> FOLLOWING_ITEM =
            ENTITY_TYPES.register("following_item", () ->
                    EntityType.Builder.<EntityFollowingItem>of(EntityFollowingItem::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(6)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "following_item"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFluxRift>> FLUX_RIFT =
            ENTITY_TYPES.register("flux_rift", () ->
                    EntityType.Builder.<EntityFluxRift>of(EntityFluxRift::new, MobCategory.MISC)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "flux_rift"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFallingTaint>> FALLING_TAINT =
            ENTITY_TYPES.register("falling_taint", () ->
                    EntityType.Builder.<EntityFallingTaint>of(EntityFallingTaint::new, MobCategory.MISC)
                            .sized(0.98f, 0.98f)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "falling_taint"))));
}
