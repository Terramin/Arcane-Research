package arcaneresearch.common.world.maze;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Генерация лабиринта: детерминизм и инварианты сетки")
class MazeGeneratorTest {

    private static final int SIZE = 11;

    @ParameterizedTest
    @ValueSource(longs = {1L, 7L, 42L, 12345L})
    void generationIsDeterministicForTheSameSeed(long seed) {
        int[][] first = generated(SIZE, SIZE, seed).grid;
        int[][] second = generated(SIZE, SIZE, seed).grid;

        assertArrayEquals(first, second);
    }

    @Test
    void differentSeedsProduceDifferentLayouts() {
        assertFalse(Arrays.deepEquals(
                generated(SIZE, SIZE, 1L).grid,
                generated(SIZE, SIZE, 2L).grid));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void tooSmallMazesAreRejected(int size) {
        MazeGenerator generator = new MazeGenerator(size, size, 1L);

        assertFalse(generator.generate());
    }

    @Test
    void negativeDimensionsAreRejectedEarly() {
        assertThrows(IllegalArgumentException.class, () -> new MazeGenerator(-5, 10, 1L));
        assertThrows(IllegalArgumentException.class, () -> new MazeGenerator(10, -5, 1L));
    }

    @Test
    void gridHasTheRequestedDimensionsAndRequiredMarkers() {
        MazeGenerator generator = generated(SIZE, SIZE, 42L);

        assertEqualsDimensions(SIZE, SIZE, generator.grid);
        int startX = 1 + SIZE / 2;
        int startY = 1 + SIZE / 2;
        assertTrue((generator.grid[startY][startX] & 0x100) != 0, "missing start marker");

        int markers = 0;
        for (int[] row : generator.grid) {
            for (int cell : row) {
                if ((cell & 0x200) != 0 || (cell & 0x400) != 0
                        || (cell & 0x800) != 0 || (cell & 0x1000) != 0) {
                    markers++;
                }
            }
        }
        assertTrue(markers >= 4, "missing destination markers: " + markers);
    }

    @Test
    void carvedPassagesAreSymmetricAndTemporaryMarkersAreCleared() {
        int[][] grid = generated(SIZE, SIZE, 7L).grid;

        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {
                int cell = grid[y][x];
                assertTrue(cell >= 0, "uncleared negative cell at " + x + "," + y);
                assertNotEquals(99, feature(cell), "temporary feature left in grid");
                if ((cell & MazeGenerator.E) != 0) {
                    assertTrue((grid[y][x + 1] & MazeGenerator.W) != 0, "east passage at " + x + "," + y);
                }
                if ((cell & MazeGenerator.W) != 0) {
                    assertTrue((grid[y][x - 1] & MazeGenerator.E) != 0, "west passage at " + x + "," + y);
                }
                if ((cell & MazeGenerator.S) != 0) {
                    assertTrue((grid[y + 1][x] & MazeGenerator.N) != 0, "south passage at " + x + "," + y);
                }
                if ((cell & MazeGenerator.N) != 0) {
                    assertTrue((grid[y - 1][x] & MazeGenerator.S) != 0, "north passage at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void generatorPlacesSpecialFeatures() {
        int[][] grid = generated(SIZE, SIZE, 12345L).grid;
        boolean treasury = false;
        boolean decoration = false;

        for (int[] row : grid) {
            for (int cell : row) {
                treasury |= feature(cell) == 6;
                decoration |= feature(cell) >= 7;
            }
        }
        assertTrue(treasury, "no treasury cell");
        assertTrue(decoration, "no decorative feature");
    }

    @Test
    void repeatedGenerationOnTheSameInstanceContinuesItsRandomStream() {
        MazeGenerator generator = new MazeGenerator(SIZE, SIZE, 5L);
        while (!generator.generate()) {
            generator = new MazeGenerator(SIZE, SIZE, 6L);
        }
        int[][] first = Arrays.stream(generator.grid).map(int[]::clone).toArray(int[][]::new);

        // A second call starts from the existing grid rather than resetting it.
        generator.generate();
        assertFalse(Arrays.deepEquals(first, generator.grid));
    }

    private static MazeGenerator generated(int width, int height, long seed) {
        for (int attempt = 0; attempt < 100; attempt++) {
            MazeGenerator generator = new MazeGenerator(width, height, seed + attempt);
            if (generator.generate()) {
                return generator;
            }
        }
        return fail("generation never succeeded for seed " + seed);
    }

    private static int feature(int cell) {
        return ((short) cell >> 8) & 0xFF;
    }

    private static void assertEqualsDimensions(int width, int height, int[][] grid) {
        org.junit.jupiter.api.Assertions.assertEquals(height, grid.length);
        for (int[] row : grid) {
            org.junit.jupiter.api.Assertions.assertEquals(width, row.length);
        }
    }
}
