package arcaneresearch.gui;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Read-only source inspection for GUI wiring tests (no Minecraft bootstrap). */
final class GuiSources {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()))
            .getParent().resolve("java");

    private static final Pattern MENU_REGISTRATION = Pattern.compile(
            "RegistryObject<MenuType<([\\w.]+)>>\\s+(\\w+)\\s*=\\s*MENU_TYPES\\.register\\(\"([^\"]+)\"");
    private static final Pattern BE_REGISTRATION = Pattern.compile(
            "RegistryObject<BlockEntityType<([\\w.]+)>>\\s+(\\w+)\\s*=\\s*\\w+\\.register\\(\"([^\"]+)\"");

    private GuiSources() {
    }

    static String source(String relativePath) {
        Path file = JAVA.resolve(relativePath);
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return fail("Cannot read source " + relativePath, e);
        }
    }

    static String source(String packagePath, String simpleName) {
        return source(packagePath + "/" + simpleName + ".java");
    }

    static boolean sourceExists(String relativePath) {
        return Files.isRegularFile(JAVA.resolve(relativePath));
    }

    static String tileSource(String simpleName) {
        for (String directory : List.of("aura", "crafting", "devices", "essentia", "misc")) {
            Path file = JAVA.resolve("arcaneresearch/common/tiles/" + directory + "/" + simpleName + ".java");
            if (Files.isRegularFile(file)) {
                return source("arcaneresearch/common/tiles/" + directory + "/" + simpleName + ".java");
            }
        }
        return fail("No tile source found for " + simpleName);
    }

    /** Whitespace-insensitive view for matching multi-line registrations. */
    static String compact(String source) {
        return source.replaceAll("\\s+", " ");
    }

    static List<Registration> menus() {
        return registrations("arcaneresearch/init/ModMenuTypes.java", MENU_REGISTRATION);
    }

    static List<Registration> blockEntities() {
        List<Registration> result = new ArrayList<>();
        result.addAll(registrations("arcaneresearch/init/ModBlockEntities.java", BE_REGISTRATION));
        result.addAll(registrations("arcaneresearch/common/blocks/aura/AuraBlockEntities.java", BE_REGISTRATION));
        return result;
    }

    static boolean hasConstructor(String source, String className, String thirdParameter) {
        Pattern pattern = Pattern.compile("public\\s+" + Pattern.quote(className)
                + "\\s*\\(\\s*int\\s+\\w+\\s*,\\s*Inventory\\s+\\w+\\s*,\\s*"
                + Pattern.quote(thirdParameter) + "\\s+\\w+\\s*\\)");
        return pattern.matcher(compact(source)).find();
    }

    static boolean hasBlockEntity(String simpleName) {
        return blockEntities().stream().anyMatch(registration -> registration.type().equals(simpleName));
    }

    /** Tile sources that provide a menu directly or open one from their block. */
    static Set<String> allGuiTileNames() {
        Set<String> result = new HashSet<>();
        Path tiles = JAVA.resolve("arcaneresearch/common/tiles");
        try (var files = Files.walk(tiles)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                if (text.contains("MenuProvider")
                        || Pattern.compile("new\\s+(?!AbstractContainerMenu)\\w+Menu\\s*\\(").matcher(text).find()) {
                    result.add(file.getFileName().toString().replace(".java", ""));
                }
            }
        } catch (IOException e) {
            fail("Cannot scan tile sources", e);
        }
        // Menus can be opened by a block while the tile only supplies the container.
        for (Registration menu : menus()) {
            String text = source("arcaneresearch/common/menu", menu.type());
            Matcher matcher = Pattern.compile("\\b(Tile[A-Z]\\w+|AuraTotemTile|DeconstructTile)\\b").matcher(text);
            while (matcher.find()) {
                if (hasBlockEntity(matcher.group(1))) {
                    result.add(matcher.group(1));
                }
            }
        }
        return result;
    }

    static Set<String> englishKeys() {
        Path lang = JAVA.getParent().resolve("resources/assets/arcaneresearch/lang/en_us.json");
        Set<String> keys = new HashSet<>();
        try (var reader = Files.newBufferedReader(lang, StandardCharsets.UTF_8)) {
            com.google.gson.JsonParser.parseReader(reader).getAsJsonObject()
                    .entrySet().forEach(entry -> keys.add(entry.getKey()));
        } catch (IOException e) {
            fail("Cannot read en_us.json", e);
        }
        return keys;
    }

    private static List<Registration> registrations(String path, Pattern pattern) {
        List<Registration> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(source(path));
        while (matcher.find()) {
            String type = matcher.group(1);
            result.add(new Registration(type.substring(type.lastIndexOf('.') + 1),
                    matcher.group(2), matcher.group(3)));
        }
        return result;
    }

    record Registration(String type, String field, String id) {
    }
}
