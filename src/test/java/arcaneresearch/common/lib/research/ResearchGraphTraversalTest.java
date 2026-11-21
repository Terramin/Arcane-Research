package arcaneresearch.common.lib.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.research.ResearchGraphTraversal.Node;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Граф исследований: безопасный обход зависимостей")
class ResearchGraphTraversalTest {

    @Test
    void parentsAreGrantedBeforeTheEntryAndSiblingsAfter() {
        Map<String, Node> graph = Map.of(
                "CHILD", new Node(List.of("PARENT"), List.of("SIBLING"), List.of("REQUIRED")),
                "PARENT", new Node(List.of(), List.of(), List.of()),
                "SIBLING", new Node(List.of(), List.of(), List.of()));

        assertEquals(List.of("PARENT", "REQUIRED", "CHILD", "SIBLING"),
                ResearchGraphTraversal.plan("CHILD", graph::get));
    }

    @Test
    void stageSuffixesAndHiddenPrefixesAreNormalized() {
        Map<String, Node> graph = Map.of("A", new Node(List.of(), List.of(), List.of()));

        assertEquals(List.of("A"),
                ResearchGraphTraversal.plan("A@1", key -> key.equals("A") ? graph.get("A")
                        : new Node(List.of(), List.of(), List.of())));
        assertEquals(List.of("A"), ResearchGraphTraversal.plan("~A", graph::get));
        assertEquals(List.of("A"), ResearchGraphTraversal.plan(" A ", graph::get));
    }

    @Test
    void unknownFlagKeysAreStillGrantedOnce() {
        assertEquals(List.of("!FLAG"), ResearchGraphTraversal.plan("!FLAG", key -> null));
        assertEquals(List.of(), ResearchGraphTraversal.plan("", key -> null));
        assertEquals(List.of(), ResearchGraphTraversal.plan(null, key -> null));
    }

    @Test
    void cyclesAndDuplicatesTerminate() {
        Map<String, Node> cyclic = Map.of(
                "A", new Node(List.of("B"), List.of("C"), List.of("B")),
                "B", new Node(List.of("A"), List.of(), List.of()),
                "C", new Node(List.of("A"), List.of(), List.of()));

        List<String> order = ResearchGraphTraversal.plan("A", cyclic::get);

        assertEquals(3, order.size());
        assertEquals("B", order.get(0));
        assertEquals("A", order.get(1));
        assertTrue(order.contains("C"));
        assertFalse(order.contains("!MISSING"));
    }

    @Test
    void completedEntriesAreNotExpanded() {
        Map<String, Node> graph = Map.of(
                "A", new Node(List.of("B"), List.of("C"), List.of()),
                "B", new Node(List.of(), List.of(), List.of()),
                "C", new Node(List.of(), List.of(), List.of()));

        // A completed entry is neither granted nor expanded, like the old command.
        assertEquals(List.of(), ResearchGraphTraversal.plan("A", graph::get, key -> false));
        assertEquals(List.of("A", "C"),
                ResearchGraphTraversal.plan("A", graph::get, key -> !key.equals("B")));
    }

    @Test
    void requiredResearchFromStagesIsDeduplicated() {
        Node node = new Node(List.of(), List.of(), List.of("X@1", "X", "~X", "Y"));
        List<String> order = ResearchGraphTraversal.plan("ROOT", key -> key.equals("ROOT") ? node : null);

        assertEquals(List.of("X", "Y", "ROOT"), order);
    }
}
