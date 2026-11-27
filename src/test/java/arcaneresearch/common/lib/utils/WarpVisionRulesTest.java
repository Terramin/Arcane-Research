package arcaneresearch.common.lib.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Warp: конус Death Gaze")
class WarpVisionRulesTest {

    private static final double[] APEX = {0, 0, 0};
    private static final double[] AXIS = {0, 0, 10};

    @Test
    @DisplayName("цель прямо по направлению внутри радиуса видна")
    void targetAhead() {
        assertTrue(WarpVisionRules.isLyingInCone(new double[]{0, 0, 5}, APEX, AXIS, 0.75f));
    }

    @Test
    @DisplayName("цель за пределами round cap не видна")
    void beyondRange() {
        assertFalse(WarpVisionRules.isLyingInCone(new double[]{0, 0, 12}, APEX, AXIS, 0.75f));
    }

    @Test
    @DisplayName("цель позади не видна")
    void behind() {
        assertFalse(WarpVisionRules.isLyingInCone(new double[]{0, 0, -5}, APEX, AXIS, 0.75f));
    }

    @Test
    @DisplayName("апертура 0.75 — радианы, а не косинус: 15 градусов внутри, 30 снаружи")
    void apertureIsRadians() {
        assertTrue(WarpVisionRules.isLyingInCone(
                new double[]{5 * Math.sin(Math.toRadians(15)), 0,
                        5 * Math.cos(Math.toRadians(15))}, APEX, AXIS, 0.75f));
        assertFalse(WarpVisionRules.isLyingInCone(
                new double[]{5 * Math.sin(Math.toRadians(30)), 0,
                        5 * Math.cos(Math.toRadians(30))}, APEX, AXIS, 0.75f));
    }

    @Test
    @DisplayName("старая проверка dot >= 0.75 была бы шире: 30 градусов проходила бы")
    void oldCosineWouldBeWider() {
        double cos30 = Math.cos(Math.toRadians(30));
        assertTrue(cos30 >= 0.75);
        assertFalse(WarpVisionRules.isLyingInCone(
                new double[]{5 * Math.sin(Math.toRadians(30)), 0,
                        5 * Math.cos(Math.toRadians(30))}, APEX, AXIS, 0.75f));
    }

    @Test
    @DisplayName("нулевой вектор оси не создаёт NaN и не совпадает")
    void zeroAxis() {
        assertFalse(WarpVisionRules.isLyingInCone(new double[]{0, 0, 5}, APEX, APEX, 0.75f));
    }
}
