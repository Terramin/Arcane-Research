package arcaneresearch.common.tiles.crafting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Alchemy furnace: recipe catalogue resolution")
class RecipeCatalogResolverTest {

    @Test
    @DisplayName("unknown hashes are kept and resolved later when the catalogue appears")
    void unresolvedHashesBecomeResolved() {
        List<Integer> hashes = new ArrayList<>(List.of(1, 2, 3));
        List<String> aspects = new ArrayList<>();
        List<String> owners = new ArrayList<>();
        Map<Integer, String> catalogue = new HashMap<>();

        assertFalse(RecipeCatalogResolver.resolve(hashes, aspects, owners,
                catalogue::get, "<empty>"));
        assertEquals(List.of("<empty>", "<empty>", "<empty>"), aspects);
        assertEquals(3, owners.size());
        assertEquals(3, hashes.size(), "resolving must never drop hashes");

        catalogue.put(1, "air");
        catalogue.put(3, "fire");
        assertFalse(RecipeCatalogResolver.resolve(hashes, aspects, owners,
                catalogue::get, "<empty>"));
        assertEquals(List.of("air", "<empty>", "fire"), aspects);

        catalogue.put(2, "water");
        assertTrue(RecipeCatalogResolver.resolve(hashes, aspects, owners,
                catalogue::get, "<empty>"));
        assertEquals(List.of("air", "water", "fire"), aspects);
    }

    @Test
    @DisplayName("short owner lists are extended without losing stored names")
    void shortOwnerList() {
        List<Integer> hashes = new ArrayList<>(List.of(10, 20, 30));
        List<String> aspects = new ArrayList<>();
        List<String> owners = new ArrayList<>(List.of("alice"));

        RecipeCatalogResolver.resolve(hashes, aspects, owners, h -> "aspect" + h, "?");

        assertEquals(List.of("alice", "", ""), owners);
        assertEquals(List.of("aspect10", "aspect20", "aspect30"), aspects);
    }

    @Test
    @DisplayName("adding a recipe extends the parallel lists in place")
    void addingRecipeKeepsListsAligned() {
        List<Integer> hashes = new ArrayList<>(List.of(1));
        List<String> aspects = new ArrayList<>();
        List<String> owners = new ArrayList<>();
        RecipeCatalogResolver.resolve(hashes, aspects, owners, h -> "a" + h, "?");

        hashes.add(2);
        RecipeCatalogResolver.resolve(hashes, aspects, owners, h -> "a" + h, "?");

        assertEquals(List.of("a1", "a2"), aspects);
        assertEquals(List.of("", ""), owners);
        assertEquals(hashes.size(), aspects.size());
        assertEquals(hashes.size(), owners.size());
    }

    @Test
    @DisplayName("hash comparison detects only real recipe changes")
    void sameHashes() {
        assertTrue(RecipeCatalogResolver.sameHashes(List.of(1, 2, 3), List.of(1, 2, 3)));
        assertFalse(RecipeCatalogResolver.sameHashes(List.of(1, 2), List.of(1, 2, 3)));
        assertFalse(RecipeCatalogResolver.sameHashes(List.of(1, 2, 4), List.of(1, 2, 3)));
        assertTrue(RecipeCatalogResolver.sameHashes(List.of(), List.of()));
    }

    @Test
    @DisplayName("empty catalogue yields a resolved state with no recipes")
    void emptyCatalogue() {
        List<Integer> hashes = new ArrayList<>();
        List<String> aspects = new ArrayList<>(List.of("stale"));
        List<String> owners = new ArrayList<>(List.of("stale"));

        boolean resolved = RecipeCatalogResolver.resolve(hashes, aspects, owners, h -> null, "?");

        assertTrue(resolved);
        assertTrue(aspects.isEmpty());
        assertTrue(owners.isEmpty());
    }

    @Test
    @DisplayName("duplicate hashes are resolved consistently")
    void duplicateHashes() {
        List<Integer> hashes = new ArrayList<>(Arrays.asList(7, 7, 7));
        List<String> aspects = new ArrayList<>();
        List<String> owners = new ArrayList<>();

        assertTrue(RecipeCatalogResolver.resolve(hashes, aspects, owners, h -> "seven", "?"));

        assertEquals(List.of("seven", "seven", "seven"), aspects);
    }
}
