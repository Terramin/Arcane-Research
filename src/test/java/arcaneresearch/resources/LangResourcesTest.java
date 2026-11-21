package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ресурсы: файлы локализации")
class LangResourcesTest {

    private static final String EN = "assets/arcaneresearch/lang/en_us.json";
    private static final String RU = "assets/arcaneresearch/lang/ru_ru.json";

    @Test
    void englishAndRussianHaveTheSameKeySet() {
        Set<String> english = keys(EN);
        Set<String> russian = keys(RU);

        assertTrue(english.size() > 2000);
        assertEquals(english, russian);
    }

    @Test
    void primaryLanguagesHaveNoDuplicateKeys() {
        assertTrue(ResourceFiles.duplicateKeys(ResourceFiles.file(EN)).isEmpty());
        assertTrue(ResourceFiles.duplicateKeys(ResourceFiles.file(RU)).isEmpty());
    }

    @Test
    void coreTranslationKeysArePresent() {
        Set<String> english = keys(EN);

        assertTrue(english.contains("item.arcaneresearch.thaumonomicon"));
        assertTrue(english.contains("research.FIRSTSTEPS.title"));
        assertTrue(english.contains("item.Wand.name"));
        assertTrue(english.contains("gui.arcaneresearch.arcane_workbench"));
    }

    private static Set<String> keys(String path) {
        return ResourceFiles.readJson(path).getAsJsonObject().entrySet().stream()
                .map(entry -> entry.getKey())
                .collect(Collectors.toSet());
    }
}
