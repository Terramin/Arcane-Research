package arcaneresearch.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Ресурсы тигля")
class CrucibleResourcesTest {

    private static final Path ROOT = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()));
    private static final Path RECIPES = ROOT.resolve("data/arcaneresearch/recipes/crucible");

    private static List<Path> crucibleRecipes() {
        try (var stream = Files.list(RECIPES)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        } catch (IOException e) {
            return fail("Cannot list crucible recipes", e);
        }
    }

    private static JsonObject read(Path file) {
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement element = com.google.gson.JsonParser.parseReader(reader);
            assertTrue(element.isJsonObject());
            return element.getAsJsonObject();
        } catch (IOException e) {
            return fail("Cannot read " + file, e);
        }
    }

    private static JsonElement catalyst(JsonObject json) {
        return json.has("catalyst") ? json.get("catalyst") : json.get("ingredient");
    }

    @Test
    @DisplayName("every crucible recipe has type, research, catalyst, aspects and result")
    void recipeShape() {
        List<Path> files = crucibleRecipes();
        assertTrue(files.size() > 20, "too few recipes: " + files.size());
        for (Path file : files) {
            JsonObject json = read(file);
            assertEquals("arcaneresearch:crucible", json.get("type").getAsString(), file.toString());
            assertTrue(json.has("research"), file.toString());
            assertTrue(json.has("catalyst") || json.has("ingredient"), file.toString());
            assertTrue(json.getAsJsonObject("aspects").entrySet().size() > 0, file.toString());
            assertTrue(json.getAsJsonObject("result").has("item"), file.toString());
        }
    }

    @Test
    @DisplayName("no two recipes share the same catalyst and result")
    void noDuplicateRecipes() {
        Set<String> seen = new HashSet<>();
        for (Path file : crucibleRecipes()) {
            JsonObject json = read(file);
            String key = catalyst(json).toString() + "->"
                    + json.getAsJsonObject("result").toString();
            assertTrue(seen.add(key), "duplicate recipe: " + file.getFileName() + " (" + key + ")");
        }
    }

    @Test
    @DisplayName("leather has exactly one recipe gated by HEDGEALCHEMY@1")
    void leatherRecipe() {
        long count = crucibleRecipes().stream()
                .filter(p -> p.getFileName().toString().contains("leather"))
                .count();
        assertEquals(1, count);
        JsonObject leather = read(RECIPES.resolve("leather.json"));
        assertEquals("HEDGEALCHEMY@1", leather.get("research").getAsString());
        assertEquals(3, leather.getAsJsonObject("aspects").get("aer").getAsInt());
        assertEquals(3, leather.getAsJsonObject("aspects").get("bestia").getAsInt());
    }

    @Test
    @DisplayName("sanity soap uses cognitio (MIND), not sensus")
    void sanitySoapAspects() {
        JsonObject soap = read(RECIPES.resolve("sanity_soap.json"));
        assertEquals(75, soap.getAsJsonObject("aspects").get("cognitio").getAsInt());
        assertFalse(soap.getAsJsonObject("aspects").has("sensus"));
        assertEquals(50, soap.getAsJsonObject("aspects").get("alienis").getAsInt());
        assertEquals(75, soap.getAsJsonObject("aspects").get("ordo").getAsInt());
        assertEquals(50, soap.getAsJsonObject("aspects").get("victus").getAsInt());
    }

    @Test
    @DisplayName("advanced seal recipes use compound research keys")
    void compoundResearchKeys() {
        long compound = crucibleRecipes().stream()
                .map(CrucibleResourcesTest::read)
                .filter(json -> json.get("research").getAsString().contains("&&"))
                .count();
        assertTrue(compound > 0, "expected compound research gates for advanced seals");
    }

    @Test
    @DisplayName("every research gate points to an existing research entry")
    void researchGatesExist() throws IOException {
        Path researchDir = ROOT.resolve("assets/arcaneresearch/research");
        StringBuilder allResearch = new StringBuilder();
        try (var stream = Files.list(researchDir)) {
            for (Path file : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                allResearch.append(Files.readString(file, StandardCharsets.UTF_8));
            }
        }
        String researchText = allResearch.toString();

        for (Path file : crucibleRecipes()) {
            String gate = read(file).get("research").getAsString();
            for (String part : gate.split("&&|\\|\\|")) {
                String key = part.split("@")[0];
                assertTrue(researchText.contains("\"key\": \"" + key + "\""),
                        file.getFileName() + " -> " + key);
            }
        }
    }

    @Test
    @DisplayName("all mod result items have item models")
    void resultModelsExist() {
        for (Path file : crucibleRecipes()) {
            String item = read(file).getAsJsonObject("result").get("item").getAsString();
            if (item.startsWith("arcaneresearch:")) {
                String path = item.substring("arcaneresearch:".length());
                assertTrue(ResourceFiles.exists("assets/arcaneresearch/models/item/" + path + ".json"),
                        item + " from " + file.getFileName());
            }
        }
    }

    @Test
    @DisplayName("vis crystals have unique NBT and cover the aspect set")
    void visCrystals() {
        Set<String> nbt = new HashSet<>();
        int count = 0;
        for (Path file : crucibleRecipes()) {
            if (!file.getFileName().toString().startsWith("vis_crystal_")) continue;
            count++;
            JsonObject result = read(file).getAsJsonObject("result");
            String crystalNbt = result.has("nbt") ? result.get("nbt").getAsString() : "";
            assertTrue(crystalNbt.contains("Aspects"), file.toString());
            assertTrue(nbt.add(crystalNbt), "duplicate crystal NBT in " + file.getFileName());
        }
        assertEquals(37, count);
    }

    @Test
    @DisplayName("research pages only reference existing crucible recipes")
    void researchLinks() throws IOException {
        Path researchDir = ROOT.resolve("assets/arcaneresearch/research");
        Set<String> recipeIds = new HashSet<>();
        for (Path file : crucibleRecipes()) {
            recipeIds.add("arcaneresearch:crucible/"
                    + file.getFileName().toString().replace(".json", ""));
        }

        try (var stream = Files.list(researchDir)) {
            for (Path file : stream.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                int index = 0;
                while ((index = text.indexOf("arcaneresearch:crucible/", index)) >= 0) {
                    int end = text.indexOf('"', index);
                    if (end > index) {
                        String id = text.substring(index, end);
                        assertTrue(recipeIds.contains(id), file.getFileName() + " -> " + id);
                    }
                    index += "arcaneresearch:crucible/".length();
                }
            }
        }
    }
}
