package arcaneresearch.common.world.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Генерация лабиринта: упаковка ячейки")
class CellTest {

    @Test
    void emptyCellPacksToZero() {
        assertEquals(0, new Cell().pack());
        Cell unpacked = new Cell((short) 0);
        assertFalse(unpacked.north);
        assertFalse(unpacked.south);
        assertFalse(unpacked.east);
        assertFalse(unpacked.west);
        assertFalse(unpacked.above);
        assertFalse(unpacked.below);
        assertEquals(0, unpacked.feature);
    }

    @Test
    void passagesAndFeatureRoundTrip() {
        Cell cell = new Cell();
        cell.north = true;
        cell.east = true;
        cell.below = true;
        cell.feature = 21;

        short packed = cell.pack();
        Cell restored = new Cell(packed);

        assertTrue(restored.north);
        assertFalse(restored.south);
        assertTrue(restored.east);
        assertFalse(restored.west);
        assertFalse(restored.above);
        assertTrue(restored.below);
        assertEquals(21, restored.feature);
        assertEquals(packed, restored.pack());
    }

    @Test
    void eachExitUsesItsOwnBit() {
        for (int bit = 0; bit < 6; bit++) {
            Cell cell = new Cell((short) (1 << bit));
            boolean[] flags = {cell.north, cell.south, cell.east, cell.west, cell.above, cell.below};
            int set = 0;
            for (int i = 0; i < flags.length; i++) {
                if (flags[i]) {
                    set++;
                    assertTrue(i == bit, "wrong flag for bit " + bit);
                }
            }
            assertEquals(1, set);
        }
    }

    @Test
    void featureByteSurvivesSignExtension() {
        Cell cell = new Cell();
        cell.feature = (byte) 0xFF;
        short packed = cell.pack();

        assertEquals((byte) 0xFF, new Cell(packed).feature);
        assertEquals(packed & 0xFFFF, 0xFF00);
    }

    @Test
    void oppositeDirectionsAreSymmetric() {
        for (int direction : new int[]{MazeGenerator.N, MazeGenerator.S, MazeGenerator.E, MazeGenerator.W}) {
            assertEquals(direction == MazeGenerator.N ? MazeGenerator.S
                            : direction == MazeGenerator.S ? MazeGenerator.N
                            : direction == MazeGenerator.E ? MazeGenerator.W : MazeGenerator.E,
                    MazeGenerator.getOPP(direction));
        }
        assertEquals(-99, MazeGenerator.getOPP(MazeGenerator.A));
        assertEquals(0, MazeGenerator.getDX(MazeGenerator.N));
        assertEquals(-1, MazeGenerator.getDY(MazeGenerator.N));
        assertEquals(1, MazeGenerator.getDX(MazeGenerator.E));
        assertEquals(1, MazeGenerator.getDY(MazeGenerator.S));
        assertEquals(-99, MazeGenerator.getDX(16));
        assertEquals(-99, MazeGenerator.getDY(16));
    }
}
