package arcaneresearch.api.crit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Криты: значения и реестр модификаторов")
class CriticalStatsTest {

    @AfterEach
    void clearRegistry() {
        for (ICriticalModifier modifier : List.copyOf(CritModifiers.getModifiers())) {
            CritModifiers.unregister(modifier);
        }
    }

    @Test
    void baselineAndZeroValues() {
        assertEquals(5.0, CritStats.BASE.getChance(), 1.0e-9);
        assertEquals(1.5, CritStats.BASE.getDamageMultiplier(), 1.0e-9);
        assertTrue(CritStats.BASE.canCrit());

        assertFalse(CritStats.NONE.canCrit());
        assertEquals(0.0, CritStats.NONE.getChanceMultiplier(), 1.0e-9);
        assertFalse(new CritStats(-5, -20).canCrit());
        assertEquals(0.0, new CritStats(-5, -20).getDamage(), 1.0e-9);
    }

    @Test
    void plusReturnsANewImmutableSnapshot() {
        CritStats original = CritStats.of(10, 150);
        CritStats boosted = original.plus(5, 25);

        assertEquals(10.0, original.getChance(), 1.0e-9);
        assertEquals(15.0, boosted.getChance(), 1.0e-9);
        assertEquals(175.0, boosted.getDamage(), 1.0e-9);
        assertEquals(20.0, original.plus(CritStats.of(10, 0)).getChance(), 1.0e-9);
    }

    @Test
    void modifiersAreAppliedInRegistrationOrderAndDeduplicated() {
        List<String> order = new ArrayList<>();
        ICriticalModifier first = (player, stats) -> {
            order.add("first");
            return stats.plus(1, 0);
        };
        ICriticalModifier second = (player, stats) -> {
            order.add("second");
            return stats.plus(0, 10);
        };

        CritModifiers.register(first);
        CritModifiers.register(first);
        CritModifiers.register(second);
        CritModifiers.register(null);

        assertEquals(2, CritModifiers.getModifiers().size());
        CritStats result = CritModifiers.apply(null, CritStats.of(5, 100));
        assertEquals(List.of("first", "second"), order);
        assertEquals(6.0, result.getChance(), 1.0e-9);
        assertEquals(110.0, result.getDamage(), 1.0e-9);

        CritModifiers.unregister(first);
        assertEquals(1, CritModifiers.getModifiers().size());
    }
}
