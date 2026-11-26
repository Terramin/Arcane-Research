package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Ресурсы: конфигурации миксинов")
class MixinConfigTest {

    private static final List<String> CONFIGS = List.of(
            "arcaneresearch.mixins.json",
            "arcaneresearch.crit.mixins.json",
            "arcaneresearch.lore.mixins.json",
            "arcaneresearch.helper.mixins.json",
            "arcaneresearch.wards.mixins.json");

    @Test
    void allConfigsArePresentAndDeclareRequiredMetadata() {
        for (String name : CONFIGS) {
            assertTrue(ResourceFiles.exists(name), "missing mixin config " + name);
            JsonObject config = ResourceFiles.readJson(name).getAsJsonObject();

            assertTrue(config.get("required").getAsBoolean(), name + " must be required");
            assertEquals("JAVA_17", config.get("compatibilityLevel").getAsString());
            assertFalse(config.get("minVersion").getAsString().isBlank());
            assertFalse(config.get("package").getAsString().isBlank());
            assertFalse(config.get("refmap").getAsString().isBlank());
        }
    }

    @Test
    void everyDeclaredMixinClassExistsInSources() {
        for (String name : CONFIGS) {
            JsonObject config = ResourceFiles.readJson(name).getAsJsonObject();
            Path packageDir = ResourceFiles.javaRoot().resolve(
                    config.get("package").getAsString().replace('.', '/'));
            List<String> missing = new ArrayList<>();
            int count = 0;

            for (String section : List.of("mixins", "client", "server")) {
                JsonArray classes = config.getAsJsonArray(section);
                if (classes == null) {
                    continue;
                }
                for (var element : classes) {
                    count++;
                    Path source = packageDir.resolve(element.getAsString().replace('.', '/') + ".java");
                    if (!Files.isRegularFile(source)) {
                        missing.add(section + ":" + element.getAsString());
                    }
                }
            }

            assertTrue(count > 0, name + " declares no mixins");
            assertTrue(missing.isEmpty(), name + " references missing classes: " + missing);
        }
    }

    @Test
    void wardBreakMixinStopsClientSideBreakPrediction() throws Exception {
        Path mixin = ResourceFiles.javaRoot().resolve(
                "arcaneresearch/client/wards/mixin/WardBlockBreakMixin.java");
        String source = Files.readString(mixin, StandardCharsets.UTF_8);
        assertTrue(source.contains("@Mixin(MultiPlayerGameMode.class)"));
        assertTrue(source.contains("startDestroyBlock"));
        assertTrue(source.contains("destroyBlock"));
        assertTrue(source.contains("setReturnValue(false)"));
    }
}
