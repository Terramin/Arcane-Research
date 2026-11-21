package arcaneresearch.api.lifesteal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Лайфстил: значения и реестр источников")
class LifestealStatsTest {

    @AfterEach
    void clearRegistry() {
        for (ILifestealSource source : List.copyOf(LifestealModifiers.getSources())) {
            LifestealModifiers.unregister(source);
        }
    }

    @Test
    void levelTableMatchesTheEnchantmentDesign() {
        assertEquals(0.0, LifestealStats.fromLevel(0).getChance(), 1.0e-9);
        assertEquals(75.0, LifestealStats.fromLevel(1).getChance(), 1.0e-9);
        assertEquals(1.0, LifestealStats.fromLevel(1).getHeal(), 1.0e-9);
        assertEquals(60.0, LifestealStats.fromLevel(5).getChance(), 1.0e-9);
        assertEquals(5.0, LifestealStats.fromLevel(5).getHeal(), 1.0e-9);
        assertFalse(LifestealStats.fromLevel(6).canLifesteal());
        assertFalse(LifestealStats.fromLevel(-1).canLifesteal());
    }

    @Test
    void constructorClampsToDocumentedCaps() {
        LifestealStats stats = LifestealStats.of(250, 99);
        assertEquals(LifestealStats.MAX_CHANCE, stats.getChance(), 1.0e-9);
        assertEquals(LifestealStats.MAX_HEAL, stats.getHeal(), 1.0e-9);
        assertEquals(1.0, stats.getChanceMultiplier(), 1.0e-9);
        assertTrue(stats.canLifesteal());
        assertFalse(LifestealStats.NONE.canLifesteal());
    }

    @Test
    void plusIsImmutableAndCapsValues() {
        LifestealStats base = LifestealStats.of(40, 2);
        LifestealStats boosted = base.plus(30, 4);

        assertEquals(40.0, base.getChance(), 1.0e-9);
        assertEquals(2.0, base.getHeal(), 1.0e-9);
        assertEquals(70.0, boosted.getChance(), 1.0e-9);
        assertEquals(LifestealStats.MAX_HEAL, boosted.getHeal(), 1.0e-9);

        LifestealStats combined = base.plus(LifestealStats.of(10, 1));
        assertEquals(50.0, combined.getChance(), 1.0e-9);
        assertEquals(3.0, combined.getHeal(), 1.0e-9);
    }

    @Test
    void sourcesAreAppliedInRegistrationOrderAndDeduplicated() {
        List<String> order = new ArrayList<>();
        ILifestealSource first = (player, stats) -> {
            order.add("first");
            return stats.plus(5, 0);
        };
        ILifestealSource second = (player, stats) -> {
            order.add("second");
            return stats.plus(0, 1);
        };

        LifestealModifiers.register(first);
        LifestealModifiers.register(first);
        LifestealModifiers.register(second);
        LifestealModifiers.register(null);

        assertEquals(2, LifestealModifiers.getSources().size());
        LifestealStats result = LifestealModifiers.apply(null, LifestealStats.of(10, 1));
        assertEquals(List.of("first", "second"), order);
        assertEquals(15.0, result.getChance(), 1.0e-9);
        assertEquals(2.0, result.getHeal(), 1.0e-9);

        LifestealModifiers.unregister(first);
        assertEquals(1, LifestealModifiers.getSources().size());
    }
}
