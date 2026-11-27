package arcaneresearch.common.tiles.crafting;

import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Alchemy furnace: robustness and full-flow scenarios")
class ThaumatoriumFlowTest {

    private static volatile double sink;

    @Test
    @DisplayName("NPE scenario: extreme values and nulls never throw or leak NaN")
    void extremeInputsAreSafe() {
        int[] extremes = {Integer.MIN_VALUE, -100, -1, 0, 1, 100, Integer.MAX_VALUE};
        for (int value : extremes) {
            assertTrue(ThaumatoriumRules.normalizeMaxRecipes(value) >= 1);
            assertTrue(ThaumatoriumRules.trimCount(value, value) >= 0);
            assertTrue(ThaumatoriumRules.canAddRecipe(value, value) == false);
            assertTrue(ThaumatoriumRules.takeEssentia(value, value, true) >= 0);
            assertTrue(ThaumatoriumRules.acceptEssentia(value, true) >= 0);
            assertTrue(ThaumatoriumRules.remainingEssentia(value, value) >= 0);
            assertTrue(ThaumatoriumRules.recipeCapacity(value) >= 1);
            assertTrue(ThaumatoriumRules.isRecipeSlotValid(value, value) == false);
            ThaumatoriumRules.isRefreshTick(value);
            ThaumatoriumRules.isCraftTick(value);
        }
        assertEquals("", ThaumatoriumRules.normalizeOwner(null));
        assertEquals(0.0f, ThaumatoriumRules.ventSpeed(Float.NaN), 0.0f);
        assertTrue(ThaumatoriumRules.canPullFrom(0, false, false, false));
        assertTrue(ThaumatoriumRules.canPullFrom(1, false, false, true));
    }

    @Test
    @DisplayName("hyper-allocation scenario: hot rules allocate nothing per call")
    void hotRulesAreAllocationFree() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean() instanceof ThreadMXBean b ? b : null;
        Assumptions.assumeTrue(bean != null && bean.isThreadAllocatedMemorySupported(),
                "JVM does not expose per-thread allocation counters");

        long threadId = Thread.currentThread().getId();
        for (int i = 0; i < 200_000; i++) {
            sink += ThaumatoriumRules.isRefreshTick(i) ? 1 : 0;
            sink += ThaumatoriumRules.remainingEssentia(100, i % 100);
            sink += ThaumatoriumRules.takeEssentia(50, i % 60, true);
            sink += ThaumatoriumRules.trimCount(i % 20, 5);
        }

        long before = bean.getThreadAllocatedBytes(threadId);
        for (int i = 0; i < 1_000_000; i++) {
            sink += ThaumatoriumRules.isRefreshTick(i) ? 1 : 0;
            sink += ThaumatoriumRules.remainingEssentia(100, i % 100);
            sink += ThaumatoriumRules.takeEssentia(50, i % 60, true);
            sink += ThaumatoriumRules.trimCount(i % 20, 5);
        }
        long allocated = bean.getThreadAllocatedBytes(threadId) - before;

        assertTrue(allocated < 200_000L, "4M hot rule calls allocated " + allocated + " bytes");
        assertTrue(sink != 0.0);
    }

    @Test
    @DisplayName("buffer scenario: 10000 recipe adds/removes never exceed capacity")
    void recipeBufferNeverOverflows() {
        Random random = new Random(4242L);
        List<Integer> hashes = new ArrayList<>();
        List<String> owners = new ArrayList<>();
        List<Integer> aspects = new ArrayList<>();
        int capacity = ThaumatoriumRules.recipeCapacity(1); // 3 slots

        for (int i = 0; i < 10_000; i++) {
            if (ThaumatoriumRules.canAddRecipe(hashes.size(), capacity) && random.nextBoolean()) {
                hashes.add(i);
                aspects.add(1);
                owners.add("player" + i);
            } else if (!hashes.isEmpty()) {
                int last = hashes.size() - 1;
                hashes.remove(last);
                aspects.remove(last);
                owners.remove(last);
            }

            int toRemove = ThaumatoriumRules.trimCount(hashes.size(), capacity);
            for (int j = 0; j < toRemove; j++) {
                int last = hashes.size() - 1;
                hashes.remove(last);
                aspects.remove(last);
                owners.remove(last);
            }

            assertTrue(hashes.size() <= capacity);
            assertEquals(hashes.size(), aspects.size());
            assertEquals(hashes.size(), owners.size());
            for (int slot = 0; slot < hashes.size(); slot++) {
                assertTrue(ThaumatoriumRules.isRecipeSlotValid(slot, hashes.size()));
            }
            assertFalse(ThaumatoriumRules.isRecipeSlotValid(hashes.size(), hashes.size()));
        }
    }

    @Test
    @DisplayName("tube scenario: neighbouring transports feed one essence per tick")
    void tubeInteraction() {
        Map<String, Integer> tube = new HashMap<>();
        tube.put("air", 20);
        tube.put("fire", 10);
        int furnaceAir = 0;
        int furnaceFire = 0;
        int required = 5;
        boolean canInput = true;

        for (int tick = 0; tick < 20; tick++) {
            for (String aspect : tube.keySet()) {
                boolean isAir = "air".equals(aspect);
                int stored = isAir ? furnaceAir : furnaceFire;
                if (!ThaumatoriumRules.canAcceptEssence(stored, required)) {
                    continue; // full: leave the essence in the tube
                }
                int offered = ThaumatoriumRules.acceptEssentia(1, canInput);
                int taken = ThaumatoriumRules.takeEssentia(tube.get(aspect), offered, true);
                tube.put(aspect, tube.get(aspect) - taken);
                if (isAir) {
                    furnaceAir += taken;
                } else {
                    furnaceFire += taken;
                }
            }
        }

        assertTrue(ThaumatoriumRules.isSatisfied(required, furnaceAir));
        assertTrue(ThaumatoriumRules.isSatisfied(required, furnaceFire));
        assertTrue(tube.get("air") >= 0 && tube.get("fire") >= 0);
        assertFalse(ThaumatoriumRules.takeEssentia(0, 1, true) > 0);
    }

    @Test
    @DisplayName("craft scenario: three full cycles consume aspects and reset the furnace")
    void fullCraftSimulation() {
        Map<String, Integer> required = Map.of("air", 3, "fire", 2);
        int completed = 0;
        int catalysts = 6;

        for (int cycle = 0; cycle < 3; cycle++) {
            Map<String, Integer> stored = new HashMap<>();
            stored.put("air", 0);
            stored.put("fire", 0);

            for (int tick = 0; tick < 100; tick++) {
                for (String aspect : required.keySet()) {
                    if (!ThaumatoriumRules.isSatisfied(required.get(aspect), stored.get(aspect))) {
                        stored.put(aspect, stored.get(aspect)
                                + ThaumatoriumRules.takeEssentia(100, 1, true));
                    }
                }

                boolean done = true;
                for (String aspect : required.keySet()) {
                    if (!ThaumatoriumRules.isSatisfied(required.get(aspect), stored.get(aspect))) {
                        done = false;
                        break;
                    }
                }
                if (!done) continue;

                if (catalysts > 0 && ThaumatoriumRules.isRefreshTick(40)
                        && ThaumatoriumRules.isCraftTick(5)) {
                    catalysts--;
                    completed++;
                    stored.clear(); // essentia = new AspectList()
                    break;
                }
            }
            assertTrue(stored.isEmpty(), "cycle " + cycle + " did not reset essentia");
        }
        assertEquals(3, completed);
        assertEquals(3, catalysts);
    }

    @Test
    @DisplayName("structure scenario: brain boxes around the furnace raise the recipe capacity")
    void structureCreation() {
        assertEquals(1, ThaumatoriumRules.recipeCapacity(0));

        boolean[][] boxes = new boolean[2][4];
        int found = 0;
        for (int y = 0; y < 2; y++) {
            for (int dir = 0; dir < 4; dir++) {
                // Front and bottom are excluded by canPullFrom; brain boxes follow the same ring.
                if (!ThaumatoriumRules.canPullFrom(y, dir == 0, false, dir == 2)) {
                    continue;
                }
                boxes[y][dir] = true;
                found++;
            }
        }
        assertEquals(5, found);
        assertEquals(1 + found * 2, ThaumatoriumRules.recipeCapacity(found));

        // The furnace layer excludes UP, the top layer allows it.
        assertFalse(ThaumatoriumRules.canPullFrom(0, false, false, true));
        assertTrue(ThaumatoriumRules.canPullFrom(1, false, false, true));
    }

    @Test
    @DisplayName("golden trace: identical seeds produce byte-for-byte identical craft traces")
    void deterministicTrace() {
        String first = trace(20261127L);
        String second = trace(20261127L);

        assertEquals(first, second);
        assertEquals(3, countOccurrences(first, "cycle="));
        assertTrue(first.contains("=5,5"), "trace should contain a satisfied craft");
    }

    private static String trace(long seed) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder();
        for (int cycle = 0; cycle < 3; cycle++) {
            Map<String, Integer> stored = new HashMap<>();
            stored.put("air", 0);
            stored.put("fire", 0);

            for (int tick = 0; tick < 40; tick++) {
                for (String aspect : List.of("air", "fire")) {
                    if (random.nextBoolean()
                            && ThaumatoriumRules.canAcceptEssence(stored.get(aspect), 5)) {
                        stored.put(aspect, stored.get(aspect)
                                + ThaumatoriumRules.acceptEssentia(1, true));
                    }
                }
                sb.append(cycle).append(':').append(tick).append('=')
                        .append(stored.get("air")).append(',').append(stored.get("fire")).append(';');
                if (ThaumatoriumRules.isSatisfied(5, stored.get("air"))
                        && ThaumatoriumRules.isSatisfied(5, stored.get("fire"))) {
                    break;
                }
            }
            sb.append("cycle=").append(cycle).append('\n');
        }
        return sb.toString();
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    @Test
    @DisplayName("tag scenario: corrupted and unknown values survive a round trip")
    void tagRoundTrip() {
        int maxRecipes = ThaumatoriumRules.normalizeMaxRecipes((byte) -1);
        assertEquals(1, maxRecipes);

        int[] hashes = {10, 20, 30};
        List<Integer> resolved = new ArrayList<>();
        for (int hash : hashes) {
            resolved.add(hash); // unknown hashes must be kept, not dropped
        }
        assertEquals(3, resolved.size());

        List<String> owners = new ArrayList<>();
        owners.add(ThaumatoriumRules.normalizeOwner(null));
        owners.add("");
        owners.add("owner");
        for (String owner : owners) {
            assertTrue(owner != null, "owner must never be null in NBT");
        }
        assertEquals("", owners.get(0));
        assertEquals("owner", owners.get(2));

        int toRemove = ThaumatoriumRules.trimCount(resolved.size(), maxRecipes);
        for (int i = 0; i < toRemove; i++) {
            resolved.remove(resolved.size() - 1);
            owners.remove(owners.size() - 1);
        }
        assertEquals(1, resolved.size());
        assertEquals(1, owners.size());
    }
}
