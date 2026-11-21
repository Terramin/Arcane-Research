package arcaneresearch.lib.vec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Измерения: прямоугольные параллелепипеды")
class CuboidMeasurementsTest {

    @Test
    void centerAndSidesAreMeasuredFromTheCorners() {
        Cuboid6 cuboid = new Cuboid6(0, 2, 4, 10, 8, 12);

        assertEquals(new Vector3(5, 5, 8), cuboid.center());
        assertEquals(0.0, cuboid.getSide(4));
        assertEquals(10.0, cuboid.getSide(5));
        assertEquals(2.0, cuboid.getSide(0));
        assertEquals(8.0, cuboid.getSide(1));
        assertEquals(4.0, cuboid.getSide(2));
        assertEquals(12.0, cuboid.getSide(3));
    }

    @Test
    void expansionGrowsInBothDirectionsAndOffsetMovesBothCorners() {
        Cuboid6 cuboid = new Cuboid6(0, 0, 0, 4, 4, 4);

        cuboid.expand(1.0);
        assertCuboidEquals(new Cuboid6(-1, -1, -1, 5, 5, 5), cuboid);

        cuboid.offset(new Cuboid6(1, 2, 3, 1, 2, 3));
        assertCuboidEquals(new Cuboid6(0, 1, 2, 6, 7, 8), cuboid);
    }

    @Test
    void enclosingIncludesPointsAndOtherCuboids() {
        Cuboid6 cuboid = new Cuboid6(0, 0, 0, 1, 1, 1);

        cuboid.enclose(new Vector3(-2, 3, 0.5));
        cuboid.enclose(new Cuboid6(-4, -4, -4, 0, 0, 0));

        assertCuboidEquals(new Cuboid6(-4, -4, -4, 1, 3, 1), cuboid);
    }

    @Test
    void touchingCuboidsDoNotIntersect() {
        Cuboid6 first = new Cuboid6(0, 0, 0, 1, 1, 1);

        assertTrue(first.intersects(new Cuboid6(0.5, 0.5, 0.5, 2, 2, 2)));
        assertFalse(first.intersects(new Cuboid6(1, 0, 0, 2, 1, 1)));
        assertFalse(first.intersects(new Cuboid6(-2, 0, 0, 0, 1, 1)));
        assertTrue(Cuboid6.intersects(first, first.copy()));
    }

    @Test
    void setSideAndCopyMutateIndependentState() {
        Cuboid6 cuboid = new Cuboid6(0, 0, 0, 1, 1, 1);
        Cuboid6 copy = cuboid.copy();

        assertNotSame(cuboid, copy);
        cuboid.setSide(5, 7.0);
        assertEquals(7.0, cuboid.max.x);
        assertEquals(1.0, copy.max.x);
        assertFalse(cuboid.equals(copy)); // Cuboid6 has no equals: identity semantics
    }

    // Cuboid6 has no equals(): compare the measured corners field by field.
    private static void assertCuboidEquals(Cuboid6 expected, Cuboid6 actual) {
        assertEquals(expected.min, actual.min, "min");
        assertEquals(expected.max, actual.max, "max");
    }
}
