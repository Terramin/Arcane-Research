package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Рецепты: поля по типам и регистрация сериализаторов")
class RecipeResourcesDetailTest {

    private static final String RECIPES = "data/arcaneresearch/recipes";
    private static final Set<String> WORKBENCH_TYPES = Set.of(
            "arcaneresearch:arcane_workbench_shaped",
            "arcaneresearch:arcane_workbench_shapeless",
            "arcaneresearch:arcane_workbench_void_jar",
            "arcaneresearch:arcane_workbench_essence_phial");

    @Test
    void everyCustomRecipeTypeHasASerializer() {
        String serializers = source("arcaneresearch/init/ModRecipeSerializers.java")
                + source("arcaneresearch/init/ModRecipeTypes.java");
        List<String> problems = new ArrayList<>();

        for (Path file : ResourceFiles.jsonFilesIn(RECIPES)) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            JsonElement type = recipe.get("type");
            if (type == null || !type.getAsString().startsWith("arcaneresearch:")) {
                continue;
            }
            String id = type.getAsString().substring("arcaneresearch:".length());
            if (!serializers.contains("\"" + id + "\"")) {
                problems.add(ResourceFiles.relative(file) + ": " + id);
            }
        }
        assertTrue(problems.isEmpty(), "unregistered recipe types: " + problems);
    }

    @Test
    void workbenchRecipesDeclareVisAndIngredients() {
        Set<String> research = ThaumonomiconResourcesTest.researchKeys();
        List<String> problems = new ArrayList<>();

        for (Path file : ResourceFiles.jsonFilesIn(RECIPES)) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            String type = string(recipe, "type");
            if (type == null || !WORKBENCH_TYPES.contains(type)) {
                continue;
            }
            String name = file.getFileName().toString();
            JsonElement vis = recipe.get("vis");
            if (vis == null || !vis.isJsonPrimitive() || vis.getAsInt() < 0) {
                problems.add(name + ": invalid vis");
            }
            checkResearch(recipe, research, name, problems);
            if (!recipe.has("ingredients") && !recipe.has("pattern")) {
                problems.add(name + ": no ingredients or pattern");
            }
            if (!recipe.has("result")) {
                problems.add(name + ": no result");
            }
        }
        assertTrue(problems.isEmpty(), "invalid workbench recipes: " + problems);
    }

    @Test
    void crystalEssenceInputsUseTheVariantAwareIngredient() {
        List<String> problems = new ArrayList<>();
        for (Path file : ResourceFiles.jsonFilesIn(RECIPES)) {
            String name = ResourceFiles.relative(file);
            // Crucible recipes produce aspect-tagged crystals in their result.
            if (name.startsWith(RECIPES + "/crucible")) {
                continue;
            }
            findPlainCrystalInputs(ResourceFiles.readJson(file), name, false, problems);
        }
        assertTrue(problems.isEmpty(), "crystal inputs without aspect variants: " + problems);
    }

    private static void findPlainCrystalInputs(JsonElement element, String name,
                                               boolean skip, List<String> problems) {
        if (element == null) {
            return;
        }
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> member : element.getAsJsonObject().entrySet()) {
                boolean childSkip = skip || "result".equals(member.getKey());
                if (!childSkip && member.getValue().isJsonObject()) {
                    JsonElement item = member.getValue().getAsJsonObject().get("item");
                    if (item != null && item.isJsonPrimitive()
                            && "arcaneresearch:crystal_essence".equals(item.getAsString())) {
                        problems.add(name + " (" + member.getKey() + ")");
                    }
                }
                findPlainCrystalInputs(member.getValue(), name, childSkip, problems);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                findPlainCrystalInputs(child, name, skip, problems);
            }
        }
    }

    @Test
    void crucibleRecipesDeclareAspectsAndCatalyst() {
        Set<String> research = ThaumonomiconResourcesTest.researchKeys();
        List<String> problems = new ArrayList<>();

        for (Path file : ResourceFiles.jsonFilesIn(RECIPES + "/crucible")) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            String name = file.getFileName().toString();
            checkAspects(recipe, name, problems);
            checkResearch(recipe, research, name, problems);
            JsonElement catalyst = recipe.get("catalyst");
            boolean validCatalyst = catalyst == null && recipe.has("ingredient");
            if (catalyst != null && catalyst.isJsonObject()) {
                JsonObject object = catalyst.getAsJsonObject();
                validCatalyst = object.has("item") || object.has("tag") || object.has("type");
            }
            if (!validCatalyst) {
                problems.add(name + ": invalid catalyst");
            }
        }
        assertTrue(problems.isEmpty(), "invalid crucible recipes: " + problems);
    }

    @Test
    void infusionEnchantmentRecipesDeclareEnchantmentAndAspects() {
        List<String> problems = new ArrayList<>();
        Pattern enchantmentName = Pattern.compile("[A-Z][A-Z0-9_]*");

        for (Path file : ResourceFiles.jsonFilesIn(RECIPES + "/infusion_enchantment")) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            String name = file.getFileName().toString();
            String enchantment = string(recipe, "enchantment");
            if (enchantment == null || !enchantmentName.matcher(enchantment).matches()) {
                problems.add(name + ": invalid enchantment " + enchantment);
            }
            checkAspects(recipe, name, problems);
            JsonElement components = recipe.get("components");
            if (components == null || !components.isJsonArray() || components.getAsJsonArray().isEmpty()) {
                problems.add(name + ": no components");
            }
        }
        assertTrue(problems.isEmpty(), "invalid infusion enchantments: " + problems);
    }

    @Test
    void recipeResearchReferencesPointToKnownEntries() {
        Set<String> research = ThaumonomiconResourcesTest.researchKeys();
        List<String> problems = new ArrayList<>();

        for (Path file : ResourceFiles.jsonFilesIn(RECIPES)) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            if (recipe.has("research")) {
                checkResearch(recipe, research, file.getFileName().toString(), problems);
            }
        }
        assertTrue(problems.isEmpty(), "unknown recipe research: " + problems);
    }

    private static void checkResearch(JsonObject recipe, Set<String> known, String name, List<String> problems) {
        String value = string(recipe, "research");
        if (value == null || value.isBlank()) {
            return; // recipes without a research gate are valid
        }
        for (String reference : value.split("&&")) {
            String key = reference.trim().split("@")[0];
            if (!known.contains(key)) {
                problems.add(name + ": unknown research " + reference);
            }
        }
    }

    private static void checkAspects(JsonObject recipe, String name, List<String> problems) {
        JsonElement aspects = recipe.get("aspects");
        if (aspects == null || !aspects.isJsonObject() || aspects.getAsJsonObject().entrySet().isEmpty()) {
            problems.add(name + ": no aspects");
            return;
        }
        for (var aspect : aspects.getAsJsonObject().entrySet()) {
            JsonElement amount = aspect.getValue();
            if (!aspect.getKey().matches("[a-z]+") || !amount.isJsonPrimitive() || amount.getAsInt() <= 0) {
                problems.add(name + ": invalid aspect " + aspect.getKey());
            }
        }
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }

    private static String source(String relativePath) {
        Path file = ResourceFiles.root().getParent().resolve("java").resolve(relativePath);
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Cannot read " + relativePath, e);
        }
    }
}
