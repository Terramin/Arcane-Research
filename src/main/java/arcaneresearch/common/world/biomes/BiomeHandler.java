package arcaneresearch.common.world.biomes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import java.util.*;
public class BiomeHandler {
    public static final ResourceKey<Biome> EERIE = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "eerie"));
    public static final ResourceKey<Biome> MAGICAL_FOREST = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "magical_forest"));
    public static final ResourceKey<Biome> ELDRITCH = ResourceKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "eldritch"));
    public static final TagKey<Biome> IS_ARCANERESEARCH = TagKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "is_arcaneresearch"));
    public static final TagKey<Biome> IS_MAGICAL = TagKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "is_magical"));
    public static final TagKey<Biome> IS_TAINTED = TagKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "is_tainted"));
    public static final TagKey<Biome> BIOME_BLACKLIST = TagKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "biome_blacklist"));
    public static final TagKey<Biome> DIMENSION_BLACKLIST = TagKey.create(
            Registries.BIOME, new ResourceLocation(Arcaneresearch.MODID, "dimension_blacklist"));
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeHandler.class);
    private static final Map<TagKey<Biome>, BiomeInfo> BIOME_TAG_INFO = new HashMap<>();
    private static final Map<ResourceKey<Biome>, BiomeInfo> BIOME_SPECIFIC_INFO = new HashMap<>();
    public static void registerBiomeInfo() {
        LOGGER.info("Registering Arcaneresearch biome info");
        registerTagInfo(BiomeTags.IS_OCEAN, 0.33f, Aspect.WATER, false, 0.0f);
        registerTagInfo(BiomeTags.IS_RIVER, 0.4f, Aspect.WATER, false, 0.0f);
        registerTagInfo(BiomeTags.IS_BEACH, 0.3f, Aspect.EARTH, false, 0.0f);
        registerTagInfo(BiomeTags.IS_NETHER, 0.125f, Aspect.FIRE, false, 0.0f);
        registerTagInfo(BiomeTags.IS_BADLANDS, 0.33f, Aspect.FIRE, false, 0.0f);
        registerTagInfo(BiomeTags.IS_TAIGA, 0.33f, Aspect.EARTH, true, 0.2f);
        registerTagInfo(BiomeTags.IS_FOREST, 0.5f, Aspect.EARTH, true, 1.0f);
        registerTagInfo(BiomeTags.IS_JUNGLE, 0.6f, Aspect.EARTH, false, 0.0f);
        registerTagInfo(BiomeTags.IS_MOUNTAIN, 0.3f, Aspect.AIR, false, 0.0f);
        registerTagInfo(BiomeTags.IS_HILL, 0.33f, Aspect.AIR, false, 0.0f);
        registerTagInfo(BiomeTags.IS_SAVANNA, 0.25f, Aspect.AIR, true, 0.2f);
        registerTagInfo(BiomeTags.IS_END, 0.125f, Aspect.AIR, false, 0.0f);
        registerBiomeInfo(MAGICAL_FOREST, 1.5f, Aspect.ORDER, true, 1.0f);
        registerBiomeInfo(EERIE, 0.75f, Aspect.ENTROPY, false, 0.0f);
        registerBiomeInfo(ELDRITCH, 0.25f, Aspect.ENTROPY, false, 0.0f);
        registerBiomeInfo(Biomes.PLAINS, 0.3f, Aspect.AIR, true, 0.2f);
        registerBiomeInfo(Biomes.SUNFLOWER_PLAINS, 0.35f, Aspect.AIR, true, 0.2f);
        registerBiomeInfo(Biomes.MEADOW, 0.4f, Aspect.AIR, true, 0.3f);
        registerBiomeInfo(Biomes.SWAMP, 0.5f, Aspect.ENTROPY, true, 0.2f);
        registerBiomeInfo(Biomes.MANGROVE_SWAMP, 0.5f, Aspect.ENTROPY, false, 0.0f);
        registerBiomeInfo(Biomes.MUSHROOM_FIELDS, 0.75f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.LUSH_CAVES, 0.5f, Aspect.WATER, false, 0.0f);
        registerTagInfo(IS_MAGICAL, 0.75f, Aspect.ORDER, true, 1.0f);
        registerBiomeInfo(Biomes.DESERT, 0.25f, Aspect.EARTH, false, 0.0f);
        registerBiomeInfo(Biomes.SNOWY_PLAINS, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.SNOWY_TAIGA, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.SNOWY_BEACH, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.SNOWY_SLOPES, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.GROVE, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.FROZEN_RIVER, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.FROZEN_OCEAN, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.DEEP_FROZEN_OCEAN, 0.25f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.FROZEN_PEAKS, 0.3f, Aspect.AIR, false, 0.0f);
        registerBiomeInfo(Biomes.DRIPSTONE_CAVES, 0.4f, Aspect.ORDER, false, 0.0f);
        registerBiomeInfo(Biomes.DEEP_DARK, 0.5f, Aspect.FIRE, false, 0.0f);
    }
    public static void addDimBlacklist(ResourceLocation dimension, int level) {
        LOGGER.debug("IMC dimensionBlacklist called for {} (level {}) - blacklisting now uses biome tags. Add to arcaneresearch:dimension_blacklist tag.", dimension, level);
    }
    public static void addBiomeBlacklist(ResourceLocation biome, int level) {
        LOGGER.debug("IMC biomeBlacklist called for {} (level {}) - blacklisting now uses biome tags. Add to arcaneresearch:biome_blacklist tag.", biome, level);
    }
    public static boolean isBiomeBlacklisted(Holder<Biome> biome) {
        return biome.is(BIOME_BLACKLIST);
    }
    public static boolean isDimensionBlacklisted(Holder<Biome> biome) {
        return biome.is(DIMENSION_BLACKLIST);
    }
    public static void registerTagInfo(TagKey<Biome> tag, float auraModifier, Aspect aspect,
                                       boolean supportsGreatwood, float greatwoodChance) {
        BIOME_TAG_INFO.put(tag, new BiomeInfo(auraModifier, aspect, supportsGreatwood, greatwoodChance));
    }
    public static void registerBiomeInfo(ResourceKey<Biome> biome, float auraModifier, Aspect aspect,
                                         boolean supportsGreatwood, float greatwoodChance) {
        BIOME_SPECIFIC_INFO.put(biome, new BiomeInfo(auraModifier, aspect, supportsGreatwood, greatwoodChance));
    }
    public static float getAuraModifier(Holder<Biome> biome) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isPresent()) {
            BiomeInfo specificInfo = BIOME_SPECIFIC_INFO.get(key.get());
            if (specificInfo != null) {
                return specificInfo.auraModifier();
            }
        }
        float total = 0.0f;
        int count = 0;
        for (Map.Entry<TagKey<Biome>, BiomeInfo> entry : BIOME_TAG_INFO.entrySet()) {
            if (biome.is(entry.getKey())) {
                total += entry.getValue().auraModifier();
                count++;
            }
        }
        return count > 0 ? total / count : 0.5f;
    }
    public static Aspect getBiomeAspect(Holder<Biome> biome) {
        return getRandomBiomeTag(biome, net.minecraft.util.RandomSource.create());
    }
    public static Aspect getRandomBiomeTag(Holder<Biome> biome, net.minecraft.util.RandomSource random) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isPresent()) {
            BiomeInfo specificInfo = BIOME_SPECIFIC_INFO.get(key.get());
            if (specificInfo != null) {
                return specificInfo.aspect();
            }
        }
        List<BiomeInfo> matches = new ArrayList<>();
        for (Map.Entry<TagKey<Biome>, BiomeInfo> entry : BIOME_TAG_INFO.entrySet()) {
            if (biome.is(entry.getKey())) {
                matches.add(entry.getValue());
            }
        }
        if (!matches.isEmpty()) {
            return matches.get(random.nextInt(matches.size())).aspect();
        }
        return null;
    }
    public static float getGreatwoodChance(Holder<Biome> biome) {
        Optional<ResourceKey<Biome>> key = biome.unwrapKey();
        if (key.isPresent()) {
            BiomeInfo specificInfo = BIOME_SPECIFIC_INFO.get(key.get());
            if (specificInfo != null && specificInfo.supportsGreatwood()) {
                return specificInfo.greatwoodChance();
            }
        }
        for (Map.Entry<TagKey<Biome>, BiomeInfo> entry : BIOME_TAG_INFO.entrySet()) {
            if (biome.is(entry.getKey()) && entry.getValue().supportsGreatwood()) {
                return entry.getValue().greatwoodChance();
            }
        }
        return 0.0f;
    }
    public static boolean isMagicalBiome(Holder<Biome> biome) {
        return biome.is(IS_MAGICAL) || biome.is(IS_ARCANERESEARCH);
    }
    public static boolean isTaintedBiome(Holder<Biome> biome) {
        return biome.is(IS_TAINTED);
    }
    public record BiomeInfo(float auraModifier, Aspect aspect, boolean supportsGreatwood, float greatwoodChance) {
    }
}
