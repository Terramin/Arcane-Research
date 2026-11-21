package arcaneresearch.discovered.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.discovered.table.HexUtils.CubicHex;
import arcaneresearch.discovered.table.HexUtils.Hex;
import arcaneresearch.discovered.table.HexUtils.Pixel;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("discovered HexUtils: геометрия доски исследовательского стола")
class HexUtilsTest {

    @Test
    void distanceIsSymmetricAndNullSafe() {
        Hex origin = new Hex(0, 0);
        Hex target = new Hex(2, -3);

        assertEquals(0, HexUtils.getDistance(origin, origin));
        assertEquals(3, HexUtils.getDistance(origin, target));
        assertEquals(3, HexUtils.getDistance(target, origin));
        assertEquals(Integer.MAX_VALUE, HexUtils.getDistance(null, origin));
        assertEquals(Integer.MAX_VALUE, HexUtils.getDistance(origin, null));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    void ringHasDistinctHexesAtExactDistance(int radius) {
        List<Hex> ring = HexUtils.getRing(radius);

        assertEquals(6 * radius, ring.size());
        assertEquals(6 * radius, new HashSet<>(ring).size());
        ring.forEach(hex -> assertEquals(radius, HexUtils.getDistance(new Hex(0, 0), hex)));
    }

    @Test
    void nonPositiveRingAndNegativeAreaAreSafe() {
        assertTrue(HexUtils.getRing(0).isEmpty());
        assertTrue(HexUtils.getRing(-2).isEmpty());
        assertEquals(1, HexUtils.generateHexes(-5).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    void generatedAreaHasTheExpectedSize(int radius) {
        assertEquals(1 + 3 * radius * (radius + 1), HexUtils.generateHexes(radius).size());
    }

    @Test
    void roundingPreservesCubicInvariant() {
        CubicHex rounded = HexUtils.getRoundedCubicHex(1.4, -0.7, -0.7);

        assertEquals(0, rounded.x + rounded.y + rounded.z);
        assertEquals(new Hex(rounded.x, rounded.z), rounded.toHex());
    }

    @Test
    void pixelConversionRoundTrips() {
        Hex[] cells = {new Hex(0, 0), new Hex(1, 0), new Hex(-3, 2), new Hex(4, -7)};
        for (Hex cell : cells) {
            assertEquals(cell, cell.toPixel(16).toHex(16));
        }
        assertEquals(new Hex(0, 0), new Pixel(50, 50).toHex(0));
    }

    @Test
    void seededRandomRotatesTheRingInsteadOfAlwaysStartingAtTheSameCell() {
        int radius = 3;
        int seed = 7;
        List<Hex> ring = HexUtils.getRing(radius);
        Hex expectedFirst = ring.get(new Random(seed).nextInt(ring.size()));

        List<Hex> placed = HexUtils.distributeRingRandomly(radius, 6, new Random(seed));

        assertEquals(6, placed.size());
        assertEquals(expectedFirst, placed.get(0));
        assertEquals(6, new HashSet<>(placed).size());
        placed.forEach(hex -> assertEquals(radius, HexUtils.getDistance(new Hex(0, 0), hex)));
    }

    @Test
    void entriesAreCappedAtRingSizeAndInputIsValidated() {
        List<Hex> placed = HexUtils.distributeRingRandomly(1, 12, new Random(3));

        assertEquals(6, placed.size());
        assertEquals(6, new HashSet<>(placed).size());
        assertTrue(HexUtils.distributeRingRandomly(3, 0, new Random(3)).isEmpty());
        assertTrue(HexUtils.distributeRingRandomly(3, 4, null).isEmpty());
    }

    @Test
    void hexValueSemanticsAndNeighbours() {
        Hex hex = new Hex(2, 1);

        assertEquals(new Hex(3, 1), hex.getNeighbour(0));
        assertEquals(new Hex(2, 0), hex.getNeighbour(2));
        assertTrue(hex.equals(new Hex(2, 1)));
        assertFalse(hex.equals(null));
        assertEquals(new Hex(2, 1), hex.toCubicHex().toHex());
        assertEquals("2:1", hex.toString());
    }
}
