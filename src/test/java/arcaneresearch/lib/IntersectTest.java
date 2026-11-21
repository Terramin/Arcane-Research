package arcaneresearch.lib;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Измерения: пересечения и плоскости")
class IntersectTest {

    private static final double DELTA = 1.0e-6;

    @Test
    void colinearityDetectsStraightAndDuplicatePoints() {
        assertTrue(intersect.COLINEAR(new double[]{0, 0, 0}, new double[]{1, 0, 0}, new double[]{2, 0, 0}));
        assertTrue(intersect.COLINEAR(new double[]{1, 0, 0}, new double[]{1, 0, 0}, new double[]{1, 0, 0}));
        assertFalse(intersect.COLINEAR(new double[]{0, 0, 0}, new double[]{1, 0, 0}, new double[]{1, 1, 0}));
    }

    @Test
    void bisectingPlaneNormalIsTheNormalizedAngleBisector() {
        double[] normal = intersect.bisecting_plane(
                new double[]{1, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 1, 0});

        assertArrayEquals(new double[]{0.7071067811865476, -0.7071067811865476, 0}, normal, DELTA);
        assertEqualsLength(normal);
    }

    @Test
    void cuttingPlaneWritesThePerpendicularDirectionIntoThePassedArray() {
        double[] normal = new double[3];
        boolean valid = intersect.CUTTING_PLANE(normal,
                new double[]{1, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 1, 0});

        assertTrue(valid);
        assertArrayEquals(new double[]{-0.7071067811865476, -0.7071067811865476, 0}, normal, DELTA);
    }

    @Test
    void degenerateSegmentsAreRejected() {
        double[] normal = new double[3];
        assertFalse(intersect.CUTTING_PLANE(normal,
                new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 0, 0}));
        assertArrayEquals(new double[]{0, 0, 0},
                intersect.bisecting_plane(new double[]{0, 0, 0}, new double[]{0, 0, 0}, new double[]{0, 0, 0}), DELTA);
    }

    @Test
    void segmentPlaneIntersectionIsComputedAtTheCrossingPoint() {
        double[] point = intersect.INNERSECT(
                new double[]{0, 0, 0}, new double[]{0, 0, 1},
                new double[]{0, 0, -1}, new double[]{0, 0, 1});

        assertArrayEquals(new double[]{0, 0, 0}, point, DELTA);
    }

    @Test
    void parallelSegmentHasNoIntersection() {
        double[] point = intersect.INNERSECT(
                new double[]{0, 0, 0}, new double[]{0, 0, 1},
                new double[]{-1, 0, 1}, new double[]{1, 0, 1});

        assertArrayEquals(new double[]{0, 0, 0}, point, DELTA);
    }

    @Test
    void degeneratePointSearchSkipsRepeatedPoints() {
        double[][] points = {
                {0, 0, 0}, {0, 0, 0}, {1, 0, 0}, {2, 0, 0}
        };
        double[] diff = new double[3];

        int index = intersect.FIND_NON_DEGENERATE_POINT(0, points.length, 0.0, diff, points);

        assertTrue(index >= 2);
        assertArrayEquals(new double[]{1, 0, 0}, diff, DELTA);
    }

    private static void assertEqualsLength(double[] vector) {
        org.junit.jupiter.api.Assertions.assertEquals(1.0, matrix.VEC_LENGTH(vector), DELTA);
    }
}
