package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Локализация: полнота и плейсхолдеры переводов")
class LocalizationResourcesTest {

    private static final String LANG = "assets/arcaneresearch/lang";
    private static final Pattern PLACEHOLDER = Pattern.compile("%[sd]");

    @Test
    void allLanguagesArePresentAndUseEnglishAsTheMasterKeySet() {
        List<Path> files = ResourceFiles.jsonFilesIn(LANG);
        assertEquals(9, files.size());
        Map<String, JsonElement> english = english();
        assertTrue(english.size() > 2000);

        for (Path file : files) {
            Map<String, JsonElement> language = ResourceFiles.readJson(file).getAsJsonObject().asMap();
            assertTrue(language.size() > 1500, file.getFileName() + " is nearly empty");
            Set<String> orphans = language.keySet().stream()
                    .filter(key -> !english.containsKey(key))
                    .collect(Collectors.toSet());
            assertTrue(orphans.isEmpty(), file.getFileName() + " has unknown keys: " + orphans);
        }
    }

    @Test
    void translationsKeepTheSameFormatPlaceholdersAsEnglish() {
        Map<String, JsonElement> english = english();
        List<String> problems = new ArrayList<>();

        for (Path file : ResourceFiles.jsonFilesIn(LANG)) {
            if (file.getFileName().toString().equals("en_us.json")) {
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : ResourceFiles.readJson(file)
                    .getAsJsonObject().entrySet()) {
                JsonElement original = english.get(entry.getKey());
                if (original == null || !original.isJsonPrimitive() || !entry.getValue().isJsonPrimitive()) {
                    continue;
                }
                List<String> expected = placeholders(original.getAsString());
                List<String> actual = placeholders(entry.getValue().getAsString());
                if (!expected.equals(actual)) {
                    problems.add(file.getFileName() + " " + entry.getKey()
                            + ": expected " + expected + " got " + actual);
                }
            }
        }
        assertTrue(problems.isEmpty(), "placeholder mismatches: " + problems);
    }

    @Test
    void translatedValuesAreStrings() {
        for (Path file : ResourceFiles.jsonFilesIn(LANG)) {
            for (Map.Entry<String, JsonElement> entry : ResourceFiles.readJson(file)
                    .getAsJsonObject().entrySet()) {
                assertTrue(entry.getValue().isJsonPrimitive(), file.getFileName() + " " + entry.getKey());
                entry.getValue().getAsString();
            }
        }
    }

    private static Map<String, JsonElement> english() {
        return ResourceFiles.readJson(LANG + "/en_us.json").getAsJsonObject().asMap();
    }

    private static List<String> placeholders(String text) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
}
