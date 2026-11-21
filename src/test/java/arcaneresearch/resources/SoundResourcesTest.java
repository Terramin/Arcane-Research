package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ресурсы: звуки")
class SoundResourcesTest {

    @Test
    void everyReferencedOggFileExists() {
        var events = ResourceFiles.readJson("assets/arcaneresearch/sounds.json").getAsJsonObject();
        List<String> missing = new ArrayList<>();

        for (var event : events.entrySet()) {
            JsonElement sounds = event.getValue().getAsJsonObject().get("sounds");
            assertTrue(sounds != null && sounds.isJsonArray(), "event " + event.getKey() + " has no sounds array");
            for (JsonElement sound : sounds.getAsJsonArray()) {
                String name = sound.isJsonPrimitive()
                        ? sound.getAsString()
                        : sound.getAsJsonObject().get("name").getAsString();
                if (name.startsWith("arcaneresearch:")) {
                    String file = "assets/arcaneresearch/sounds/" + name.substring("arcaneresearch:".length()) + ".ogg";
                    if (!ResourceFiles.exists(file)) {
                        missing.add(event.getKey() + " -> " + name);
                    }
                }
            }
        }

        assertTrue(events.size() > 50, "unexpectedly few sound events: " + events.size());
        assertTrue(missing.isEmpty(), "missing sound files: " + missing);
    }

    @Test
    void soundFilesAreLowerSnakeCase() {
        var sounds = ResourceFiles.file("assets/arcaneresearch/sounds");
        try (var files = java.nio.file.Files.list(sounds)) {
            List<String> invalid = files
                    .map(path -> path.getFileName().toString())
                    .filter(name -> !name.matches("[a-z0-9_]+\\.ogg"))
                    .sorted()
                    .toList();
            assertTrue(invalid.isEmpty(), "unexpected sound file names: " + invalid);
        } catch (java.io.IOException e) {
            throw new AssertionError("Cannot list sound files", e);
        }
    }
}
