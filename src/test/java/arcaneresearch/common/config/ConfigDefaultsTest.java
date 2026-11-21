package arcaneresearch.common.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Конфигурация: значения по умолчанию и синхронизация алиасов")
class ConfigDefaultsTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", "src/main/resources")).getParent().resolve("java");

    @Test
    void worldDefaultsAreInsideSaneBounds() throws Exception {
        Class<?> world = Class.forName("arcaneresearch.common.config.ModConfig$CONFIG_WORLD");
        assertEquals(100, intValue(world, "oreDensity"));
        assertEquals(32, intValue(world, "taintSpreadArea"));
        assertEquals(5, intValue(world, "biomeMagicalForestWeight"));
        assertEquals(100.0f, floatValue(world, "taintSpreadRate"), 1.0e-6f);
        assertTrue(booleanValue(world, "generateAura"));
        assertTrue(booleanValue(world, "generateStructure"));
        assertFalse(booleanValue(world, "regenAura"));
    }

    @Test
    void miscDefaultsAreInsideSaneBounds() throws Exception {
        Class<?> misc = Class.forName("arcaneresearch.common.config.ModConfig$CONFIG_MISC");
        assertEquals(2000, intValue(misc, "shieldRecharge"));
        assertEquals(4000, intValue(misc, "shieldWait"));
        assertEquals(1, intValue(misc, "shieldCost"));
        assertFalse(booleanValue(misc, "allowCheatSheet"));
    }

    @Test
    void everyFlatAliasIsSynchronizedFromItsConfigGroup() throws Exception {
        String source = Files.readString(
                JAVA.resolve("arcaneresearch/common/config/ModConfig.java"), StandardCharsets.UTF_8);
        String sync = source.substring(source.indexOf("syncFlatAliases()"));
        sync = sync.substring(0, sync.indexOf("\n    }"));
        SetOfNames aliases = new SetOfNames();

        Matcher matcher = Pattern.compile("^\\s*public static [\\w<>\\[\\]]+ (\\w+) = CONFIG_(\\w+)\\.(\\w+);",
                Pattern.MULTILINE).matcher(source);
        int count = 0;
        while (matcher.find()) {
            count++;
            aliases.add(matcher.group(1));
            String expected = matcher.group(2) + "." + matcher.group(3);
            assertTrue(sync.contains("= CONFIG_" + expected + ";"),
                    "syncFlatAliases misses " + matcher.group(1) + " <- " + expected);
        }
        assertTrue(count > 20, "unexpectedly few flat aliases: " + count);
    }

    private static int intValue(Class<?> owner, String name) throws Exception {
        return field(owner, name).getInt(null);
    }

    private static float floatValue(Class<?> owner, String name) throws Exception {
        return field(owner, name).getFloat(null);
    }

    private static boolean booleanValue(Class<?> owner, String name) throws Exception {
        return field(owner, name).getBoolean(null);
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getField(name);
        field.setAccessible(true);
        return field;
    }

    private static final class SetOfNames {
        private final java.util.Set<String> names = new java.util.HashSet<>();

        void add(String name) {
            names.add(name);
        }
    }
}
