package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Зачарования и аспекты предметов")
class EnchantmentAspectRulesTest {

    @Test
    @DisplayName("profile enchantments add their aspect plus the original rarity bonus")
    void profileEnchantments() {
        EnchantmentAspectRules.Contribution protection =
                EnchantmentAspectRules.contribution("minecraft:protection", 1);
        assertEquals(3, protection.aspects().getAmount(Aspect.PROTECT));
        assertEquals(3, protection.magic(), "Protection is common in 1.12.2");

        EnchantmentAspectRules.Contribution mending =
                EnchantmentAspectRules.contribution("minecraft:mending", 1);
        assertEquals(3, mending.aspects().getAmount(Aspect.CRAFT));
        assertEquals(7, mending.magic(), "3 vis + 4 rare bonus");
    }

    @Test
    @DisplayName("half-profile enchantments split their vis after multiplying by three")
    void halfProfile() {
        EnchantmentAspectRules.Contribution sharpness =
                EnchantmentAspectRules.contribution("minecraft:sharpness", 2);
        assertEquals(6, sharpness.aspects().getAmount(Aspect.AVERSION));
        assertEquals(0, sharpness.aspects().getAmount(Aspect.FIRE));
        assertEquals(6, sharpness.magic());
    }

    @Test
    @DisplayName("unknown enchantments still contribute MAGIC, modded ids get no vanilla profile")
    void unknownEnchantments() {
        assertEquals(6, EnchantmentAspectRules.contribution("modded:custom", 2).magic());
        assertEquals(0, EnchantmentAspectRules.contribution("modded:protection", 2)
                .aspects().size());
        assertEquals(0, EnchantmentAspectRules.contribution(null, 2).aspects().size());
        assertEquals(0, EnchantmentAspectRules.contribution("minecraft:sharpness", -1).magic());
        assertEquals(0, EnchantmentAspectRules.contribution("minecraft:mending", 0).magic());
        assertEquals(0, EnchantmentAspectRules.contribution("minecraft:binding_curse", -1).magic());
    }

    @Test
    @DisplayName("levels scale linearly")
    void levels() {
        assertEquals(12, EnchantmentAspectRules.contribution("minecraft:protection", 4).magic());
        assertEquals(12, EnchantmentAspectRules.contribution("minecraft:protection", 4)
                .aspects().getAmount(Aspect.PROTECT));
    }

    private static Stream<Arguments> enchantmentTable() {
        // Values transcribed from Thaumcraft 1.12.2 ThaumcraftCraftingManager:293-322.
        return Stream.of(
                Arguments.of("aqua_affinity", 1, "aqua", 3, 7),
                Arguments.of("bane_of_arthropods", 1, "bestia", 1, 5),
                Arguments.of("blast_protection", 1, "praemunio", 1, 7),
                Arguments.of("efficiency", 3, "instrumentum", 9, 9),
                Arguments.of("feather_falling", 1, "volatus", 3, 5),
                Arguments.of("fire_aspect", 1, "ignis", 1, 7),
                Arguments.of("fire_protection", 2, "praemunio", 3, 8),
                Arguments.of("flame", 1, "ignis", 3, 7),
                Arguments.of("fortune", 3, "desiderium", 9, 13),
                Arguments.of("infinity", 1, "fabrico", 3, 9),
                Arguments.of("knockback", 1, "aer", 3, 5),
                Arguments.of("looting", 2, "desiderium", 6, 10),
                Arguments.of("power", 3, "aversio", 9, 9),
                Arguments.of("projectile_protection", 1, "praemunio", 3, 5),
                Arguments.of("protection", 1, "praemunio", 3, 3),
                Arguments.of("punch", 1, "aer", 3, 7),
                Arguments.of("respiration", 2, "aer", 6, 10),
                Arguments.of("sharpness", 2, "aversio", 6, 6),
                Arguments.of("silk_touch", 1, "permutatio", 3, 9),
                Arguments.of("thorns", 1, "aversio", 3, 9),
                Arguments.of("smite", 1, "exanimis", 1, 5),
                Arguments.of("unbreaking", 2, "terra", 6, 8),
                Arguments.of("depth_strider", 1, "aqua", 3, 7),
                Arguments.of("luck_of_the_sea", 1, "desiderium", 3, 7),
                Arguments.of("lure", 1, "bestia", 3, 7),
                Arguments.of("frost_walker", 2, "gelum", 6, 10),
                Arguments.of("mending", 1, "fabrico", 3, 7),
                Arguments.of("binding_curse", 1, null, 0, 9),
                Arguments.of("sweeping", 1, null, 0, 7),
                Arguments.of("vanishing_curse", 1, null, 0, 9)
        );
    }

    @ParameterizedTest(name = "{0} lvl {1}")
    @MethodSource("enchantmentTable")
    @DisplayName("vanilla enchantments match the 1.12.2 reference table")
    void enchantmentMapping(String id, int level, String aspectTag, int aspectAmount, int magic) {
        EnchantmentAspectRules.Contribution contribution =
                EnchantmentAspectRules.contribution("minecraft:" + id, level);

        assertEquals(magic, contribution.magic(), id);
        if (aspectTag == null) {
            assertEquals(0, contribution.aspects().size(), id);
        } else {
            Aspect aspect = Aspect.getAspect(aspectTag);
            assertNotNull(aspect, aspectTag);
            assertEquals(aspectAmount, contribution.aspects().getAmount(aspect), id);
        }
    }

    @Test
    @DisplayName("dissolving an enchanted item keeps the expected aspects within all caps")
    void crucibleDissolveSimulation() {
        AspectList dissolved = new AspectList();
        dissolved.add(Aspect.AIR, 4);
        dissolved.add(Aspect.ORDER, 6);

        for (String id : new String[]{"minecraft:sharpness", "minecraft:mending",
                "minecraft:efficiency", "minecraft:protection"}) {
            EnchantmentAspectRules.Contribution contribution =
                    EnchantmentAspectRules.contribution(id, 3);
            for (Aspect aspect : contribution.aspects().getAspects()) {
                dissolved.merge(aspect, contribution.aspects().getAmount(aspect));
            }
        }

        AspectList capped = AspectTagRules.cap(dissolved, ArcaneresearchCraftingManager.ASPECT_CAP);
        AspectList culled = AspectHelper.cullTags(capped, ArcaneresearchCraftingManager.OBJECT_ASPECT_CAP);

        assertTrue(culled.size() <= 10);
        assertTrue(culled.getAmount(Aspect.AVERSION) > 0, "sharpness contributes aversion");
        assertTrue(culled.getAmount(Aspect.CRAFT) > 0, "mending contributes craft");
        assertTrue(culled.getAmount(Aspect.TOOL) > 0, "efficiency contributes tool");
        for (Aspect aspect : culled.getAspects()) {
            assertTrue(culled.getAmount(aspect) <= 500);
        }
    }

    @Test
    @DisplayName("object tags are capped at ten aspects")
    void aspectCap() {
        AspectList many = new AspectList();
        for (Aspect aspect : new Aspect[]{Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH,
                Aspect.ORDER, Aspect.ENTROPY, Aspect.BEAST, Aspect.PROTECT, Aspect.TOOL,
                Aspect.CRAFT, Aspect.MAGIC, Aspect.DESIRE}) {
            many.add(aspect, 1);
        }
        assertEquals(12, many.size());
        assertEquals(10, AspectHelper.cullTags(many, 10).size());
        assertFalse(AspectHelper.cullTags(many, 10).getAmount(Aspect.AIR) > 0);
    }
}
