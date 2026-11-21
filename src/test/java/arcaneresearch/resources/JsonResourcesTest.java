package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ресурсы: корректность JSON")
class JsonResourcesTest {

    @Test
    void everyJsonResourceParses() {
        List<Path> files = ResourceFiles.jsonFiles();
        List<String> invalid = new ArrayList<>();

        for (Path file : files) {
            try {
                ResourceFiles.readJson(file);
            } catch (AssertionError e) {
                invalid.add(ResourceFiles.relative(file));
            }
        }

        assertTrue(files.size() > 2000, "unexpectedly few JSON resources: " + files.size());
        assertTrue(invalid.isEmpty(), "invalid JSON: " + invalid);
    }

    @Test
    void noJsonObjectContainsDuplicateKeys() {
        Map<String, List<String>> duplicates = new LinkedHashMap<>();

        for (Path file : ResourceFiles.jsonFiles()) {
            List<String> keys = ResourceFiles.duplicateKeys(file);
            if (!keys.isEmpty()) {
                duplicates.put(ResourceFiles.relative(file), keys);
            }
        }

        assertTrue(duplicates.isEmpty(), "duplicate keys (last value silently wins): " + duplicates);
    }

    @Test
    void packMetadataDeclaresExpectedFormat() {
        var pack = ResourceFiles.readJson("pack.mcmeta").getAsJsonObject().getAsJsonObject("pack");

        assertEquals(15, pack.get("pack_format").getAsInt());
        assertFalse(pack.get("description").getAsString().isBlank());
    }

    @Test
    void legacyMetadataDeclaresThisMod() {
        var entries = ResourceFiles.readJson("mcmod.info").getAsJsonArray();

        assertEquals(1, entries.size());
        assertEquals("arcaneresearch", entries.get(0).getAsJsonObject().get("modid").getAsString());
    }
}
