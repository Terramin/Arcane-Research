package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.research.ResearchGraphTraversal;
import arcaneresearch.common.lib.research.ResearchGraphTraversal.Node;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Граф исследований: данные из research/*.json")
class ResearchGraphResourcesTest {

    private static final String RESEARCH = "assets/arcaneresearch/research";

    @Test
    void realResearchGraphHasNoParentCycles() {
        Map<String, Node> graph = graph();
        Set<String> visiting = new HashSet<>();
        Set<String> done = new HashSet<>();
        List<String> cycles = new ArrayList<>();

        for (String key : graph.keySet()) {
            detectCycle(key, graph, visiting, done, cycles);
        }
        assertTrue(cycles.isEmpty(), "cyclic parent references: " + cycles);
    }

    @Test
    void recursiveExpansionTerminatesForEveryEntry() {
        Map<String, Node> graph = graph();
        int max = graph.size() + 32; // room for virtual flag keys

        for (String key : graph.keySet()) {
            List<String> order = ResearchGraphTraversal.plan(key, graph::get);
            assertTrue(order.size() <= max, "expansion does not terminate for " + key);
            assertTrue(order.contains(key), "expansion of " + key + " does not include itself");
        }
    }

    @Test
    void ancestorsArePresentInTheExpansionOfAChild() {
        Map<String, Node> graph = graph();
        Node child = graph.get("ARCANEBORE");
        assertTrue(child != null);

        List<String> order = ResearchGraphTraversal.plan("ARCANEBORE", graph::get);
        for (String parent : child.parents()) {
            String key = parent.startsWith("~") ? parent.substring(1) : parent.split("@")[0];
            if (graph.containsKey(key)) {
                assertTrue(order.indexOf(key) < order.indexOf("ARCANEBORE"),
                        "parent " + key + " must be granted before the entry");
            }
        }
    }

    private static Map<String, Node> graph() {
        Map<String, Node> result = new HashMap<>();
        for (Path file : ResourceFiles.jsonFilesIn(RESEARCH)) {
            JsonObject root = ResourceFiles.readJson(file).getAsJsonObject();
            JsonElement entries = root.get("entries");
            if (entries == null || !entries.isJsonArray()) {
                continue;
            }
            for (JsonElement element : entries.getAsJsonArray()) {
                JsonObject entry = element.getAsJsonObject();
                String key = entry.get("key").getAsString();
                List<String> required = new ArrayList<>();
                for (String pageField : List.of("stages", "addenda")) {
                    JsonElement pages = entry.get(pageField);
                    if (pages == null || !pages.isJsonArray()) {
                        continue;
                    }
                    for (JsonElement pageElement : pages.getAsJsonArray()) {
                        JsonElement refs = pageElement.getAsJsonObject().get("required_research");
                        if (refs != null && refs.isJsonArray()) {
                            for (JsonElement ref : refs.getAsJsonArray()) {
                                required.add(ref.getAsString().split(";")[0]);
                            }
                        }
                    }
                }
                result.put(key, new Node(
                        strings(entry, "parents"),
                        strings(entry, "siblings"),
                        required));
            }
        }
        return result;
    }

    private static List<String> strings(JsonObject object, String field) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonElement element : value.getAsJsonArray()) {
            result.add(element.getAsString());
        }
        return result;
    }

    private static void detectCycle(String key, Map<String, Node> graph,
                                    Set<String> visiting, Set<String> done, List<String> cycles) {
        if (done.contains(key) || !visiting.add(key)) {
            return;
        }
        Node node = graph.get(key);
        if (node != null) {
            for (String parent : node.parents()) {
                String parentKey = parent.startsWith("~") ? parent.substring(1) : parent.split("@")[0];
                if (visiting.contains(parentKey) && !done.contains(parentKey)) {
                    cycles.add(parentKey + " -> " + key);
                }
                detectCycle(parentKey, graph, visiting, done, cycles);
            }
        }
        visiting.remove(key);
        done.add(key);
    }
}
