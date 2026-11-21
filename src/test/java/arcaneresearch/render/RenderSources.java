package arcaneresearch.render;

import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Read-only access to renderer sources and shader resources. */
final class RenderSources {

    private static final Path ROOT = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()));
    private static final Path JAVA = ROOT.getParent().resolve("java");

    private static final Pattern ENTITY_REGISTRATION = Pattern.compile(
            "(?:(?<!Block)EntityRenderers\\.register|registerEntityRenderer)\\(\\s*(?:[\\w.]+\\.)?(\\w+)\\.get\\(\\)\\s*,\\s*([\\w.]+)::new");
    private static final Pattern BE_REGISTRATION = Pattern.compile(
            "(?:BlockEntityRenderers\\.register|registerBlockEntityRenderer)\\(\\s*(?:[\\w.]+\\.)?(\\w+)\\.get\\(\\)\\s*,\\s*([\\w.]+)::new");

    private RenderSources() {
    }

    static String source(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return fail("Cannot read source " + relativePath, e);
        }
    }

    static String compact(String source) {
        return source.replaceAll("\\s+", " ");
    }

    static boolean resourceExists(String relativePath) {
        return Files.isRegularFile(ROOT.resolve(relativePath));
    }

    static JsonObject json(String relativePath) {
        try (Reader reader = Files.newBufferedReader(ROOT.resolve(relativePath), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            return fail("Cannot read resource " + relativePath, e);
        }
    }

    static List<Path> jsonFiles(String relativeDirectory) {
        Path directory = ROOT.resolve(relativeDirectory);
        try (var files = Files.walk(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted().toList();
        } catch (IOException e) {
            return fail("Cannot list " + relativeDirectory, e);
        }
    }

    static List<Path> javaFiles(String relativeDirectory) {
        Path directory = JAVA.resolve(relativeDirectory);
        try (var files = Files.walk(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted().toList();
        } catch (IOException e) {
            return fail("Cannot list " + relativeDirectory, e);
        }
    }

    static List<Registration> entityRenderers() {
        List<Registration> result = new ArrayList<>();
        for (String file : List.of("arcaneresearch/Arcaneresearch.java", "arcaneresearch/client/WandClientSetup.java")) {
            result.addAll(registrations(file, ENTITY_REGISTRATION));
        }
        return result;
    }

    static List<Registration> blockEntityRenderers() {
        List<Registration> result = new ArrayList<>();
        for (String file : List.of("arcaneresearch/Arcaneresearch.java",
                "arcaneresearch/client/WandClientSetup.java",
                "arcaneresearch/discovered/table/HexTableRegistry.java")) {
            result.addAll(registrations(file, BE_REGISTRATION));
        }
        return result;
    }

    static java.util.Set<String> registeredEntityFields() {
        java.util.Set<String> fields = new java.util.HashSet<>();
        for (String file : List.of("arcaneresearch/init/ModEntities.java",
                "arcaneresearch/common/entities/WandEntities.java")) {
            Matcher matcher = Pattern.compile("DeferredHolder<EntityType<[^>]*>,\\s*EntityType<[\\w.<>]+>\\s*>\\s+(\\w+)\\s*=")
                    .matcher(source(file));
            while (matcher.find()) {
                fields.add(matcher.group(1));
            }
        }
        return fields;
    }

    static java.util.Set<String> registeredBlockEntityFields() {
        java.util.Set<String> fields = new java.util.HashSet<>();
        for (String file : List.of("arcaneresearch/init/ModBlockEntities.java",
                "arcaneresearch/common/blocks/aura/AuraBlockEntities.java")) {
            Matcher matcher = Pattern.compile("DeferredHolder<BlockEntityType<[^>]*>,\\s*BlockEntityType<[\\w.<>]+>\\s*>\\s+(\\w+)\\s*=")
                    .matcher(source(file));
            while (matcher.find()) {
                fields.add(matcher.group(1));
            }
        }
        return fields;
    }

    static String rendererSource(String simpleName) {
        for (Path path : javaFiles("arcaneresearch/client/renderers")) {
            if (path.getFileName().toString().equals(simpleName + ".java")) {
                return source(JAVA.relativize(path).toString().replace('\\', '/'));
            }
        }
        return null;
    }

    private static List<Registration> registrations(String file, Pattern pattern) {
        List<Registration> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(compact(source(file)));
        while (matcher.find()) {
            String renderer = matcher.group(2);
            result.add(new Registration(matcher.group(1), renderer.substring(renderer.lastIndexOf('.') + 1)));
        }
        return result;
    }

    record Registration(String field, String renderer) {
    }
}
