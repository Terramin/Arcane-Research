package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

import java.util.HashMap;
import java.util.Map;

/**
 * Enchantment-to-aspect mapping transcribed from thaumcraft 1.12.2
 * {@code ThaumcraftCraftingManager.getBonusTags} (lines 293-328). Each level
 * contributes 3 vis; half-profile enchantments split it between two aspects.
 */
public final class EnchantmentAspectRules {

    /**
     * Aspects contributed by one enchantment stack.
     *
     * @param aspects a mutable list of profile aspects
     * @param magic vis added to the MAGIC aspect
     */
    public record Contribution(AspectList aspects, int magic) {
    }

    /** Profiles keyed by short enchantment name (no namespace). */
    private static final Map<String, Aspect[]> PROFILE = new HashMap<>();
    private static final Map<String, Aspect[]> HALF_PROFILE = new HashMap<>();
    private static final Map<String, Integer> VANILLA_RARITY = new HashMap<>();

    static {
        HALF_PROFILE.put("bane_of_arthropods", new Aspect[]{Aspect.BEAST, Aspect.AVERSION});
        HALF_PROFILE.put("blast_protection", new Aspect[]{Aspect.PROTECT, Aspect.ENTROPY});
        HALF_PROFILE.put("fire_aspect", new Aspect[]{Aspect.FIRE, Aspect.AVERSION});
        HALF_PROFILE.put("fire_protection", new Aspect[]{Aspect.PROTECT, Aspect.FIRE});
        HALF_PROFILE.put("smite", new Aspect[]{Aspect.UNDEAD, Aspect.AVERSION});

        PROFILE.put("aqua_affinity", new Aspect[]{Aspect.WATER});
        PROFILE.put("efficiency", new Aspect[]{Aspect.TOOL});
        PROFILE.put("feather_falling", new Aspect[]{Aspect.FLIGHT});
        PROFILE.put("flame", new Aspect[]{Aspect.FIRE});
        PROFILE.put("fortune", new Aspect[]{Aspect.DESIRE});
        PROFILE.put("infinity", new Aspect[]{Aspect.CRAFT});
        PROFILE.put("knockback", new Aspect[]{Aspect.AIR});
        PROFILE.put("looting", new Aspect[]{Aspect.DESIRE});
        PROFILE.put("power", new Aspect[]{Aspect.AVERSION});
        PROFILE.put("projectile_protection", new Aspect[]{Aspect.PROTECT});
        PROFILE.put("protection", new Aspect[]{Aspect.PROTECT});
        PROFILE.put("punch", new Aspect[]{Aspect.AIR});
        PROFILE.put("respiration", new Aspect[]{Aspect.AIR});
        PROFILE.put("sharpness", new Aspect[]{Aspect.AVERSION});
        PROFILE.put("silk_touch", new Aspect[]{Aspect.EXCHANGE});
        PROFILE.put("thorns", new Aspect[]{Aspect.AVERSION});
        PROFILE.put("unbreaking", new Aspect[]{Aspect.EARTH});
        PROFILE.put("depth_strider", new Aspect[]{Aspect.WATER});
        PROFILE.put("luck_of_the_sea", new Aspect[]{Aspect.DESIRE});
        PROFILE.put("lure", new Aspect[]{Aspect.BEAST});
        PROFILE.put("frost_walker", new Aspect[]{Aspect.COLD});
        PROFILE.put("mending", new Aspect[]{Aspect.CRAFT});

        VANILLA_RARITY.put("aqua_affinity", 4);
        VANILLA_RARITY.put("bane_of_arthropods", 2);
        VANILLA_RARITY.put("blast_protection", 4);
        VANILLA_RARITY.put("binding_curse", 6);
        VANILLA_RARITY.put("feather_falling", 2);
        VANILLA_RARITY.put("fire_aspect", 4);
        VANILLA_RARITY.put("fire_protection", 2);
        VANILLA_RARITY.put("flame", 4);
        VANILLA_RARITY.put("fortune", 4);
        VANILLA_RARITY.put("infinity", 6);
        VANILLA_RARITY.put("knockback", 2);
        VANILLA_RARITY.put("looting", 4);
        VANILLA_RARITY.put("lure", 4);
        VANILLA_RARITY.put("mending", 4);
        VANILLA_RARITY.put("projectile_protection", 2);
        VANILLA_RARITY.put("punch", 4);
        VANILLA_RARITY.put("respiration", 4);
        VANILLA_RARITY.put("silk_touch", 6);
        VANILLA_RARITY.put("smite", 2);
        VANILLA_RARITY.put("sweeping", 4);
        VANILLA_RARITY.put("thorns", 6);
        VANILLA_RARITY.put("unbreaking", 2);
        VANILLA_RARITY.put("vanishing_curse", 6);
        VANILLA_RARITY.put("depth_strider", 4);
        VANILLA_RARITY.put("luck_of_the_sea", 4);
        VANILLA_RARITY.put("frost_walker", 4);
    }

    private EnchantmentAspectRules() {
    }

    /**
     * @param enchantmentId full or short enchantment id
     * @return the vanilla 1.12.2 rarity bonus (0/2/4/6)
     */
    public static int vanillaRarityBonus(String enchantmentId) {
        String key = enchantmentId == null ? "" : enchantmentId.substring(enchantmentId.indexOf(':') + 1);
        return VANILLA_RARITY.getOrDefault(key, 0);
    }

    /**
     * @param enchantmentId full or short enchantment id
     * @return true when the id belongs to the vanilla namespace (or has none)
     */
    public static boolean isVanilla(String enchantmentId) {
        return enchantmentId == null || !enchantmentId.contains(":")
                || enchantmentId.startsWith("minecraft:");
    }

    /**
     * @param enchantmentId full or short enchantment id
     * @param level enchantment level; each level contributes 3 vis; values <= 0
     *              produce no profile aspects and no MAGIC
     * @param rarityBonus bonus from the enchantment's rarity (2/4/6), supplied by
     *                    the caller from the enchantment registry
     * @return profile aspects (empty for modded ids) plus the MAGIC contribution
     */
    public static Contribution contribution(String enchantmentId, int level, int rarityBonus) {
        String key = enchantmentId == null ? "" : enchantmentId.substring(enchantmentId.indexOf(':') + 1);
        int vis = Math.max(0, level) * 3;
        AspectList aspects = new AspectList();

        if (isVanilla(enchantmentId)) {
            if (HALF_PROFILE.containsKey(key)) {
                for (Aspect aspect : HALF_PROFILE.get(key)) {
                    aspects.merge(aspect, vis / 2);
                }
            } else if (PROFILE.containsKey(key)) {
                for (Aspect aspect : PROFILE.get(key)) {
                    aspects.merge(aspect, vis);
                }
            }
        }

        int magic = level > 0 ? Math.max(0, vis + rarityBonus) : 0;
        return new Contribution(aspects, magic);
    }

    /** Uses the vanilla rarity bonus for known enchantments. */
    public static Contribution contribution(String enchantmentId, int level) {
        return contribution(enchantmentId, level, vanillaRarityBonus(enchantmentId));
    }
}
