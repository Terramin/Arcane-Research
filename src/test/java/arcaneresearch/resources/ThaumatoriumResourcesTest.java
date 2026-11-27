package arcaneresearch.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Ресурсы тауматория")
class ThaumatoriumResourcesTest {

    private static final String ASSETS = "assets/arcaneresearch/";
    private static final String DATA = "data/arcaneresearch/";

    private static JsonObject object(String relativePath) {
        JsonElement element = ResourceFiles.readJson(relativePath);
        assertTrue(element.isJsonObject(), relativePath + " is not an object");
        return element.getAsJsonObject();
    }

    @Test
    @DisplayName("blockstate defines all four facings and their models exist")
    void blockstates() {
        JsonObject variants = object(ASSETS + "blockstates/thaumatorium.json")
                .getAsJsonObject("variants");
        for (String facing : new String[]{"north", "south", "east", "west"}) {
            assertTrue(variants.has("facing=" + facing), "missing facing " + facing);
            JsonObject model = variants.getAsJsonObject("facing=" + facing);
            String name = model.get("model").getAsString().replace("arcaneresearch:", "");
            assertTrue(ResourceFiles.exists(ASSETS + "models/" + name + ".json"), name);
        }
        assertTrue(ResourceFiles.exists(ASSETS + "blockstates/thaumatorium_top.json"));
    }

    @Test
    @DisplayName("OBJ model and its textures are present")
    void modelsAndTextures() {
        JsonObject model = object(ASSETS + "models/block/thaumatorium.json");
        assertEquals("forge:obj", model.get("loader").getAsString());
        String obj = model.get("model").getAsString().replace("arcaneresearch:", "");
        assertTrue(ResourceFiles.exists(ASSETS + obj), obj);
        assertTrue(ResourceFiles.exists(ASSETS
                + obj.substring(0, obj.lastIndexOf('.')) + ".mtl"));

        JsonObject textures = model.getAsJsonObject("textures");
        String texture = textures.get("texture").getAsString().replace("arcaneresearch:", "");
        assertTrue(ResourceFiles.exists(ASSETS + "textures/" + texture + ".png"));
        assertTrue(ResourceFiles.exists(ASSETS + "models/item/thaumatorium.json"));
        assertTrue(ResourceFiles.exists(ASSETS + "models/item/thaumatorium_top.json"));
    }

    @Test
    @DisplayName("sounds used by the furnace exist")
    void sounds() {
        JsonObject sounds = object(ASSETS + "sounds.json");
        for (String name : new String[]{"clack", "hhon"}) {
            assertTrue(sounds.has(name), "missing sound " + name);
        }
        assertTrue(ResourceFiles.exists(ASSETS + "sounds/clack1.ogg"));
        assertTrue(ResourceFiles.exists(ASSETS + "sounds/clack2.ogg"));
        assertTrue(ResourceFiles.exists(ASSETS + "sounds/clack3.ogg"));
        assertTrue(ResourceFiles.exists(ASSETS + "sounds/hhon.ogg"));
    }

    @Test
    @DisplayName("loot table, research entry and pickaxe tag are wired")
    void dataFiles() {
        assertTrue(ResourceFiles.exists(DATA + "loot_tables/blocks/thaumatorium.json"));
        String research = ResourceFiles.readJson(
                ASSETS + "research/alchemy.json").toString();
        assertTrue(research.contains("\"key\":\"THAUMATORIUM\""));

        String tag = ResourceFiles.readJson(
                "data/minecraft/tags/blocks/mineable/pickaxe.json").toString();
        assertTrue(tag.contains("arcaneresearch:thaumatorium"));
        assertTrue(tag.contains("arcaneresearch:thaumatorium_top"));
    }

    @Test
    @DisplayName("localization keys exist in English and Russian")
    void localization() {
        for (String lang : new String[]{"en_us", "ru_ru"}) {
            JsonObject keys = object(ASSETS + "lang/" + lang + ".json");
            assertTrue(keys.has("block.arcaneresearch.thaumatorium"), lang);
            assertTrue(keys.has("block.arcaneresearch.thaumatorium_top"), lang);
            assertTrue(keys.has("research.THAUMATORIUM.title"), lang);
            assertTrue(keys.has("research.THAUMATORIUM.stage.1"), lang);
            assertTrue(keys.has("research.THAUMATORIUM.stage.2"), lang);
        }
    }

    @Test
    @DisplayName("GUI texture is large enough for every drawn element")
    void guiTextureSize() {
        try {
            BufferedImage image = ImageIO.read(ResourceFiles.file(
                    ASSETS + "textures/gui/gui_thaumatorium.png").toFile());
            assertNotNull(image);
            assertTrue(image.getWidth() >= 175, "width=" + image.getWidth());
            assertTrue(image.getHeight() >= 216, "height=" + image.getHeight());
        } catch (IOException e) {
            fail("Cannot read GUI texture", e);
        }
    }
}
