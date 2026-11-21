package arcaneresearch.discovered.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.table.HexResearchGame.PlaceResult;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Игра гексов: состояние заметки и размещение аспектов")
class HexResearchGameTest {

    @Test
    void createNoteIsDeterministicForASeed() {
        HexResearchNoteData first = HexResearchGame.createNote("KEY", List.of(), 0x112233, new Random(42));
        HexResearchNoteData second = HexResearchGame.createNote("KEY", List.of(), 0x112233, new Random(42));

        assertEquals(first.hexes.keySet(), second.hexes.keySet());
        assertEquals(first.hexEntries.keySet(), second.hexEntries.keySet());
        assertEquals("KEY", first.key);
        assertEquals(0x112233, first.color);
        assertFalse(first.complete);
        assertEquals(0, first.copies);
        assertFalse(first.hexes.isEmpty());
    }

    @Test
    void blankCellsAcceptAspectsAndEraseRestoresThem() {
        Aspect aer = Aspect.getAspect("aer");
        assertNotNull(aer);
        HexResearchNoteData data = HexResearchGame.createNote("KEY", List.of(), 0, new Random(7));
        String blank = data.hexEntries.entrySet().stream()
                .filter(entry -> entry.getValue().type == 0)
                .map(java.util.Map.Entry::getKey)
                .findFirst().orElseThrow();
        HexUtils.Hex cell = cell(blank);
        assertNotNull(cell);

        assertEquals(PlaceResult.OK, HexResearchGame.placeAspect(data, cell.q, cell.r, aer));
        HexEntry placed = data.hexEntries.get(blank);
        assertEquals(aer, placed.aspect);
        assertEquals(2, placed.type);

        assertEquals(aer, HexResearchGame.eraseAspect(data, cell.q, cell.r));
        assertEquals(0, data.hexEntries.get(blank).type);
        assertEquals(PlaceResult.NO_HEX, HexResearchGame.placeAspect(data, 999, 999, aer));
        assertEquals(PlaceResult.NO_HEX, HexResearchGame.placeAspect(data, cell.q, cell.r, null));
    }

    @Test
    void poolPlacementConsumesOneAspectOnlyOnSuccess() {
        Aspect aer = Aspect.getAspect("aer");
        assertNotNull(aer);
        HexResearchNoteData data = HexResearchGame.createNote("KEY", List.of(), 0, new Random(11));
        String blank = data.hexEntries.entrySet().stream()
                .filter(entry -> entry.getValue().type == 0)
                .map(java.util.Map.Entry::getKey)
                .findFirst().orElseThrow();
        HexUtils.Hex cell = cell(blank);
        AspectList empty = new AspectList();
        AspectList full = new AspectList();
        full.add(aer, 3);

        assertEquals(PlaceResult.NO_POOL,
                HexResearchGame.placeAspect(data, empty, cell.q, cell.r, aer));
        assertEquals(PlaceResult.OK,
                HexResearchGame.placeAspect(data, full, cell.q, cell.r, aer));
        assertEquals(2, full.getAmount(aer));
        assertEquals(PlaceResult.OCCUPIED,
                HexResearchGame.placeAspect(data, full, cell.q, cell.r, aer));
        assertEquals(2, full.getAmount(aer));
    }

    private static HexUtils.Hex cell(String key) {
        String[] parts = key.split(":");
        return new HexUtils.Hex(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    @Test
    void emptyPuzzleCompletesImmediatelyAndIdempotently() {
        HexResearchNoteData data = HexResearchGame.createNote("KEY", List.of(), 0, new Random(3));
        data.hexEntries.clear();
        data.hexes.clear();

        assertTrue(HexResearchGame.checkCompletion(data, null));
        assertTrue(data.complete);
        assertTrue(HexResearchGame.checkCompletion(data, null));
        assertEquals(0, data.hexes.size());
    }
}
