package arcaneresearch.init;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<EntityType<EntityFocusProjectile>> FOCUS_PROJECTILE =
            ENTITY_TYPES.register("focus_projectile", () ->
                    EntityType.Builder.<EntityFocusProjectile>of(EntityFocusProjectile::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "focus_projectile").toString()));
    public static final RegistryObject<EntityType<EntityFocusCloud>> FOCUS_CLOUD =
            ENTITY_TYPES.register("focus_cloud", () ->
                    EntityType.Builder.<EntityFocusCloud>of(EntityFocusCloud::new, MobCategory.MISC)
                            .sized(1.0f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "focus_cloud").toString()));
    public static final RegistryObject<EntityType<EntityFocusMine>> FOCUS_MINE =
            ENTITY_TYPES.register("focus_mine", () ->
                    EntityType.Builder.<EntityFocusMine>of(EntityFocusMine::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "focus_mine").toString()));
    public static final RegistryObject<EntityType<EntityAlumentum>> ALUMENTUM =
            ENTITY_TYPES.register("alumentum", () ->
                    EntityType.Builder.<EntityAlumentum>of(EntityAlumentum::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "alumentum").toString()));
    public static final RegistryObject<EntityType<EntityGrapple>> GRAPPLE =
            ENTITY_TYPES.register("grapple", () ->
                    EntityType.Builder.<EntityGrapple>of(EntityGrapple::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "grapple").toString()));
    public static final RegistryObject<EntityType<EntityBottleTaint>> BOTTLE_TAINT =
            ENTITY_TYPES.register("bottle_taint", () ->
                    EntityType.Builder.<EntityBottleTaint>of(EntityBottleTaint::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "bottle_taint").toString()));
    public static final RegistryObject<EntityType<EntityHomingShard>> HOMING_SHARD =
            ENTITY_TYPES.register("homing_shard", () ->
                    EntityType.Builder.<EntityHomingShard>of(EntityHomingShard::new, MobCategory.MISC)
                            .sized(0.15f, 0.15f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "homing_shard").toString()));
    public static final RegistryObject<EntityType<EntityGolemDart>> GOLEM_DART =
            ENTITY_TYPES.register("golem_dart", () ->
                    EntityType.Builder.<EntityGolemDart>of(EntityGolemDart::new, MobCategory.MISC)
                            .sized(0.2f, 0.2f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "golem_dart").toString()));
    public static final RegistryObject<EntityType<EntityPrimalArrow>> PRIMAL_ARROW =
            ENTITY_TYPES.register("primal_arrow", () ->
                    EntityType.Builder.<EntityPrimalArrow>of(EntityPrimalArrow::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "primal_arrow").toString()));
    public static final RegistryObject<EntityType<EntityGolemOrb>> GOLEM_ORB =
            ENTITY_TYPES.register("golem_orb", () ->
                    EntityType.Builder.<EntityGolemOrb>of(EntityGolemOrb::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "golem_orb").toString()));
    public static final RegistryObject<EntityType<EntityEldritchOrb>> ELDRITCH_ORB =
            ENTITY_TYPES.register("eldritch_orb", () ->
                    EntityType.Builder.<EntityEldritchOrb>of(EntityEldritchOrb::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "eldritch_orb").toString()));
    public static final RegistryObject<EntityType<EntityCausalityCollapser>> CAUSALITY_COLLAPSER =
            ENTITY_TYPES.register("causality_collapser", () ->
                    EntityType.Builder.<EntityCausalityCollapser>of(EntityCausalityCollapser::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "causality_collapser").toString()));
    public static final RegistryObject<EntityType<EntityRiftBlast>> RIFT_BLAST =
            ENTITY_TYPES.register("rift_blast", () ->
                    EntityType.Builder.<EntityRiftBlast>of(EntityRiftBlast::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "rift_blast").toString()));
    public static final RegistryObject<EntityType<EntityWisp>> WISP =
            ENTITY_TYPES.register("wisp", () ->
                    EntityType.Builder.<EntityWisp>of(EntityWisp::new, MobCategory.MONSTER)
                            .sized(0.9f, 0.9f)
                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wisp").toString()));
    public static final RegistryObject<EntityType<EntityFireBat>> FIRE_BAT =
            ENTITY_TYPES.register("fire_bat", () ->
                    EntityType.Builder.<EntityFireBat>of(EntityFireBat::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.9f)
                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "fire_bat").toString()));
    public static final RegistryObject<EntityType<EntityBrainyZombie>> BRAINY_ZOMBIE =
            ENTITY_TYPES.register("brainy_zombie", () ->
                    EntityType.Builder.<EntityBrainyZombie>of(EntityBrainyZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "brainy_zombie").toString()));
    public static final RegistryObject<EntityType<EntityMindSpider>> MIND_SPIDER =
            ENTITY_TYPES.register("mind_spider", () ->
                    EntityType.Builder.<EntityMindSpider>of(EntityMindSpider::new, MobCategory.MONSTER)
                            .sized(0.7f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "mind_spider").toString()));
    public static final RegistryObject<EntityType<EntityThaumicSlime>> THAUMIC_SLIME =
            ENTITY_TYPES.register("thaumic_slime", () ->
                    EntityType.Builder.<EntityThaumicSlime>of(EntityThaumicSlime::new, MobCategory.MONSTER)
                            .sized(2.04f, 2.04f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "thaumic_slime").toString()));
    public static final RegistryObject<EntityType<EntityGiantBrainyZombie>> GIANT_BRAINY_ZOMBIE =
            ENTITY_TYPES.register("giant_brainy_zombie", () ->
                    EntityType.Builder.<EntityGiantBrainyZombie>of(EntityGiantBrainyZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "giant_brainy_zombie").toString()));
    public static final RegistryObject<EntityType<EntityInhabitedZombie>> INHABITED_ZOMBIE =
            ENTITY_TYPES.register("inhabited_zombie", () ->
                    EntityType.Builder.<EntityInhabitedZombie>of(EntityInhabitedZombie::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.95f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "inhabited_zombie").toString()));
    public static final RegistryObject<EntityType<EntityEldritchCrab>> ELDRITCH_CRAB =
            ENTITY_TYPES.register("eldritch_crab", () ->
                    EntityType.Builder.<EntityEldritchCrab>of(EntityEldritchCrab::new, MobCategory.MONSTER)
                            .sized(0.8f, 0.6f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "eldritch_crab").toString()));
    public static final RegistryObject<EntityType<EntitySpellBat>> SPELL_BAT =
            ENTITY_TYPES.register("spell_bat", () ->
                    EntityType.Builder.<EntitySpellBat>of(EntitySpellBat::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.9f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "spell_bat").toString()));
    public static final RegistryObject<EntityType<EntityEldritchGuardian>> ELDRITCH_GUARDIAN =
            ENTITY_TYPES.register("eldritch_guardian", () ->
                    EntityType.Builder.<EntityEldritchGuardian>of(EntityEldritchGuardian::new, MobCategory.MONSTER)
                            .sized(0.8f, 2.25f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "eldritch_guardian").toString()));
    public static final RegistryObject<EntityType<EntityCultist>> CULTIST =
            ENTITY_TYPES.register("cultist", () ->
                    EntityType.Builder.<EntityCultist>of(EntityCultist::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist").toString()));
    public static final RegistryObject<EntityType<EntityCultistKnight>> CULTIST_KNIGHT =
            ENTITY_TYPES.register("cultist_knight", () ->
                    EntityType.Builder.<EntityCultistKnight>of(EntityCultistKnight::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist_knight").toString()));
    public static final RegistryObject<EntityType<EntityCultistCleric>> CULTIST_CLERIC =
            ENTITY_TYPES.register("cultist_cleric", () ->
                    EntityType.Builder.<EntityCultistCleric>of(EntityCultistCleric::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist_cleric").toString()));
    public static final RegistryObject<EntityType<EntityCultistPortalLesser>> CULTIST_PORTAL_LESSER =
            ENTITY_TYPES.register("cultist_portal_lesser", () ->
                    EntityType.Builder.<EntityCultistPortalLesser>of(EntityCultistPortalLesser::new, MobCategory.MONSTER)
                            .sized(1.5f, 3.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist_portal_lesser").toString()));
    public static final RegistryObject<EntityType<EntityPech>> PECH =
            ENTITY_TYPES.register("pech", () ->
                    EntityType.Builder.<EntityPech>of(EntityPech::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.2f)
                            .clientTrackingRange(4)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "pech").toString()));
    public static final RegistryObject<EntityType<EntityTaintCrawler>> TAINT_CRAWLER =
            ENTITY_TYPES.register("taint_crawler", () ->
                    EntityType.Builder.<EntityTaintCrawler>of(EntityTaintCrawler::new, MobCategory.MONSTER)
                            .sized(0.5f, 0.4f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taint_crawler").toString()));
    public static final RegistryObject<EntityType<EntityTaintSwarm>> TAINT_SWARM =
            ENTITY_TYPES.register("taint_swarm", () ->
                    EntityType.Builder.<EntityTaintSwarm>of(EntityTaintSwarm::new, MobCategory.MONSTER)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taint_swarm").toString()));
    public static final RegistryObject<EntityType<EntityTaintacle>> TAINTACLE =
            ENTITY_TYPES.register("taintacle", () ->
                    EntityType.Builder.<EntityTaintacle>of(EntityTaintacle::new, MobCategory.MONSTER)
                            .sized(0.8f, 3.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taintacle").toString()));
    public static final RegistryObject<EntityType<EntityTaintacleSmall>> TAINTACLE_SMALL =
            ENTITY_TYPES.register("taintacle_small", () ->
                    EntityType.Builder.<EntityTaintacleSmall>of(EntityTaintacleSmall::new, MobCategory.MONSTER)
                            .sized(0.22f, 1.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taintacle_small").toString()));
    public static final RegistryObject<EntityType<EntityTaintSeed>> TAINT_SEED =
            ENTITY_TYPES.register("taint_seed", () ->
                    EntityType.Builder.<EntityTaintSeed>of(EntityTaintSeed::new, MobCategory.MONSTER)
                            .sized(1.5f, 1.25f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taint_seed").toString()));
    public static final RegistryObject<EntityType<EntityTaintSeedPrime>> TAINT_SEED_PRIME =
            ENTITY_TYPES.register("taint_seed_prime", () ->
                    EntityType.Builder.<EntityTaintSeedPrime>of(EntityTaintSeedPrime::new, MobCategory.MONSTER)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taint_seed_prime").toString()));
    public static final RegistryObject<EntityType<EntityCultistLeader>> CULTIST_LEADER =
            ENTITY_TYPES.register("cultist_leader", () ->
                    EntityType.Builder.<EntityCultistLeader>of(EntityCultistLeader::new, MobCategory.MONSTER)
                            .sized(0.75f, 2.25f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist_leader").toString()));
    public static final RegistryObject<EntityType<EntityTaintacleGiant>> TAINTACLE_GIANT =
            ENTITY_TYPES.register("taintacle_giant", () ->
                    EntityType.Builder.<EntityTaintacleGiant>of(EntityTaintacleGiant::new, MobCategory.MONSTER)
                            .sized(1.1f, 6.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "taintacle_giant").toString()));
    public static final RegistryObject<EntityType<EntityCultistPortalGreater>> CULTIST_PORTAL_GREATER =
            ENTITY_TYPES.register("cultist_portal_greater", () ->
                    EntityType.Builder.<EntityCultistPortalGreater>of(EntityCultistPortalGreater::new, MobCategory.MONSTER)
                            .sized(1.5f, 3.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "cultist_portal_greater").toString()));
    public static final RegistryObject<EntityType<EntityEldritchGolem>> ELDRITCH_GOLEM =
            ENTITY_TYPES.register("eldritch_golem", () ->
                    EntityType.Builder.<EntityEldritchGolem>of(EntityEldritchGolem::new, MobCategory.MONSTER)
                            .sized(1.75f, 3.5f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "eldritch_golem").toString()));
    public static final RegistryObject<EntityType<EntityEldritchWarden>> ELDRITCH_WARDEN =
            ENTITY_TYPES.register("eldritch_warden", () ->
                    EntityType.Builder.<EntityEldritchWarden>of(EntityEldritchWarden::new, MobCategory.MONSTER)
                            .sized(1.5f, 3.5f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "eldritch_warden").toString()));
    public static final RegistryObject<EntityType<EntityTurretCrossbow>> TURRET_CROSSBOW =
            ENTITY_TYPES.register("turret_crossbow", () ->
                    EntityType.Builder.<EntityTurretCrossbow>of(EntityTurretCrossbow::new, MobCategory.MISC)
                            .sized(0.95f, 1.25f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "turret_crossbow").toString()));
    public static final RegistryObject<EntityType<EntityTurretCrossbowAdvanced>> TURRET_CROSSBOW_ADVANCED =
            ENTITY_TYPES.register("turret_crossbow_advanced", () ->
                    EntityType.Builder.<EntityTurretCrossbowAdvanced>of(EntityTurretCrossbowAdvanced::new, MobCategory.MISC)
                            .sized(0.95f, 1.5f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "turret_crossbow_advanced").toString()));
    public static final RegistryObject<EntityType<EntityArcaneresearchGolem>> ARCANERESEARCH_GOLEM =
            ENTITY_TYPES.register("arcaneresearch_golem", () ->
                    EntityType.Builder.<EntityArcaneresearchGolem>of(EntityArcaneresearchGolem::new, MobCategory.MISC)
                            .sized(0.4F, 0.9F)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "arcaneresearch_golem").toString()));
    public static final RegistryObject<EntityType<EntityArcaneBore>> ARCANE_BORE =
            ENTITY_TYPES.register("arcane_bore", () ->
                    EntityType.Builder.<EntityArcaneBore>of(EntityArcaneBore::new, MobCategory.MISC)
                            .sized(0.9f, 0.9f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "arcane_bore").toString()));
    public static final RegistryObject<EntityType<EntitySpecialItem>> SPECIAL_ITEM =
            ENTITY_TYPES.register("special_item", () ->
                    EntityType.Builder.<EntitySpecialItem>of(EntitySpecialItem::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(6)
                            .updateInterval(20)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "special_item").toString()));
    public static final RegistryObject<EntityType<EntityFollowingItem>> FOLLOWING_ITEM =
            ENTITY_TYPES.register("following_item", () ->
                    EntityType.Builder.<EntityFollowingItem>of(EntityFollowingItem::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(6)
                            .updateInterval(20)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "following_item").toString()));
    public static final RegistryObject<EntityType<EntityFluxRift>> FLUX_RIFT =
            ENTITY_TYPES.register("flux_rift", () ->
                    EntityType.Builder.<EntityFluxRift>of(EntityFluxRift::new, MobCategory.MISC)
                            .sized(2.0f, 2.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .fireImmune()
                            .build(new ResourceLocation(Arcaneresearch.MODID, "flux_rift").toString()));
    public static final RegistryObject<EntityType<EntityFallingTaint>> FALLING_TAINT =
            ENTITY_TYPES.register("falling_taint", () ->
                    EntityType.Builder.<EntityFallingTaint>of(EntityFallingTaint::new, MobCategory.MISC)
                            .sized(0.98f, 0.98f)
                            .clientTrackingRange(10)
                            .updateInterval(20)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "falling_taint").toString()));
}
