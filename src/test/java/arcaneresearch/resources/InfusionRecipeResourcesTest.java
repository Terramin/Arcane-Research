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

@DisplayName("Инфузионный алтарь: данные рецептов инфузии")
class InfusionRecipeResourcesTest {

    private static final String INFUSION = "data/arcaneresearch/recipes/infusion";

    @Test
    void everyInfusionRecipeHasCompleteInputsAndAspects() {
        List<Path> files = ResourceFiles.jsonFilesIn(INFUSION);
        Set<String> researchKeys = ThaumonomiconResourcesTest.researchKeys();
        List<String> problems = new ArrayList<>();

        for (Path file : files) {
            JsonObject recipe = ResourceFiles.readJson(file).getAsJsonObject();
            String name = file.getFileName().toString();

            if (!"arcaneresearch:infusion".equals(string(recipe, "type"))) {
                problems.add(name + ": wrong recipe type");
            }
            String research = string(recipe, "research");
            if (research == null || !researchKeys.contains(research.split("@")[0])) {
                problems.add(name + ": unknown research " + research);
            }
            JsonElement instability = recipe.get("instability");
            if (instability == null || !instability.isJsonPrimitive()
                    || instability.getAsInt() < 0) {
                problems.add(name + ": invalid instability");
            }
            if (!hasIngredient(recipe, "input") && !hasIngredient(recipe, "center")
                    && !hasIngredient(recipe, "central")) {
                problems.add(name + ": no central ingredient");
            }
            if (!hasIngredient(recipe, "result")) {
                problems.add(name + ": no result");
            }
            JsonElement ingredients = firstPresent(recipe, "ingredients", "components");
            if (ingredients == null || !ingredients.isJsonArray() || ingredients.getAsJsonArray().isEmpty()) {
                problems.add(name + ": no pedestal ingredients");
            } else {
                for (JsonElement ingredient : ingredients.getAsJsonArray()) {
                    if (!isValidIngredient(ingredient)) {
                        problems.add(name + ": malformed ingredient");
                        break;
                    }
                }
            }
            JsonElement aspects = recipe.get("aspects");
            if (aspects == null || !aspects.isJsonObject() || aspects.getAsJsonObject().entrySet().isEmpty()) {
                problems.add(name + ": no aspects");
            } else {
                for (var aspect : aspects.getAsJsonObject().entrySet()) {
                    JsonElement amount = aspect.getValue();
                    if (!aspect.getKey().matches("[a-z]+")
                            || !amount.isJsonPrimitive() || amount.getAsInt() <= 0) {
                        problems.add(name + ": invalid aspect " + aspect.getKey());
                    }
                }
            }
        }

        assertTrue(files.size() > 80, "unexpectedly few infusion recipes: " + files.size());
        assertTrue(problems.isEmpty(), "invalid infusion recipes: " + problems);
    }

    @Test
    void everyAspectNameIsUsedWithAPositiveAmount() {
        Set<String> names = new HashSet<>();
        for (Path file : ResourceFiles.jsonFilesIn(INFUSION)) {
            JsonElement aspects = ResourceFiles.readJson(file).getAsJsonObject().get("aspects");
            if (aspects != null && aspects.isJsonObject()) {
                names.addAll(aspects.getAsJsonObject().keySet());
            }
        }
        assertTrue(names.size() > 20, "aspect set looks truncated: " + names.size());
        assertFalse(names.contains(""));
    }

    private static JsonElement firstPresent(JsonObject object, String... keys) {
        for (String key : keys) {
            JsonElement value = object.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** Ingredients may be a single entry or an array of alternatives. */
    private static boolean isValidIngredient(JsonElement element) {
        if (element.isJsonArray()) {
            if (element.getAsJsonArray().isEmpty()) {
                return false;
            }
            for (JsonElement alternative : element.getAsJsonArray()) {
                if (!isValidIngredient(alternative)) {
                    return false;
                }
            }
            return true;
        }
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject ingredient = element.getAsJsonObject();
        return ingredient.has("item") || ingredient.has("tag") || ingredient.has("type");
    }

    private static boolean hasIngredient(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonObject()) {
            return false;
        }
        JsonObject ingredient = value.getAsJsonObject();
        return ingredient.has("item") || ingredient.has("tag") || ingredient.has("type");
    }

    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }
}
