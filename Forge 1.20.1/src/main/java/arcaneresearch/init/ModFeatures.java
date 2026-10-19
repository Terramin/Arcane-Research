package arcaneresearch.init;
import arcaneresearch.common.world.maze.MazeFeature;
import arcaneresearch.common.world.structures.AuraNodeStructure;
import arcaneresearch.common.world.structures.EldritchRingStructure;
import arcaneresearch.common.world.structures.HilltopStonesStructure;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.world.features.*;
import arcaneresearch.common.world.structures.CultistPortalFeature;
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Arcaneresearch.MODID);
    public static final RegistryObject<GreatwoodTreeFeature> GREATWOOD_TREE =
            FEATURES.register("greatwood_tree",
                    () -> new GreatwoodTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<GreatwoodTreeFeature> GREATWOOD_NEST =
            FEATURES.register("greatwood_nest",
                    () -> new GreatwoodTreeFeature(NoneFeatureConfiguration.CODEC, true));
    public static final RegistryObject<SilverwoodTreeFeature> SILVERWOOD_TREE =
            FEATURES.register("silverwood_tree",
                    () -> new SilverwoodTreeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<BigMagicTreeFeature> BIG_MAGIC_TREE =
            FEATURES.register("big_magic_tree",
                    () -> new BigMagicTreeFeature(NoneFeatureConfiguration.CODEC,
                            BigMagicTreeFeature.TreeType.OAK));
    public static final RegistryObject<BigMagicTreeFeature> BIG_SILVERWOOD_TREE =
            FEATURES.register("big_silverwood_tree",
                    () -> new BigMagicTreeFeature(NoneFeatureConfiguration.CODEC,
                            BigMagicTreeFeature.TreeType.SILVERWOOD));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CINDERPEARL_PATCH =
            FEATURES.register("cinderpearl_patch",
                    () -> new ArcaneresearchPlantFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchPlantFeature.PlantType.CINDERPEARL));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SHIMMERLEAF_PATCH =
            FEATURES.register("shimmerleaf_patch",
                    () -> new ArcaneresearchPlantFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchPlantFeature.PlantType.SHIMMERLEAF));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> VISHROOM_PATCH =
            FEATURES.register("vishroom_patch",
                    () -> new ArcaneresearchPlantFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchPlantFeature.PlantType.VISHROOM));
    public static final RegistryObject<GrassAmbientFeature> GRASS_AMBIENT_PATCH =
            FEATURES.register("grass_ambient_patch",
                    () -> new GrassAmbientFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<CrystalClusterFeature> CRYSTAL =
            FEATURES.register("crystal",
                    () -> new CrystalClusterFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<ArcaneresearchOreFeature> ORE_CINNABAR =
            FEATURES.register("ore_cinnabar",
                    () -> new ArcaneresearchOreFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchOreFeature.OreType.CINNABAR));
    public static final RegistryObject<ArcaneresearchOreFeature> ORE_QUARTZ =
            FEATURES.register("ore_quartz",
                    () -> new ArcaneresearchOreFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchOreFeature.OreType.QUARTZ));
    public static final RegistryObject<ArcaneresearchOreFeature> ORE_AMBER =
            FEATURES.register("ore_amber",
                    () -> new ArcaneresearchOreFeature(NoneFeatureConfiguration.CODEC,
                            ArcaneresearchOreFeature.OreType.AMBER));
    public static final RegistryObject<CultistPortalFeature> CULTIST_PORTAL =
            FEATURES.register("cultist_portal",
                    () -> new CultistPortalFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<arcaneresearch.common.world.structures.AuraNodeStructure> AURA_NODE =
            FEATURES.register("aura_node",
                    () -> new arcaneresearch.common.world.structures.AuraNodeStructure(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<arcaneresearch.common.world.structures.EldritchRingStructure.Feature> ELDRITCH_RING =
            FEATURES.register("eldritch_ring",
                    () -> new arcaneresearch.common.world.structures.EldritchRingStructure.Feature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<arcaneresearch.common.world.structures.HilltopStonesStructure.Feature> HILLTOP_STONES =
            FEATURES.register("hilltop_stones",
                    () -> new arcaneresearch.common.world.structures.HilltopStonesStructure.Feature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<arcaneresearch.common.world.maze.MazeFeature> MAZE_ROOM =
            FEATURES.register("maze_room",
                    () -> new arcaneresearch.common.world.maze.MazeFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<arcaneresearch.common.world.features.ObsidianTotemFeature> OBSIDIAN_TOTEM =
            FEATURES.register("obsidian_totem",
                    () -> new arcaneresearch.common.world.features.ObsidianTotemFeature(NoneFeatureConfiguration.CODEC));
}
