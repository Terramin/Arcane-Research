package arcaneresearch.common.tiles.crafting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тигель: чистые правила")
class CrucibleRulesTest {

    @Test
    @DisplayName("constants match the original crucible")
    void constants() {
        assertEquals(200, CrucibleRules.HEAT_MAX);
        assertEquals(151, CrucibleRules.BOIL_HEAT);
        assertEquals(149, CrucibleRules.COOL_SYNC_HEAT);
        assertEquals(50, CrucibleRules.WATER_PER_CRAFT);
        assertEquals(500, CrucibleRules.ASPECT_LIMIT);
        assertEquals(1000, CrucibleRules.TANK_CAPACITY);
        assertEquals(100, CrucibleRules.SPILL_INTERVAL);
        assertEquals(-250L, CrucibleRules.COUNTER_AFTER_CRAFT);
        assertEquals(-150L, CrucibleRules.COUNTER_AFTER_ABSORB);
        assertEquals(-100L, CrucibleRules.COUNTER_INITIAL);
    }

    @Test
    @DisplayName("heat rises only with water and a heat source")
    void heating() {
        assertEquals(1, CrucibleRules.heatDelta(true, true, 0));
        assertEquals(1, CrucibleRules.heatDelta(true, true, 150));
        assertEquals(0, CrucibleRules.heatDelta(true, true, 200));
        assertEquals(-1, CrucibleRules.heatDelta(true, false, 100));
        assertEquals(-1, CrucibleRules.heatDelta(false, true, 100));
        assertEquals(0, CrucibleRules.heatDelta(false, false, 0));
        assertEquals(0, CrucibleRules.heatDelta(true, false, 0));
    }

    @Test
    @DisplayName("heat simulation stops at 200 and cools to zero")
    void heatCurve() {
        int heat = 0;
        for (int i = 0; i < 500; i++) {
            heat += CrucibleRules.heatDelta(true, true, heat);
            assertTrue(heat <= CrucibleRules.HEAT_MAX);
        }
        assertEquals(200, heat);

        while (heat > 0) {
            heat += CrucibleRules.heatDelta(false, false, heat);
        }
        assertEquals(0, heat);
    }

    @Test
    @DisplayName("sync happens only when crossing boiling or cooling thresholds")
    void heatSync() {
        assertTrue(CrucibleRules.shouldSyncHeat(150, 151, true, true));
        assertFalse(CrucibleRules.shouldSyncHeat(151, 152, true, true));
        assertTrue(CrucibleRules.shouldSyncHeat(150, 149, true, false));
        assertFalse(CrucibleRules.shouldSyncHeat(149, 148, false, false));
        assertFalse(CrucibleRules.shouldSyncHeat(150, 149, false, false),
                "cooling without water must not sync");
        assertFalse(CrucibleRules.shouldSyncHeat(148, 149, true, true));
        assertFalse(CrucibleRules.shouldSyncHeat(149, 150, true, true));
    }

    @Test
    @DisplayName("boiling threshold is 151")
    void isHeated() {
        assertFalse(CrucibleRules.isHeated(150));
        assertTrue(CrucibleRules.isHeated(151));
        assertTrue(CrucibleRules.isHeated(200));
    }

    @Test
    @DisplayName("spills happen on overflow and every hundredth tick")
    void spillTiming() {
        assertTrue(CrucibleRules.shouldSpill(501));
        assertFalse(CrucibleRules.shouldSpill(500));
        assertFalse(CrucibleRules.shouldSpill(0));
        assertTrue(CrucibleRules.shouldSpill(1000));
    }

    @Test
    @DisplayName("fluid height grows with water and dissolved vis")
    void fluidHeight() {
        assertEquals(0.3f, CrucibleRules.fluidHeight(0, 0), 1e-6f);
        assertEquals(0.8f, CrucibleRules.fluidHeight(1000, 0), 1e-6f);
        float withVis = CrucibleRules.fluidHeight(500, 250);
        assertTrue(withVis > 0.55f && withVis < 0.9f);
        assertTrue(CrucibleRules.fluidHeight(1000, 500) > CrucibleRules.fluidHeight(1000, 0));
    }

    @Test
    @DisplayName("fluid height keeps the original 1.001/0.9999 quirks")
    void fluidHeightQuirks() {
        assertEquals(1.001f, CrucibleRules.fluidHeight(1000, 10000), 1e-6f);
        assertEquals(0.9999f, CrucibleRules.fluidHeight(0, 500), 1e-6f);
    }

    @Test
    @DisplayName("fluid height never returns NaN for invalid inputs")
    void fluidHeightExtremes() {
        assertEquals(0.3f, CrucibleRules.fluidHeight(-100, -50), 1e-6f);
        assertEquals(1.001f, CrucibleRules.fluidHeight(Integer.MAX_VALUE, Integer.MAX_VALUE), 1e-6f);
        assertTrue(Float.isFinite(CrucibleRules.fluidHeight(Integer.MIN_VALUE, Integer.MIN_VALUE)));
    }

    @Test
    @DisplayName("flux pollution is four times stronger")
    void pollution() {
        assertEquals(1.0f, CrucibleRules.spillPollution(true));
        assertEquals(0.25f, CrucibleRules.spillPollution(false));
    }

    @Test
    @DisplayName("craft and absorb counters are ordered and distinct")
    void counters() {
        assertTrue(CrucibleRules.COUNTER_AFTER_CRAFT < CrucibleRules.COUNTER_AFTER_ABSORB);
        assertTrue(CrucibleRules.COUNTER_AFTER_ABSORB < CrucibleRules.COUNTER_INITIAL);
        assertTrue(CrucibleRules.COUNTER_INITIAL < 0);
    }
}
