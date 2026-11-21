package arcaneresearch.common.lib.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.utils.HexUtils.CubicHex;
import arcaneresearch.common.lib.utils.HexUtils.Hex;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("common HexUtils: гексагональная геометрия")
class HexUtilsTest {

    @Nested
    @DisplayName("расстояние и кольца")
    class Geometry {

        @Test
        void distanceIsSymmetricAndCountsSteps() {
            Hex origin = new Hex(0, 0);
            Hex target = new Hex(3, 4);

            assertEquals(0, HexUtils.getDistance(origin, origin));
            assertEquals(7, HexUtils.getDistance(origin, target));
            assertEquals(7, HexUtils.getDistance(target, origin));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 8})
        void ringHasSixTimesRadiusDistinctHexesAtExactDistance(int radius) {
            List<Hex> ring = HexUtils.getRing(radius);

            assertEquals(6 * radius, ring.size());
            Set<String> unique = new HashSet<>();
            for (Hex hex : ring) {
                assertTrue(unique.add(hex.toString()), "duplicate hex " + hex);
                assertEquals(radius, HexUtils.getDistance(new Hex(0, 0), hex));
            }
        }

        @Test
        void nonPositiveRingIsEmpty() {
            assertTrue(HexUtils.getRing(0).isEmpty());
            assertTrue(HexUtils.getRing(-3).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 5})
        void generateHexesContainsEveryCellInsideRadius(int radius) {
            assertEquals(1 + 3 * radius * (radius + 1), HexUtils.generateHexes(radius).size());
            assertTrue(HexUtils.generateHexes(radius).containsKey("0:0"));
        }

        @Test
        void negativeRadiusIsClampedToZero() {
            assertEquals(1, HexUtils.generateHexes(-1).size());
        }
    }

    @Nested
    @DisplayName("округление и пиксели")
    class Rounding {

        @Test
        void roundedCubicCoordinatesAlwaysSumToZero() {
            double[][] samples = {{0.1, 0.2}, {1.4, -0.3}, {-2.7, 1.9}, {0.5, 0.5}};
            for (double[] sample : samples) {
                double q = sample[0];
                double r = sample[1];
                CubicHex cubic = HexUtils.getRoundedCubicHex(q, r, -q - r);
                assertEquals(0, cubic.x + cubic.y + cubic.z);
            }
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 10, 100})
        void pixelConversionRoundTrips(int size) {
            Hex[] hexes = {new Hex(0, 0), new Hex(1, 0), new Hex(-2, 3), new Hex(5, -5)};
            for (Hex hex : hexes) {
                assertEquals(hex, hex.toPixel(size).toHex(size));
            }
        }

        @Test
        void zeroSizedPixelMapsToOrigin() {
            assertEquals(new Hex(0, 0), new HexUtils.Pixel(12.0, -8.0).toHex(0));
        }
    }

    @Nested
    @DisplayName("Размещение по кольцу")
    class Distribution {

        @Test
        void seededRandomDefinesTheFirstCell() {
            int radius = 3;
            int seed = 42;
            List<Hex> ring = HexUtils.getRing(radius);
            Hex expectedFirst = ring.get(new Random(seed).nextInt(ring.size()));

            List<Hex> placed = HexUtils.distributeRingRandomly(radius, 6, new Random(seed));

            assertEquals(6, placed.size());
            assertEquals(expectedFirst, placed.get(0));
            assertTrue(placed.stream().allMatch(hex -> HexUtils.getDistance(new Hex(0, 0), hex) == radius));
            assertEquals(6, new HashSet<>(placed).size());
        }

        @Test
        void neverReturnsMoreCellsThanTheRingContains() {
            List<Hex> placed = HexUtils.distributeRingRandomly(1, 20, new Random(1));

            assertEquals(6, placed.size());
            assertEquals(6, new HashSet<>(placed).size());
        }

        @Test
        void invalidInputYieldsEmptyList() {
            assertTrue(HexUtils.distributeRingRandomly(3, 0, new Random(1)).isEmpty());
            assertTrue(HexUtils.distributeRingRandomly(3, -1, new Random(1)).isEmpty());
            assertTrue(HexUtils.distributeRingRandomly(3, 4, null).isEmpty());
            assertTrue(HexUtils.distributeRingRandomly(0, 4, new Random(1)).isEmpty());
        }
    }

    @Test
    void hexStringRoundTripsAndRejectsMalformedInput() {
        assertEquals(new Hex(-2, 3), Hex.fromString("-2:3"));
        assertNull(Hex.fromString(null));
        assertNull(Hex.fromString("1"));
        assertNull(Hex.fromString("a:b"));
        assertNull(Hex.fromString("1:2:3"));
    }

    @Test
    void hexValueSemanticsAndInvalidNeighbourDirection() {
        Hex hex = new Hex(2, -1);
        assertTrue(hex.equals(new Hex(2, -1)));
        assertFalse(hex.equals(new Hex(2, 1)));
        assertEquals(hex, hex.getNeighbour(-1));
        assertEquals(hex, hex.getNeighbour(6));
        assertEquals(new Hex(3, -1), hex.getNeighbour(0));
        assertEquals(hex, hex.toCubicHex().toHex());
    }

    @Test
    void cubicValueSemantics() {
        assertNotNull(new CubicHex(1, 2, -3));
        assertEquals(new CubicHex(1, 2, -3), new CubicHex(1, 2, -3));
        assertFalse(new CubicHex(1, 2, -3).equals(new CubicHex(1, 2, 3)));
    }
}
