package arcaneresearch.common.world.maze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CellLoc: координаты ячеек лабиринта")
class CellLocTest {

    @Test
    void valueSemantics() {
        CellLoc first = new CellLoc(2, -3);
        CellLoc same = new CellLoc(2, -3);
        CellLoc other = new CellLoc(2, 3);

        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(other));
        assertFalse(first.equals(null));
        assertEquals(first, new CellLoc(first));
        assertEquals(new CellLoc(), new CellLoc((CellLoc) null));
    }

    @Test
    void comparisonSortsByZThenX() {
        List<CellLoc> cells = new ArrayList<>(List.of(
                new CellLoc(5, 1), new CellLoc(-1, 0), new CellLoc(2, 0), new CellLoc(0, -1)));

        Collections.sort(cells);

        assertEquals(List.of(new CellLoc(0, -1), new CellLoc(-1, 0),
                new CellLoc(2, 0), new CellLoc(5, 1)), cells);
    }

    @Test
    void distancesAreSquaredAndNullSafe() {
        CellLoc cell = new CellLoc(1, 2);

        assertEquals(25.0f, cell.getDistanceSquared(4, 6));
        assertEquals(25.0f, cell.getDistanceSquaredToChunkCoordinates(new CellLoc(4, 6)));
        assertEquals(Float.POSITIVE_INFINITY, cell.getDistanceSquaredToChunkCoordinates(null));
    }

    @Test
    void setMutatesCoordinates() {
        CellLoc cell = new CellLoc();
        cell.set(-4, 9);

        assertEquals(-4, cell.x);
        assertEquals(9, cell.z);
        assertTrue(cell.toString().contains("x=-4"));
    }
}
