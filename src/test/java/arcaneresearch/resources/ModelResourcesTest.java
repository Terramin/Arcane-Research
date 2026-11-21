package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ресурсы: модели и blockstates")
class ModelResourcesTest {

    private static final String MODELS = "assets/arcaneresearch/models/";
    private static final String TEXTURES = "assets/arcaneresearch/textures/";
    private static final String BLOCKSTATES = "assets/arcaneresearch/blockstates/";

    /** Legacy files whose model reference is intentionally not a JSON file in this tree. */
    private static final Set<String> LEGACY_UNRESOLVED_MODELS = Set.of(
            "banner_lightblue.json -> arcaneresearch:banner",
            "banner_silver.json -> arcaneresearch:banner",
            "nitor_lightblue.json -> arcaneresearch:empty",
            "nitor_silver.json -> arcaneresearch:empty",
            "slab_double_ancient.json -> arcaneresearch:ancient_stone",
            "slab_double_arcane_stone.json -> arcaneresearch:arcane_stone",
            "slab_double_eldritch.json -> arcaneresearch:eldritch_stone",
            "stone_eldritch_tile.json -> arcaneresearch:eldritch_stone",
            "thaumometer.json -> arcaneresearch:scanner.obj",
            "wand_workbench.json -> arcaneresearch:wand_workbench");

    @Test
    void everyParentModelResolves() {
        for (Path file : ResourceFiles.jsonFilesIn(MODELS)) {
            JsonObject model = ResourceFiles.readJson(file).getAsJsonObject();
            JsonElement parent = model.get("parent");
            if (parent == null || !parent.isJsonPrimitive()) {
                continue;
            }
            String path = arcaneresearchPath(parent.getAsString());
            if (path != null) {
                assertTrue(ResourceFiles.exists(MODELS + path + ".json"),
                        ResourceFiles.relative(file) + " has missing parent " + parent.getAsString());
            }
        }
    }

    @Test
    void modelParentsNeverFormACycle() {
        for (Path file : ResourceFiles.jsonFilesIn(MODELS)) {
            Set<String> visited = new HashSet<>();
            Path current = file;
            while (current != null) {
                String relative = ResourceFiles.relative(current);
                assertTrue(visited.add(relative),
                        "parent cycle detected from " + ResourceFiles.relative(file) + " at " + relative);
                JsonElement parent = ResourceFiles.readJson(current).getAsJsonObject().get("parent");
                if (parent == null || !parent.isJsonPrimitive()) {
                    break;
                }
                String path = arcaneresearchPath(parent.getAsString());
                if (path == null) {
                    break;
                }
                Path next = ResourceFiles.file(MODELS + path + ".json");
                current = java.nio.file.Files.isRegularFile(next) ? next : null;
            }
        }
    }

    @Test
    void everyReferencedTextureExists() {
        for (Path file : ResourceFiles.jsonFilesIn(MODELS)) {
            JsonElement textures = ResourceFiles.readJson(file).getAsJsonObject().get("textures");
            if (textures == null || !textures.isJsonObject()) {
                continue;
            }
            for (var entry : textures.getAsJsonObject().entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive()) {
                    continue;
                }
                String reference = value.getAsString();
                String path = arcaneresearchPath(reference);
                if (path != null) {
                    assertTrue(ResourceFiles.exists(TEXTURES + path + ".png"),
                            ResourceFiles.relative(file) + " references missing texture " + reference);
                }
            }
        }
    }

    @Test
    void blockstateModelReferencesAreResolvedOrLegacy() {
        List<String> unresolved = new ArrayList<>();
        int fileCount = 0;

        for (Path file : ResourceFiles.jsonFilesIn(BLOCKSTATES)) {
            fileCount++;
            collectModelReferences(ResourceFiles.readJson(file), file.getFileName().toString(), unresolved);
        }
        assertTrue(fileCount > 100, "no blockstates found under " + BLOCKSTATES);

        for (String entry : unresolved) {
            assertTrue(LEGACY_UNRESOLVED_MODELS.contains(entry),
                    "new unresolved blockstate model: " + entry);
        }
        assertFalse(unresolved.isEmpty(), "expected the known legacy blockstate exceptions to remain");
    }

    private static void collectModelReferences(JsonElement element, String fileName, List<String> unresolved) {
        if (element.isJsonArray()) {
            for (JsonElement item : element.getAsJsonArray()) {
                collectModelReferences(item, fileName, unresolved);
            }
        } else if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                if (entry.getKey().equals("model") && entry.getValue().isJsonPrimitive()) {
                    String reference = entry.getValue().getAsString();
                    String path = arcaneresearchPath(reference);
                    if (path != null && !ResourceFiles.exists(MODELS + path + ".json")
                            && !ResourceFiles.exists(MODELS + path + ".obj")) {
                        unresolved.add(fileName + " -> " + reference);
                    }
                } else {
                    collectModelReferences(entry.getValue(), fileName, unresolved);
                }
            }
        }
    }

    /**
     * Returns the local path only for an explicit {@code arcaneresearch:} reference.
     * Bare ids (e.g. {@code block/rail_flat}) resolve to the {@code minecraft}
     * namespace in resource packs and are validated by the game, not here.
     */
    private static String arcaneresearchPath(String reference) {
        if (reference.startsWith("#") || !reference.startsWith("arcaneresearch:")) {
            return null;
        }
        return reference.substring("arcaneresearch:".length());
    }
}
