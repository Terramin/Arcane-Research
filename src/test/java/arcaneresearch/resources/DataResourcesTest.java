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

@DisplayName("Ресурсы: рецепты, теги и research")
class DataResourcesTest {

    // 26.x server-data path is singular: data/<mod>/recipe/
    private static final String RECIPES = "data/arcaneresearch/recipe";
    private static final String TAGS = "data/arcaneresearch/tags";
    private static final String RESEARCH = "assets/arcaneresearch/research";

    /** Custom serializers that do not produce a single result item. */
    private static final Set<String> RESULTLESS_TYPES = Set.of(
            "arcaneresearch:salis_mundus",
            "arcaneresearch:label_fill",
            "arcaneresearch:triple_meat_treat",
            "arcaneresearch:infusion_enchantment");

    /** Blacklist tags are intentionally empty until configured by datapacks. */
    private static final Set<String> EMPTY_TAGS = Set.of(
            "data/arcaneresearch/tags/worldgen/biome/biome_blacklist.json",
            "data/arcaneresearch/tags/worldgen/biome/dimension_blacklist.json",
            "data/arcaneresearch/tags/worldgen/biome/is_tainted.json");

    @Test
    void everyRecipeDeclaresATypeAndStandardRecipesDeclareAResult() {
        List<Path> files = ResourceFiles.jsonFilesIn(RECIPES);
        List<String> problems = new ArrayList<>();

        for (Path file : files) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            String relative = ResourceFiles.relative(file);
            JsonElement type = recipe.get("type");
            if (type == null || !type.isJsonPrimitive()) {
                problems.add(relative + ": no type");
                continue;
            }
            if (!RESULTLESS_TYPES.contains(type.getAsString())) {
                JsonElement result = recipe.get("result");
                // 26.x writes a result id as {"id": "..."} (older data used "item").
                boolean hasItem = result != null
                        && (result.isJsonPrimitive()
                        || (result.isJsonObject() && (result.getAsJsonObject().has("item")
                        || result.getAsJsonObject().has("id"))));
                if (!hasItem) {
                    problems.add(relative + ": no result item");
                }
            }
        }

        assertTrue(files.size() > 400, "unexpectedly few recipes: " + files.size());
        assertTrue(problems.isEmpty(), "invalid recipes: " + problems);
    }

    @Test
    void everyTagHasAValuesArray() {
        List<String> problems = new ArrayList<>();
        for (Path file : ResourceFiles.jsonFilesIn(TAGS)) {
            JsonElement values = ResourceFiles.readJson(file).getAsJsonObject().get("values");
            if (values == null || !values.isJsonArray()) {
                problems.add(ResourceFiles.relative(file) + ": no values array");
            } else if (values.getAsJsonArray().size() == 0
                    && !EMPTY_TAGS.contains(ResourceFiles.relative(file))) {
                problems.add(ResourceFiles.relative(file) + ": unexpected empty tag");
            }
        }
        assertTrue(problems.isEmpty(), "invalid tags: " + problems);
    }

    @Test
    void researchKeysAreUniqueAndTranslateToEnglishTitles() {
        Set<String> english = ResourceFiles.readJson("assets/arcaneresearch/lang/en_us.json")
                .getAsJsonObject().entrySet().stream()
                .map(entry -> entry.getKey())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> seen = new HashSet<>();
        List<String> problems = new ArrayList<>();
        int count = 0;

        for (Path file : ResourceFiles.jsonFilesIn(RESEARCH)) {
            JsonObject root = ResourceFiles.readJson(file).getAsJsonObject();
            if (!root.has("entries")) {
                continue; // research_schema.json
            }
            for (JsonElement element : root.getAsJsonArray("entries")) {
                JsonObject entry = element.getAsJsonObject();
                count++;
                String key = entry.has("key") ? entry.get("key").getAsString() : null;
                if (key == null || key.isBlank()) {
                    problems.add(ResourceFiles.relative(file) + ": entry without key");
                    continue;
                }
                if (!seen.add(key)) {
                    problems.add("duplicate research key: " + key);
                }
                if (!entry.has("name") || !entry.has("location")) {
                    problems.add(key + ": missing name or location");
                }
                // '!'-prefixed keys are internal flags, not research pages.
                if (!key.startsWith("!") && !english.contains("research." + key + ".title")) {
                    problems.add(key + ": no English title");
                }
            }
        }

        assertTrue(count > 150, "unexpectedly few research entries: " + count);
        assertTrue(problems.isEmpty(), "invalid research entries: " + problems);
    }

    @Test
    void researchSchemaIsAValidObjectSchema() {
        JsonObject schema = ResourceFiles.readJson(RESEARCH + "/research_schema.json").getAsJsonObject();

        assertTrue(schema.has("$schema"));
        assertTrue(schema.has("definitions") || schema.has("properties"));
        assertFalse(schema.get("required").getAsJsonArray().isEmpty());
    }
}
