package arcaneresearch.common.tiles.crafting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Alchemy furnace: pure rules")
class ThaumatoriumRulesTest {

    @Test
    @DisplayName("constants match the expected furnace tuning")
    void constants() {
        assertEquals(128, ThaumatoriumRules.SUCTION);
        assertEquals(40, ThaumatoriumRules.REFRESH_INTERVAL);
        assertEquals(5, ThaumatoriumRules.CRAFT_INTERVAL);
        assertEquals(7, ThaumatoriumRules.VENT_DURATION);
        assertEquals(1, ThaumatoriumRules.BASE_RECIPE_SLOTS);
        assertEquals(2, ThaumatoriumRules.SLOTS_PER_BRAIN_BOX);
    }

    @Test
    @DisplayName("heat is refreshed every 40 ticks and crafting runs every 5")
    void tickIntervals() {
        assertTrue(ThaumatoriumRules.isRefreshTick(0));
        assertFalse(ThaumatoriumRules.isRefreshTick(1));
        assertFalse(ThaumatoriumRules.isRefreshTick(39));
        assertTrue(ThaumatoriumRules.isRefreshTick(40));
        assertTrue(ThaumatoriumRules.isRefreshTick(80));

        assertTrue(ThaumatoriumRules.isCraftTick(0));
        for (int i = 1; i < 5; i++) {
            assertFalse(ThaumatoriumRules.isCraftTick(i));
        }
        assertTrue(ThaumatoriumRules.isCraftTick(5));
        assertTrue(ThaumatoriumRules.isCraftTick(10));
    }

    @Test
    @DisplayName("each brain box adds two recipe slots, with no negative capacity")
    void recipeCapacity() {
        assertEquals(1, ThaumatoriumRules.recipeCapacity(0));
        assertEquals(3, ThaumatoriumRules.recipeCapacity(1));
        assertEquals(5, ThaumatoriumRules.recipeCapacity(2));
        assertEquals(1, ThaumatoriumRules.recipeCapacity(-3));
    }

    @Test
    @DisplayName("essentia can only be pulled from the four allowed neighbours")
    void pullDirections() {
        assertFalse(ThaumatoriumRules.canPullFrom(0, true, false, false));
        assertFalse(ThaumatoriumRules.canPullFrom(0, false, true, false));
        assertFalse(ThaumatoriumRules.canPullFrom(0, false, false, true));
        assertTrue(ThaumatoriumRules.canPullFrom(0, false, false, false));

        // Above is allowed on the top layer, but not on the furnace layer.
        assertTrue(ThaumatoriumRules.canPullFrom(1, false, false, true));
        assertFalse(ThaumatoriumRules.canPullFrom(1, true, false, false));
    }

    @Test
    @DisplayName("contents drop whenever the furnace changes into another block")
    void droppingContents() {
        assertTrue(ThaumatoriumRules.shouldDropContents(false, false));
        assertFalse(ThaumatoriumRules.shouldDropContents(false, true));
        assertFalse(ThaumatoriumRules.shouldDropContents(true, false));
        assertFalse(ThaumatoriumRules.shouldDropContents(true, true));
    }

    @Test
    @DisplayName("brass is dropped only for a real break, never for a partner conversion")
    void brassDrop() {
        assertTrue(ThaumatoriumRules.shouldDropBlockItem(false));
        assertFalse(ThaumatoriumRules.shouldDropBlockItem(true));
    }

    @Test
    @DisplayName("recipe slot indices are validated against the list size")
    void slotValidation() {
        assertFalse(ThaumatoriumRules.isRecipeSlotValid(-1, 3));
        assertFalse(ThaumatoriumRules.isRecipeSlotValid(0, 0));
        assertTrue(ThaumatoriumRules.isRecipeSlotValid(0, 1));
        assertTrue(ThaumatoriumRules.isRecipeSlotValid(4, 5));
        assertFalse(ThaumatoriumRules.isRecipeSlotValid(5, 5));
    }

    @Test
    @DisplayName("essentia intake stops once the recipe is satisfied")
    void essenceIntake() {
        assertTrue(ThaumatoriumRules.canAcceptEssence(0, 5));
        assertTrue(ThaumatoriumRules.canAcceptEssence(4, 5));
        assertFalse(ThaumatoriumRules.canAcceptEssence(5, 5));
        assertFalse(ThaumatoriumRules.canAcceptEssence(6, 5));
    }

    @Test
    @DisplayName("vent particles use independent random offsets in [-0.1, 0.1]")
    void ventSpeed() {
        assertEquals(0.1f, ThaumatoriumRules.ventSpeed(0.0f), 1e-6f);
        assertEquals(0.0f, ThaumatoriumRules.ventSpeed(0.5f), 1e-6f);
        assertEquals(-0.1f, ThaumatoriumRules.ventSpeed(1.0f), 1e-6f);
        assertEquals(0.0f, ThaumatoriumRules.ventSpeed(Float.NaN), 0.0f);
        assertEquals(0.0f, ThaumatoriumRules.ventSpeed(Float.POSITIVE_INFINITY), 0.0f);
    }

    @Test
    @DisplayName("tags with invalid values are normalized instead of throwing")
    void tagNormalization() {
        assertEquals(1, ThaumatoriumRules.normalizeMaxRecipes(-5));
        assertEquals(1, ThaumatoriumRules.normalizeMaxRecipes(0));
        assertEquals(3, ThaumatoriumRules.normalizeMaxRecipes(3));
        assertEquals("", ThaumatoriumRules.normalizeOwner(null));
        assertEquals("", ThaumatoriumRules.normalizeOwner(""));
        assertEquals("player", ThaumatoriumRules.normalizeOwner("player"));
    }

    @Test
    @DisplayName("recipe buffer resizes without negative indices")
    void bufferResize() {
        assertEquals(0, ThaumatoriumRules.trimCount(0, 1));
        assertEquals(2, ThaumatoriumRules.trimCount(5, 3));
        assertEquals(0, ThaumatoriumRules.trimCount(2, 5));
        assertEquals(3, ThaumatoriumRules.trimCount(3, -1));
        assertTrue(ThaumatoriumRules.canAddRecipe(0, 1));
        assertFalse(ThaumatoriumRules.canAddRecipe(1, 1));
        assertFalse(ThaumatoriumRules.canAddRecipe(0, 0));
    }

    @Test
    @DisplayName("tube transport respects open/closed sides and available essence")
    void tubeTransport() {
        assertEquals(1, ThaumatoriumRules.takeEssentia(5, 1, true));
        assertEquals(0, ThaumatoriumRules.takeEssentia(5, 1, false));
        assertEquals(0, ThaumatoriumRules.takeEssentia(0, 1, true));
        assertEquals(0, ThaumatoriumRules.takeEssentia(5, 0, true));
        assertEquals(0, ThaumatoriumRules.takeEssentia(-1, 1, true));

        assertEquals(1, ThaumatoriumRules.acceptEssentia(1, true));
        assertEquals(0, ThaumatoriumRules.acceptEssentia(1, false));
        assertEquals(0, ThaumatoriumRules.acceptEssentia(0, true));
        assertEquals(0, ThaumatoriumRules.acceptEssentia(-1, true));
    }

    @Test
    @DisplayName("recipe reselection happens on invalid slots, missing or mismatched recipes")
    void recipeReselection() {
        assertFalse(ThaumatoriumRules.shouldReselectRecipe(0, 1, false, true));
        assertTrue(ThaumatoriumRules.shouldReselectRecipe(-1, 1, false, true));
        assertTrue(ThaumatoriumRules.shouldReselectRecipe(1, 1, false, true));
        assertTrue(ThaumatoriumRules.shouldReselectRecipe(0, 1, true, false));
        assertTrue(ThaumatoriumRules.shouldReselectRecipe(0, 1, false, false));
    }

    @Test
    @DisplayName("quick move tries the catalyst slot, then hotbar or main inventory")
    void quickMoveRanges() {
        assertArrayEquals(new int[][]{{1, 37}}, ThaumatoriumRules.moveAttempts(0, 37));
        assertArrayEquals(new int[][]{{0, 1}, {28, 37}}, ThaumatoriumRules.moveAttempts(5, 37));
        assertArrayEquals(new int[][]{{0, 1}, {1, 28}, {1, 37}},
                ThaumatoriumRules.moveAttempts(30, 37));
    }

    @Test
    @DisplayName("catalogue fingerprint changes with the recipe set and is order-sensitive")
    void catalogFingerprint() {
        assertEquals(63, ThaumatoriumRules.recipeFingerprint(java.util.List.of(1)));
        assertEquals(ThaumatoriumRules.recipeFingerprint(java.util.List.of(1, 2)),
                ThaumatoriumRules.recipeFingerprint(java.util.List.of(1, 2)));
        assertFalse(ThaumatoriumRules.recipeFingerprint(java.util.List.of(1, 2))
                == ThaumatoriumRules.recipeFingerprint(java.util.List.of(2, 1)));
        assertFalse(ThaumatoriumRules.recipeFingerprint(java.util.List.of(1, 2))
                == ThaumatoriumRules.recipeFingerprint(java.util.List.of(1, 3)));
    }

    @Test
    @DisplayName("aspect requirements are consumed monotonically to zero")
    void aspectConsumption() {
        assertEquals(5, ThaumatoriumRules.remainingEssentia(5, 0));
        assertEquals(2, ThaumatoriumRules.remainingEssentia(5, 3));
        assertEquals(0, ThaumatoriumRules.remainingEssentia(5, 5));
        assertEquals(0, ThaumatoriumRules.remainingEssentia(5, 9));
        assertFalse(ThaumatoriumRules.isSatisfied(5, 4));
        assertTrue(ThaumatoriumRules.isSatisfied(5, 5));

        int stored = 0;
        for (int step = 1; step <= 5; step++) {
            stored += ThaumatoriumRules.takeEssentia(5, 1, true);
            assertTrue(ThaumatoriumRules.remainingEssentia(5, stored) <= 5 - step + 1);
        }
        assertTrue(ThaumatoriumRules.isSatisfied(5, stored));
    }
}
