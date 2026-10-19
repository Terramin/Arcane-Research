package arcaneresearch.init;

import arcaneresearch.common.world.maze.MazeFeature;
import arcaneresearch.common.world.structures.AuraNodeStructure;
import arcaneresearch.common.world.structures.EldritchRingStructure;
import arcaneresearch.common.world.structures.HilltopStonesStructure;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.world.features.*;
import arcaneresearch.common.world.structures.CultistPortalFeature;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModFeatures.java (same order, same registry names).
// REQUIRED changes only:
//   DeferredRegister.create(ForgeRegistries.FEATURES, ...) ->
//   create(Registries.FEATURE_TYPE, ...) - NOT Registries.FEATURE, which is the
//   DATAPACK registry (worldgen/feature configured instances); feature type codecs
//   dispatch through BuiltInRegistries.FEATURE_TYPE (verified in 26.3 jar sources).
//   RegistryObject -> DeferredHolder<Feature, ConcreteFeature>
//   Feature is NO LONGER generic in 26.3 (interface, no FeaturePlaceContext /
//   NoneFeatureConfiguration): constructors take only gameplay params (no codec arg).
// All worldgen params (tree/plant/ore types, forceSpiders flags) preserved verbatim.
public class ModFeatures {

    public static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(Registries.FEATURE_TYPE, Arcaneresearch.MODID);

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GreatwoodTreeFeature>> GREATWOOD_TREE =            FEATURES.register("greatwood_tree",                    () -> com.mojang.serialization.MapCodec.unit(() -> new GreatwoodTreeFeature()));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GreatwoodTreeFeature>> GREATWOOD_NEST =            FEATURES.register("greatwood_nest",                    () -> com.mojang.serialization.MapCodec.unit(() -> new GreatwoodTreeFeature(true)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<SilverwoodTreeFeature>> SILVERWOOD_TREE =            FEATURES.register("silverwood_tree",                    () -> com.mojang.serialization.MapCodec.unit(SilverwoodTreeFeature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BigMagicTreeFeature>> BIG_MAGIC_TREE =            FEATURES.register("big_magic_tree",                    () -> com.mojang.serialization.MapCodec.unit(() -> new BigMagicTreeFeature(BigMagicTreeFeature.TreeType.OAK)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<BigMagicTreeFeature>> BIG_SILVERWOOD_TREE =            FEATURES.register("big_silverwood_tree",                    () -> com.mojang.serialization.MapCodec.unit(() -> new BigMagicTreeFeature(BigMagicTreeFeature.TreeType.SILVERWOOD)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchPlantFeature>> CINDERPEARL_PATCH =            FEATURES.register("cinderpearl_patch",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchPlantFeature(ArcaneresearchPlantFeature.PlantType.CINDERPEARL)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchPlantFeature>> SHIMMERLEAF_PATCH =            FEATURES.register("shimmerleaf_patch",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchPlantFeature(ArcaneresearchPlantFeature.PlantType.SHIMMERLEAF)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchPlantFeature>> VISHROOM_PATCH =            FEATURES.register("vishroom_patch",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchPlantFeature(ArcaneresearchPlantFeature.PlantType.VISHROOM)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<GrassAmbientFeature>> GRASS_AMBIENT_PATCH =            FEATURES.register("grass_ambient_patch",                    () -> com.mojang.serialization.MapCodec.unit(GrassAmbientFeature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CrystalClusterFeature>> CRYSTAL =            FEATURES.register("crystal",                    () -> com.mojang.serialization.MapCodec.unit(CrystalClusterFeature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchOreFeature>> ORE_CINNABAR =            FEATURES.register("ore_cinnabar",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchOreFeature(ArcaneresearchOreFeature.OreType.CINNABAR)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchOreFeature>> ORE_QUARTZ =            FEATURES.register("ore_quartz",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchOreFeature(ArcaneresearchOreFeature.OreType.QUARTZ)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ArcaneresearchOreFeature>> ORE_AMBER =            FEATURES.register("ore_amber",                    () -> com.mojang.serialization.MapCodec.unit(() -> new ArcaneresearchOreFeature(ArcaneresearchOreFeature.OreType.AMBER)));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<CultistPortalFeature>> CULTIST_PORTAL =            FEATURES.register("cultist_portal",                    () -> com.mojang.serialization.MapCodec.unit(CultistPortalFeature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<AuraNodeStructure>> AURA_NODE =            FEATURES.register("aura_node",                    () -> com.mojang.serialization.MapCodec.unit(AuraNodeStructure::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<EldritchRingStructure.Feature>> ELDRITCH_RING =            FEATURES.register("eldritch_ring",                    () -> com.mojang.serialization.MapCodec.unit(EldritchRingStructure.Feature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<HilltopStonesStructure.Feature>> HILLTOP_STONES =            FEATURES.register("hilltop_stones",                    () -> com.mojang.serialization.MapCodec.unit(HilltopStonesStructure.Feature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<MazeFeature>> MAZE_ROOM =            FEATURES.register("maze_room",                    () -> com.mojang.serialization.MapCodec.unit(MazeFeature::new));

    public static final DeferredHolder<MapCodec<? extends Feature>, MapCodec<ObsidianTotemFeature>> OBSIDIAN_TOTEM =            FEATURES.register("obsidian_totem",                    () -> com.mojang.serialization.MapCodec.unit(ObsidianTotemFeature::new));

    public static void register(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}