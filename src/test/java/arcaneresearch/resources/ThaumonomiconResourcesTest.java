package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Data-level tests for the Thaumonomicon: the book is driven by assets/.../research/*.json. */
@DisplayName("Книга таумономикон: данные исследований")
class ThaumonomiconResourcesTest {

    private static final String RESEARCH = "assets/arcaneresearch/research";
    private static final String RECIPES = "data/arcaneresearch/recipes";

    private static final Set<String> CATEGORIES = Set.of(
            "BASICS", "AUROMANCY", "ALCHEMY", "ARTIFICE", "INFUSION", "GOLEMANCY", "ELDRITCH", "LEGACY");
    private static final Set<String> META_FLAGS = Set.of(
            "ROUND", "SPIKY", "HIDDEN", "REVERSE", "AUTOUNLOCK", "HEX");
    private static final Set<String> KNOWLEDGE_TYPES = Set.of("THEORY", "OBSERVATION");
    private static final Set<String> VIRTUAL_PREFIXES = Set.of("!", "m_", "f_", "c_");

    /** Legacy recipe ids kept in book pages; the game maps them to current recipes. */
    private static final Set<String> LEGACY_RECIPE_REFS = Set.of(
            "arcaneresearch:eldritchportal",
            "arcaneresearch:golempress",
            "arcaneresearch:infernalfurnace",
            "arcaneresearch:infusionaltar",
            "arcaneresearch:infusionaltarancient",
            "arcaneresearch:infusionaltareldritch",
            "arcaneresearch:infusion/golem_module_vision",
            "arcaneresearch:nodejar",
            "arcaneresearch:thaumatorium");

    @Test
    void everyEntryHasRequiredFieldsAndAKnownCategory() {
        List<String> problems = new ArrayList<>();

        for (JsonObject entry : entries()) {
            String key = string(entry, "key");
            if (key == null || key.isBlank()) {
                problems.add("entry without key");
                continue;
            }
            if (string(entry, "name") == null || string(entry, "category") == null) {
                problems.add(key + ": missing name or category");
            }
            if (!CATEGORIES.contains(string(entry, "category"))) {
                problems.add(key + ": unknown category " + string(entry, "category"));
            }
            JsonElement location = entry.get("location");
            if (location == null || !location.isJsonArray() || location.getAsJsonArray().size() != 2) {
                problems.add(key + ": invalid location");
            }
            for (JsonElement flag : array(entry, "meta")) {
                if (!META_FLAGS.contains(flag.getAsString())) {
                    problems.add(key + ": unknown meta flag " + flag.getAsString());
                }
            }
        }

        assertTrue(entries().size() > 150);
        assertTrue(problems.isEmpty(), "invalid research entries: " + problems);
    }

    @Test
    void iconsDoNotOverlapInsideACategory() {
        List<String> problems = new ArrayList<>();
        Set<String> occupied = new HashSet<>();

        for (JsonObject entry : entries()) {
            JsonElement location = entry.get("location");
            if (location == null || !location.isJsonArray() || location.getAsJsonArray().size() != 2) {
                continue;
            }
            String cell = string(entry, "category") + ":" + location.getAsJsonArray().get(0)
                    + ":" + location.getAsJsonArray().get(1);
            if (!occupied.add(cell)) {
                problems.add("two research icons at " + cell);
            }
        }

        assertTrue(problems.isEmpty(), "overlapping research icons: " + problems);
    }

    @Test
    void everyStageAndAddendumTextIsLocalized() {
        Set<String> english = englishKeys();
        List<String> problems = new ArrayList<>();
        int pages = 0;

        for (JsonObject entry : entries()) {
            for (JsonObject page : pages(entry)) {
                pages++;
                String text = string(page, "text");
                if (text == null || !english.contains(text)) {
                    problems.add(string(entry, "key") + ": missing text key " + text);
                }
            }
        }

        assertTrue(pages > 150, "unexpectedly few book pages: " + pages);
        assertTrue(problems.isEmpty(), "untranslated book pages: " + problems);
    }

    @Test
    void bareResearchReferencesPointToRealEntries() {
        Set<String> keys = researchKeys();
        List<String> problems = new ArrayList<>();

        for (JsonObject entry : entries()) {
            for (String field : List.of("parents", "siblings")) {
                for (JsonElement reference : array(entry, field)) {
                    checkReference(reference.getAsString(), keys, string(entry, "key"), field, problems);
                }
            }
            for (JsonObject page : pages(entry)) {
                for (JsonElement reference : array(page, "required_research")) {
                    checkReference(reference.getAsString(), keys, string(entry, "key"),
                            "required_research", problems);
                }
            }
        }

        assertTrue(problems.isEmpty(), "unresolved research references: " + problems);
    }

    @Test
    void bookRecipesExistOrAreKnownLegacyIds() {
        Set<String> groups = recipeGroups();
        List<String> problems = new ArrayList<>();

        for (JsonObject entry : entries()) {
            for (JsonObject page : pages(entry)) {
                for (JsonElement reference : array(page, "recipes")) {
                    String id = reference.getAsString();
                    String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    String namespace = id.contains(":") ? id.substring(0, id.indexOf(':')) : "minecraft";
                    if (!namespace.equals("arcaneresearch")) {
                        continue;
                    }
                    if (ResourceFiles.exists(RECIPES + "/" + path + ".json")
                            || groups.contains(id)
                            || LEGACY_RECIPE_REFS.contains(id)
                            || id.matches(".*_fake(_\\d+)?")) {
                        continue;
                    }
                    problems.add(string(entry, "key") + ": unknown recipe " + id);
                }
            }
        }

        assertTrue(problems.isEmpty(), "unresolved book recipes: " + problems);
    }

    @Test
    void knowledgeCostsAndRewardsUseTheDocumentedFormat() {
        List<String> problems = new ArrayList<>();

        for (JsonObject entry : entries()) {
            checkKnowledge(entry, "required_knowledge", problems);
            checkKnowledge(entry, "reward_knowledge", problems);
            for (JsonObject page : pages(entry)) {
                checkKnowledge(page, "required_knowledge", problems);
            }
        }

        assertTrue(problems.isEmpty(), "invalid knowledge costs: " + problems);
    }

    @Test
    void warpRewardsAreNeverNegative() {
        List<String> problems = new ArrayList<>();
        for (JsonObject entry : entries()) {
            for (JsonObject page : pages(entry)) {
                JsonElement warp = page.get("warp");
                if (warp != null && (!warp.isJsonPrimitive() || warp.getAsInt() < 0)) {
                    problems.add(string(entry, "key") + ": warp " + warp);
                }
            }
        }
        assertTrue(problems.isEmpty(), "negative warp: " + problems);
    }

    static Set<String> researchKeys() {
        Set<String> keys = new HashSet<>();
        for (JsonObject entry : entries()) {
            String key = string(entry, "key");
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    private static List<JsonObject> entries() {
        List<JsonObject> result = new ArrayList<>();
        for (Path file : ResourceFiles.jsonFilesIn(RESEARCH)) {
            JsonElement root = ResourceFiles.readJson(file);
            if (!root.isJsonObject() || !root.getAsJsonObject().has("entries")) {
                continue; // research_schema.json
            }
            for (JsonElement entry : root.getAsJsonObject().getAsJsonArray("entries")) {
                result.add(entry.getAsJsonObject());
            }
        }
        return result;
    }

    private static List<JsonObject> pages(JsonObject entry) {
        List<JsonObject> pages = new ArrayList<>();
        for (String field : List.of("stages", "addenda")) {
            for (JsonElement page : array(entry, field)) {
                if (page.isJsonObject()) {
                    pages.add(page.getAsJsonObject());
                }
            }
        }
        return pages;
    }

    private static Set<String> englishKeys() {
        Set<String> keys = new HashSet<>();
        ResourceFiles.readJson("assets/arcaneresearch/lang/en_us.json").getAsJsonObject()
                .entrySet().forEach(entry -> keys.add(entry.getKey()));
        return keys;
    }

    private static Set<String> recipeGroups() {
        Set<String> groups = new HashSet<>();
        for (Path file : ResourceFiles.jsonFilesIn(RECIPES)) {
            JsonElement group = ResourceFiles.readJson(file).getAsJsonObject().get("group");
            if (group != null && group.isJsonPrimitive()) {
                groups.add(group.getAsString());
            }
        }
        return groups;
    }

    private static void checkReference(String raw, Set<String> keys, String owner,
                                       String field, List<String> problems) {
        String ref = raw.startsWith("~") ? raw.substring(1) : raw;
        ref = ref.split(";")[0].split("@")[0];
        if (ref.isEmpty()) {
            return;
        }
        for (String prefix : VIRTUAL_PREFIXES) {
            if (ref.startsWith(prefix)) {
                return; // scan flag or virtual requirement, not a research entry
            }
        }
        if (!keys.contains(ref)) {
            problems.add(owner + ": " + field + " -> " + raw);
        }
    }

    private static void checkKnowledge(JsonObject object, String field, List<String> problems) {
        for (JsonElement element : array(object, field)) {
            String value = element.getAsString();
            String[] parts = value.split(";");
            boolean valid = parts.length == 3
                    && KNOWLEDGE_TYPES.contains(parts[0])
                    && CATEGORIES.contains(parts[1])
                    && parts[2].matches("[1-9][0-9]*");
            if (!valid) {
                problems.add(string(object, "key") + ": " + field + " " + value);
            }
        }
    }

    private static List<JsonElement> array(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonArray()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(value.getAsJsonArray().asList());
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
