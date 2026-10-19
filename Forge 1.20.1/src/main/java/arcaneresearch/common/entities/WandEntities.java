package arcaneresearch.common.entities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityHellbat;
import arcaneresearch.common.entities.projectile.*;
public class WandEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<EntityType<EntityEmber>> EMBER =
            ENTITY_TYPES.register("wand_ember", () ->
                    EntityType.Builder.<EntityEmber>of(EntityEmber::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_ember").toString()));
    public static final RegistryObject<EntityType<EntityExplosiveOrb>> EXPLOSIVE_ORB =
            ENTITY_TYPES.register("wand_explosive_orb", () ->
                    EntityType.Builder.<EntityExplosiveOrb>of(EntityExplosiveOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_explosive_orb").toString()));
    public static final RegistryObject<EntityType<EntityFrostShard>> FROST_SHARD =
            ENTITY_TYPES.register("wand_frost_shard", () ->
                    EntityType.Builder.<EntityFrostShard>of(EntityFrostShard::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_frost_shard").toString()));
    public static final RegistryObject<EntityType<EntityShockOrb>> SHOCK_ORB =
            ENTITY_TYPES.register("wand_shock_orb", () ->
                    EntityType.Builder.<EntityShockOrb>of(EntityShockOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_shock_orb").toString()));
    public static final RegistryObject<EntityType<EntityPrimalOrb>> PRIMAL_ORB =
            ENTITY_TYPES.register("wand_primal_orb", () ->
                    EntityType.Builder.<EntityPrimalOrb>of(EntityPrimalOrb::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_primal_orb").toString()));
    public static final RegistryObject<EntityType<EntityPechBlast>> PECH_BLAST =
            ENTITY_TYPES.register("wand_pech_blast", () ->
                    EntityType.Builder.<EntityPechBlast>of(EntityPechBlast::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_pech_blast").toString()));
    public static final RegistryObject<EntityType<EntityHellbat>> HELLBAT =
            ENTITY_TYPES.register("wand_hellbat", () ->
                    EntityType.Builder.<EntityHellbat>of(EntityHellbat::new, MobCategory.MONSTER)
                            .sized(0.5F, 0.9F)
                            .clientTrackingRange(8)
                            .updateInterval(5)
                            .build(new ResourceLocation(Arcaneresearch.MODID, "wand_hellbat").toString()));
    private WandEntities() {
    }
}
