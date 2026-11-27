package arcaneresearch.common.tiles.crafting;

import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тигель: симуляция и предельные режимы")
class CrucibleSimulationTest {

    private static final class Furnace {
        int heat;
        int water;
        int vis;
        long counter = CrucibleRules.COUNTER_INITIAL;
        boolean heatSource;
        boolean smelting;
        int syncs;
        int spills;
        int crafted;

        void tick() {
            int previous = heat;
            heat += CrucibleRules.heatDelta(water > 0, heatSource, heat);
            if (CrucibleRules.shouldSyncHeat(previous, heat, water > 0, heatSource)) {
                syncs++;
            }
            if (CrucibleRules.shouldSpill(vis)) {
                spills++;
                vis = Math.max(0, vis - 1);
            }
            if (counter >= CrucibleRules.SPILL_INTERVAL) {
                spills++;
                counter = 0;
            } else {
                counter++;
            }
            if (smelting && CrucibleRules.isHeated(heat) && water >= CrucibleRules.WATER_PER_CRAFT) {
                water -= CrucibleRules.WATER_PER_CRAFT;
                crafted++;
                counter = CrucibleRules.COUNTER_AFTER_CRAFT;
            }
        }

        String snapshot() {
            return heat + "/" + water + "/" + vis + "/" + counter;
        }
    }

    @Test
    @DisplayName("thousand-tick simulation stays within all invariant bounds")
    void simulation() {
        Furnace furnace = new Furnace();

        for (int tick = 0; tick < 1000; tick++) {
            furnace.heatSource = tick < 300 || tick > 600;
            furnace.smelting = tick % 40 == 0;
            furnace.water = Math.min(CrucibleRules.TANK_CAPACITY, furnace.water + 55);
            if (tick % 7 == 0) {
                furnace.vis += 12;
            }
            furnace.tick();

            assertTrue(furnace.heat >= 0 && furnace.heat <= CrucibleRules.HEAT_MAX);
            assertTrue(furnace.water >= 0 && furnace.water <= CrucibleRules.TANK_CAPACITY);
            float height = CrucibleRules.fluidHeight(furnace.water, furnace.vis);
            assertTrue(height >= 0.3f && height <= 1.001f + 1e-6f);
        }

        assertTrue(furnace.crafted > 0, "no crafts completed");
        assertTrue(furnace.spills > 0, "no spills happened");
        assertTrue(furnace.syncs > 0, "no heat syncs happened");
    }

    @Test
    @DisplayName("same seed produces byte-for-byte identical traces")
    void deterministicTrace() {
        assertEquals(trace(42L), trace(42L));
        assertFalse(trace(42L).equals(trace(43L)));
        assertTrue(trace(42L).contains("craft="));
    }

    private static String trace(long seed) {
        Random random = new Random(seed);
        Furnace furnace = new Furnace();
        StringBuilder log = new StringBuilder();
        for (int tick = 0; tick < 200; tick++) {
            furnace.heatSource = true;
            furnace.water = Math.min(CrucibleRules.TANK_CAPACITY, furnace.water + 60);
            if (random.nextInt(3) == 0) {
                furnace.vis += 10;
            }
            furnace.smelting = tick % 25 == 0;
            furnace.tick();
            log.append(tick).append(':').append(furnace.snapshot()).append(';');
        }
        log.append("craft=").append(furnace.crafted);
        return log.toString();
    }

    @Test
    @DisplayName("fuzz: twenty thousand random states preserve every invariant")
    void fuzzInvariants() {
        Random random = new Random(1234567L);
        for (int i = 0; i < 20_000; i++) {
            int heat = random.nextInt(1000) - 300;
            int water = random.nextInt(2000) - 500;
            int vis = random.nextInt(2000) - 200;
            long counter = random.nextInt(1000) - 200;

            boolean heatSource = random.nextBoolean();
            int delta = CrucibleRules.heatDelta(water > 0, heatSource, heat);
            assertTrue(delta >= -1 && delta <= 1, "heat delta at i=" + i);

            int nextHeat = Math.max(0, Math.min(CrucibleRules.HEAT_MAX, heat + delta));
            assertTrue(CrucibleRules.isHeated(nextHeat) == (nextHeat >= CrucibleRules.BOIL_HEAT));
            // Explicit boundary table: delta is at most +/-1, so these are the only
            // possible sync transitions. Do not re-use the implementation's formula.
            boolean expectedSync = (heat == 150 && nextHeat == 151)
                    || (heat == 150 && nextHeat == 149 && water > 0);
            assertEquals(expectedSync, CrucibleRules.shouldSyncHeat(heat, nextHeat, water > 0, heatSource),
                    "sync at i=" + i);

            float height = CrucibleRules.fluidHeight(water, vis);
            assertTrue(Float.isFinite(height) && height >= 0.0f && height <= 1.001f + 1e-6f, "height at i=" + i);

            boolean spill = CrucibleRules.shouldSpill(vis);
            assertTrue(spill == (vis > 500), "spill at i=" + i);

            float pollution = CrucibleRules.spillPollution(random.nextBoolean());
            assertTrue(pollution == 1.0f || pollution == 0.25f);
        }
    }

    @Test
    @DisplayName("hot rule methods do not allocate per call")
    void allocationFree() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean() instanceof ThreadMXBean b ? b : null;
        Assumptions.assumeTrue(bean != null && bean.isThreadAllocatedMemorySupported(),
                "no allocation counters");

        long id = Thread.currentThread().getId();
        for (int i = 0; i < 200_000; i++) {
            sink += CrucibleRules.heatDelta(true, true, i % 200);
            sink += CrucibleRules.fluidHeight(i % 1000, i % 500);
            sink += CrucibleRules.shouldSpill(i % 600) ? 1 : 0;
        }
        long before = bean.getThreadAllocatedBytes(id);
        for (int i = 0; i < 1_000_000; i++) {
            sink += CrucibleRules.heatDelta(true, true, i % 200);
            sink += CrucibleRules.fluidHeight(i % 1000, i % 500);
            sink += CrucibleRules.shouldSpill(i % 600) ? 1 : 0;
        }
        long allocated = bean.getThreadAllocatedBytes(id) - before;
        assertTrue(allocated < 200_000L, "3M calls allocated " + allocated + " bytes");
        assertTrue(sink != 0.0);
    }

    private static volatile double sink;
}
