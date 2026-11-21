package arcaneresearch.lib.vec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quat: кватернионы без обращения к Minecraft")
class QuatTest {

    private static final double DELTA = 1.0e-3;

    @Test
    void identityQuaternionDoesNothing() {
        Quat identity = new Quat();
        Vector3 vector = new Vector3(1.0, 2.0, 3.0);

        assertEquals(1.0, identity.s, DELTA);
        assertEquals(0.0, identity.x, DELTA);
        assertEquals(0.0, identity.y, DELTA);
        assertEquals(0.0, identity.z, DELTA);

        identity.rotate(vector);
        assertEquals(new Vector3(1.0, 2.0, 3.0), vector);
    }

    @Test
    void rotationAroundXAxisMapsYToZ() {
        Quat rotation = Quat.aroundAxis(1.0, 0.0, 0.0, Math.PI / 2);
        Vector3 vector = new Vector3(0.0, 1.0, 0.0);

        rotation.rotate(vector);

        assertEquals(0.0, vector.x, DELTA);
        assertEquals(0.0, vector.y, DELTA);
        assertEquals(1.0, vector.z, DELTA);
    }

    @Test
    void rotationAroundAxisUsesHalfAngle() {
        Quat rotation = Quat.aroundAxis(new Vector3(0.0, 1.0, 0.0), Math.PI / 2);

        assertEquals(Math.cos(Math.PI / 4), rotation.s, DELTA);
        assertEquals(Math.sin(Math.PI / 4), rotation.y, DELTA);
        assertEquals(0.0, rotation.x, DELTA);
        assertEquals(0.0, rotation.z, DELTA);
    }

    @Test
    void normalizeProducesUnitMagnitudeAndIgnoresZeroQuaternion() {
        Quat quat = new Quat(2.0, 0.0, 0.0, 0.0).normalize();

        assertEquals(1.0, quat.mag(), DELTA);
        assertEquals(1.0, quat.s, DELTA);

        Quat zero = new Quat(0.0, 0.0, 0.0, 0.0).normalize();
        assertEquals(0.0, zero.mag(), DELTA);
    }

    @Test
    void multiplyAndRightMultiplyUseOppositeCompositionOrder() {
        Quat x = Quat.aroundAxis(1.0, 0.0, 0.0, Math.PI / 2);
        Quat y = Quat.aroundAxis(0.0, 1.0, 0.0, Math.PI / 2);

        // multiply() appends on the left: x.multiply(y) == y * x.
        Quat left = x.copy().multiply(y.copy());
        Quat same = y.copy().rightMultiply(x.copy());
        assertQuatEquals(same, left);

        // rightMultiply() appends on the right and is generally different.
        Quat right = x.copy().rightMultiply(y.copy());
        assertEquals(1.0, right.mag(), DELTA);
        org.junit.jupiter.api.Assertions.assertFalse(quatEquals(left, right));
    }

    @Test
    void copyIsIndependent() {
        Quat original = new Quat(1.0, 2.0, 3.0, 4.0);
        Quat copy = original.copy();

        assertNotSame(original, copy);
        assertQuatEquals(original, copy);
        copy.x = 42.0;
        assertEquals(2.0, original.x, DELTA);
    }

    @Test
    void toStringUsesRoundedComponents() {
        String text = new Quat().toString();
        org.junit.jupiter.api.Assertions.assertTrue(text.matches("Quat\\([^)]*\\)"));
        org.junit.jupiter.api.Assertions.assertEquals(3, text.chars().filter(c -> c == ',').count());
    }

    private static boolean quatEquals(Quat a, Quat b) {
        return Math.abs(a.s - b.s) < DELTA && Math.abs(a.x - b.x) < DELTA
                && Math.abs(a.y - b.y) < DELTA && Math.abs(a.z - b.z) < DELTA;
    }

    private static void assertQuatEquals(Quat expected, Quat actual) {
        assertEquals(expected.s, actual.s, DELTA);
        assertEquals(expected.x, actual.x, DELTA);
        assertEquals(expected.y, actual.y, DELTA);
        assertEquals(expected.z, actual.z, DELTA);
    }
}
