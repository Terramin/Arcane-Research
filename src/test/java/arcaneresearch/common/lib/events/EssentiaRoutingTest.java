package arcaneresearch.common.lib.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.aura.VisRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Эссенция и вис: чистые правила поиска")
class EssentiaRoutingTest {

    @Test
    void directionalScanCoversThePlaneWithoutTheOrigin() {
        List<int[]> offsets = EssentiaRouting.offsets('Y', 0, 1, 0, 1, false);

        assertEquals(3 * 3 - 1, offsets.size());
        assertTrue(offsets.stream().allMatch(offset -> offset[1] == 0));
        assertFalse(offsets.stream().anyMatch(offset -> offset[0] == 0 && offset[1] == 0 && offset[2] == 0));
    }

    @Test
    void wholeAreaScanIncludesBothSidesOfThePlane() {
        List<int[]> offsets = EssentiaRouting.offsets('Y', 0, 1, 0, 2, true);

        assertEquals(5 * 5 * 4 - 1, offsets.size());
        assertTrue(offsets.stream().allMatch(offset -> offset[1] >= -2 && offset[1] <= 1));
    }

    @Test
    void axisSelectionUsesTheDirectionStep() {
        List<int[]> east = EssentiaRouting.offsets('X', 1, 0, 0, 1, false);
        List<int[]> north = EssentiaRouting.offsets('Z', 0, 0, -1, 1, false);

        assertTrue(east.stream().allMatch(offset -> offset[0] == 0));
        assertTrue(north.stream().allMatch(offset -> offset[2] == 0));
        assertTrue(east.stream().anyMatch(offset -> offset[1] == 1 && offset[2] == -1));
    }

    @Test
    void candidatesAreSortedBySquaredDistance() {
        record Candidate(String name, int x, int y, int z) {
        }
        List<Candidate> candidates = new ArrayList<>(List.of(
                new Candidate("far", 10, 0, 0),
                new Candidate("near", 1, 1, 1),
                new Candidate("mid", 0, 3, 0)));

        EssentiaRouting.sortNearestFirst(candidates,
                candidate -> new int[]{candidate.x(), candidate.y(), candidate.z()}, 0, 0, 0);

        assertEquals(List.of("near", "mid", "far"),
                candidates.stream().map(Candidate::name).toList());
    }

    @Test
    void visRangeRejectsOtherDimensionsAndOutOfRangeNodes() {
        String overworld = "minecraft:overworld";
        String nether = "minecraft:the_nether";

        assertEquals(9.0f, VisRange.inRange(overworld, overworld, 9.0, 3));
        assertEquals(16.0f, VisRange.inRange(overworld, overworld, 16.0, 4));
        assertEquals(-1.0f, VisRange.inRange(overworld, overworld, 17.0, 4));
        assertEquals(-1.0f, VisRange.inRange(overworld, nether, 1.0, 10));
        assertEquals(-1.0f, VisRange.inRange(null, overworld, 1.0, 10));
    }
}
