package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Генерация мира: связи между worldgen-ресурсами")
class WorldgenResourcesTest {

    private static final String WORLDGEN = "data/arcaneresearch/worldgen";

    @Test
    void placedFeaturesReferenceExistingConfiguredFeatures() {
        List<Path> files = ResourceFiles.jsonFilesIn(WORLDGEN + "/placed_feature");
        List<String> problems = new ArrayList<>();

        for (Path file : files) {
            String feature = string(ResourceFiles.readJson(file).getAsJsonObject(), "feature");
            checkConfigured(feature, file.getFileName().toString(), problems);
        }
        assertEquals(38, files.size());
        assertTrue(problems.isEmpty(), "broken placed features: " + problems);
    }

    @Test
    void structuresReferenceExistingConfiguredFeatures() {
        for (Path file : ResourceFiles.jsonFilesIn(WORLDGEN + "/structure")) {
            JsonObject structure = ResourceFiles.readJson(file).getAsJsonObject();
            assertTrue(structure.has("type"), file.getFileName() + " has no type");
            List<String> problems = new ArrayList<>();
            checkConfigured(string(structure, "feature"), file.getFileName().toString(), problems);
            assertTrue(problems.isEmpty(), "broken structures: " + problems);
        }
    }

    @Test
    void structureSetsReferenceExistingStructures() {
        List<String> problems = new ArrayList<>();
        for (Path file : ResourceFiles.jsonFilesIn(WORLDGEN + "/structure_set")) {
            JsonObject set = ResourceFiles.readJson(file).getAsJsonObject();
            JsonElement placement = set.get("placement");
            if (placement == null || !placement.isJsonObject() || !placement.getAsJsonObject().has("type")) {
                problems.add(file.getFileName() + ": no placement type");
            }
            JsonArray structures = set.getAsJsonArray("structures");
            if (structures == null || structures.isEmpty()) {
                problems.add(file.getFileName() + ": no structures");
                continue;
            }
            for (JsonElement element : structures) {
                String name = string(element.getAsJsonObject(), "structure");
                if (name != null && name.startsWith("arcaneresearch:")
                        && !ResourceFiles.exists(WORLDGEN + "/structure/"
                        + name.substring(name.indexOf(':') + 1) + ".json")) {
                    problems.add(file.getFileName() + " -> " + name);
                }
            }
        }
        assertTrue(problems.isEmpty(), "broken structure sets: " + problems);
    }

    @Test
    void templatePoolsReferenceExistingStructureFiles() {
        List<String> problems = new ArrayList<>();
        for (Path file : ResourceFiles.jsonFilesIn(WORLDGEN + "/template_pool")) {
            JsonObject pool = ResourceFiles.readJson(file).getAsJsonObject();
            assertTrue(pool.has("fallback"), file.getFileName() + " has no fallback");
            JsonArray elements = pool.getAsJsonArray("elements");
            if (elements == null || elements.isEmpty()) {
                problems.add(file.getFileName() + ": no elements");
                continue;
            }
            for (JsonElement element : elements) {
                JsonObject holder = element.getAsJsonObject().getAsJsonObject("element");
                String location = string(holder, "location");
                if (location != null && location.startsWith("arcaneresearch:")
                        && !ResourceFiles.exists("data/arcaneresearch/structures/"
                        + location.substring(location.indexOf(':') + 1) + ".nbt")) {
                    problems.add(file.getFileName() + " -> " + location);
                }
            }
        }
        assertTrue(problems.isEmpty(), "broken template pools: " + problems);
    }

    @Test
    void everyConfiguredFeatureHasAType() {
        List<Path> files = ResourceFiles.jsonFilesIn(WORLDGEN + "/configured_feature");
        assertEquals(24, files.size());
        for (Path file : files) {
            assertTrue(ResourceFiles.readJson(file).getAsJsonObject().has("type"),
                    file.getFileName() + " has no type");
        }
    }

    private static void checkConfigured(String feature, String owner, List<String> problems) {
        if (feature != null && feature.startsWith("arcaneresearch:")
                && !ResourceFiles.exists(WORLDGEN + "/configured_feature/"
                + feature.substring(feature.indexOf(':') + 1) + ".json")) {
            problems.add(owner + " -> " + feature);
        }
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
