package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Wand Focus: Warding: wiring between code and resources")
class WardingPortResourcesTest {

    private static final String RECIPE = "data/arcaneresearch/recipes/infusion/wand_focus_warding.json";
    private static final String MODEL = "assets/arcaneresearch/models/item/wand_focus_warding.json";

    @Test
    void focusIsRegisteredAndItsModelTextureExists() throws Exception {
        String wandItems = Files.readString(ResourceFiles.javaRoot()
                .resolve("arcaneresearch/common/items/wands/WandItems.java"), StandardCharsets.UTF_8);
        assertTrue(wandItems.contains("\"wand_focus_warding\""), "focus item is not registered");

        JsonObject model = ResourceFiles.readJson(MODEL).getAsJsonObject();
        String texture = model.getAsJsonObject("textures").get("layer0").getAsString();
        assertEquals("arcaneresearch:item/wand/focus_warding", texture);
        assertTrue(ResourceFiles.exists("assets/arcaneresearch/textures/item/wand/focus_warding.png"));
    }

    @Test
    void infusionRecipeIsBoundToTheWardingResearch() {
        JsonObject recipe = ResourceFiles.readJson(RECIPE).getAsJsonObject();
        assertEquals("arcaneresearch:infusion", recipe.get("type").getAsString());
        assertEquals("FOCUSWARDING", recipe.get("research").getAsString());
        assertEquals("arcaneresearch:wand_focus_warding",
                recipe.getAsJsonObject("result").get("item").getAsString());
        assertTrue(recipe.getAsJsonObject("aspects").entrySet().stream()
                .allMatch(entry -> entry.getValue().getAsInt() > 0));
        assertTrue(recipe.getAsJsonArray("ingredients").size() >= 8);
    }

    @Test
    void researchEntryGrantsTheRecipeAndRequiresTheFocus() {
        JsonObject warding = null;
        for (JsonElement element : ResourceFiles.readJson("assets/arcaneresearch/research/legacy.json")
                .getAsJsonObject().getAsJsonArray("entries")) {
            JsonObject entry = element.getAsJsonObject();
            if ("FOCUSWARDING".equals(entry.get("key").getAsString())) {
                warding = entry;
                break;
            }
        }
        assertTrue(warding != null, "FOCUSWARDING research entry missing");
        assertEquals("arcaneresearch:wand_focus_warding",
                warding.getAsJsonArray("icons").get(0).getAsString());

        JsonObject stage = warding.getAsJsonArray("stages").get(0).getAsJsonObject();
        assertTrue(stage.getAsJsonArray("recipes").toString().contains("wand_focus_warding"));
        assertTrue(stage.getAsJsonArray("required_craft").toString().contains("wand_focus_warding"));
    }

    @Test
    void nameAndTooltipAreTranslatedInBothPrimaryLanguages() {
        for (String language : new String[]{"en_us", "ru_ru"}) {
            JsonObject lang = ResourceFiles.readJson("assets/arcaneresearch/lang/" + language + ".json")
                    .getAsJsonObject();
            assertTrue(lang.has("item.arcaneresearch.wand_focus_warding"), language + ": item name");
            assertTrue(lang.has("item.arcaneresearch.wand_focus_warding.tooltip"),
                    language + ": tooltip");
        }
    }
}
