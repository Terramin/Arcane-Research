package arcaneresearch.common.entities;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityHellbat;
import arcaneresearch.common.entities.projectile.*;

// NEOFORGE_PORT: 1:1 entries; RegistryObject -> DeferredHolder, build(String) -> build(ResourceKey).
public class WandEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Arcaneresearch.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityEmber>> EMBER =
            ENTITY_TYPES.register("wand_ember", () ->
                    EntityType.Builder.<EntityEmber>of(EntityEmber::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_ember"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityExplosiveOrb>> EXPLOSIVE_ORB =
            ENTITY_TYPES.register("wand_explosive_orb", () ->
                    EntityType.Builder.<EntityExplosiveOrb>of(EntityExplosiveOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_explosive_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityFrostShard>> FROST_SHARD =
            ENTITY_TYPES.register("wand_frost_shard", () ->
                    EntityType.Builder.<EntityFrostShard>of(EntityFrostShard::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_frost_shard"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityShockOrb>> SHOCK_ORB =
            ENTITY_TYPES.register("wand_shock_orb", () ->
                    EntityType.Builder.<EntityShockOrb>of(EntityShockOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_shock_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityPrimalOrb>> PRIMAL_ORB =
            ENTITY_TYPES.register("wand_primal_orb", () ->
                    EntityType.Builder.<EntityPrimalOrb>of(EntityPrimalOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_primal_orb"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityPechBlast>> PECH_BLAST =
            ENTITY_TYPES.register("wand_pech_blast", () ->
                    EntityType.Builder.<EntityPechBlast>of(EntityPechBlast::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_pech_blast"))));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHellbat>> HELLBAT =
            ENTITY_TYPES.register("wand_hellbat", () ->
                    EntityType.Builder.<EntityHellbat>of(EntityHellbat::new, MobCategory.MONSTER)
                            .sized(0.5F, 0.9F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_hellbat"))));

    private WandEntities() {
    }
}
