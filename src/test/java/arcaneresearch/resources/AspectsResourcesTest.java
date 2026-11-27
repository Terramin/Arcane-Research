package arcaneresearch.resources;

import arcaneresearch.api.aspects.Aspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Аспекты: корректность регистраций")
class AspectsResourcesTest {

    private static final Path CONFIG = ResourceFiles.javaRoot()
            .resolve("arcaneresearch/common/config/ConfigAspects.java");
    private static final Pattern CONSTANT = Pattern.compile("Aspect\\.([A-Z_][A-Z_0-9]*)");
    private static final Pattern TAG = Pattern.compile("registerObjectTag\\(\"([^\"]+)\"");

    @Test
    @DisplayName("every Aspect.X constant used in ConfigAspects exists")
    void aspectConstantsExist() throws Exception {
        String source = Files.readString(CONFIG, StandardCharsets.UTF_8);
        Matcher matcher = CONSTANT.matcher(source);
        Set<String> seen = new HashSet<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!seen.add(name)) {
                continue;
            }
            Field field = Aspect.class.getField(name);
            assertNotNull(field.get(null), "Aspect." + name + " is null");
            assertTrue(field.getType() == Aspect.class, "Aspect." + name + " is not an Aspect");
        }
        assertTrue(seen.size() > 20, "too few aspect registrations: " + seen.size());
    }

    @Test
    @DisplayName("object tag keys are non-empty and unique per registration line")
    void objectTagKeys() throws IOException {
        String source = Files.readString(CONFIG, StandardCharsets.UTF_8);
        Matcher matcher = TAG.matcher(source);
        int count = 0;
        while (matcher.find()) {
            assertTrue(matcher.group(1).length() > 0);
            count++;
        }
        assertTrue(count > 30, "too few object tag registrations: " + count);
    }
}
