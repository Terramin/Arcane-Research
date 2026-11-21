package arcaneresearch.lib.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("MathHelper: чистая математика без Minecraft")
class MathHelperTest {

    private static final double DELTA = 1.0e-3;

    @Test
    void trigonometricTableHasCorrectQuadrantValues() {
        assertEquals(0.0, MathHelper.sin(0.0), DELTA);
        assertEquals(1.0, MathHelper.sin(Math.PI / 2), DELTA);
        assertEquals(0.0, MathHelper.sin(Math.PI), DELTA);
        // Regression: the 3rd quadrant entry used to be +1 instead of -1.
        assertEquals(-1.0, MathHelper.sin(3 * Math.PI / 2), DELTA);
        assertEquals(0.0, MathHelper.sin(2 * Math.PI), DELTA);

        assertEquals(1.0, MathHelper.cos(0.0), DELTA);
        assertEquals(0.0, MathHelper.cos(Math.PI / 2), DELTA);
        assertEquals(-1.0, MathHelper.cos(Math.PI), DELTA);
        assertEquals(1.0, MathHelper.cos(2 * Math.PI), DELTA);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0, 4.0, 0.5, 1.5",
            "4.0, 1.0, 0.5, 3.5",
            "1.0, 4.0, 10.0, 4.0",
            "4.0, 1.0, 10.0, 1.0"
    })
    void approachLinearMovesByAtMostMax(float from, float to, float max, float expected) {
        assertEquals(expected, MathHelper.approachLinear(from, to, max), DELTA);
    }

    @Test
    void interpolationAndExponentialApproach() {
        assertEquals(2.5, MathHelper.interpolate(1.0, 4.0, 0.5), DELTA);
        assertEquals(2.5, MathHelper.approachExp(1.0, 4.0, 0.5), DELTA);
        assertEquals(2.0, MathHelper.approachExp(1.0, 4.0, 0.5, 1.0), DELTA);
        assertEquals(-0.5, MathHelper.approachExp(1.0, 4.0, -0.5), DELTA);
    }

    @Test
    void retreatStopsAtTarget() {
        assertEquals(0.0, MathHelper.retreatExp(0.0, -10.0, 0.0, 0.5, 0.0), DELTA);
        assertEquals(-5.0, MathHelper.retreatExp(0.0, -10.0, 10.0, 0.5, 0.0), DELTA);
        assertEquals(-10.0, MathHelper.retreatExp(0.0, -10.0, 0.0, 0.5, 20.0), DELTA);
        assertEquals(0.0, MathHelper.retreatExp(0.0, 10.0, 0.0, 0.0, 0.0), DELTA);
    }

    @Test
    void integerHelpersSnapWhenMovementRoundsToZero() {
        assertEquals(10, MathHelper.approachExpI(0, 10, 0.01));
        assertEquals(3, MathHelper.approachExpI(0, 10, 0.26));
        assertEquals(-10, MathHelper.retreatExpI(0, -10, 0, 0.01, 0));
        assertEquals(-3, MathHelper.retreatExpI(0, -10, 10, 0.26, 0));
    }

    @Test
    void clipAndBetweenUseInclusiveBounds() {
        assertEquals(5.0, MathHelper.clip(5.0, 0.0, 10.0), DELTA);
        assertEquals(0.0, MathHelper.clip(-1.0, 0.0, 10.0), DELTA);
        assertEquals(10.0, MathHelper.clip(11.0, 0.0, 10.0), DELTA);
        assertTrue(MathHelper.between(0.0, 0.0, 1.0));
        assertTrue(MathHelper.between(0.0, 1.0, 1.0));
        assertFalse(MathHelper.between(0.0, 1.1, 1.0));
    }

    @Test
    void roundingAndComparisonHelpers() {
        assertEquals(2, MathHelper.roundAway(1.2));
        assertEquals(-2, MathHelper.roundAway(-1.2));
        assertEquals(0, MathHelper.compare(4, 4));
        assertEquals(-1, MathHelper.compare(3, 4));
        assertEquals(1, MathHelper.compare(5, 4));
        assertEquals(-1, MathHelper.compare(3.0, 4.0));
    }
}
