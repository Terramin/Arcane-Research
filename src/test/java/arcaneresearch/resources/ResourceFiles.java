package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Read-only access to the mod's source resources for structural tests. */
final class ResourceFiles {

    private static final Path ROOT = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()));

    private ResourceFiles() {
    }

    static Path root() {
        return ROOT;
    }

    /** {@code src/main/java}, next to the resource root. */
    static Path javaRoot() {
        return ROOT.getParent().resolve("java");
    }

    static String relative(Path file) {
        return ROOT.relativize(file).toString().replace('\\', '/');
    }

    static Path file(String relativePath) {
        return ROOT.resolve(relativePath);
    }

    static boolean exists(String relativePath) {
        return Files.isRegularFile(ROOT.resolve(relativePath));
    }

    static List<Path> jsonFiles() {
        try (Stream<Path> paths = Files.walk(ROOT)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return fail("Cannot list resources under " + ROOT, e);
        }
    }

    static List<Path> jsonFilesIn(String directory) {
        String prefix = directory.endsWith("/") ? directory : directory + "/";
        return jsonFiles().stream()
                .filter(path -> relative(path).startsWith(prefix))
                .toList();
    }

    static JsonElement readJson(Path file) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            return fail("Cannot read " + relative(file), e);
        }
    }

    static JsonElement readJson(String relativePath) {
        return readJson(file(relativePath));
    }

    /** Finds repeated object member names at any depth, including nested objects. */
    static List<String> duplicateKeys(Path file) {
        List<String> duplicates = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            scanValue(new JsonReader(reader), duplicates);
        } catch (IOException e) {
            fail("Cannot scan " + relative(file), e);
        }
        return duplicates;
    }

    private static void scanValue(JsonReader reader, List<String> duplicates) throws IOException {
        JsonToken token = reader.peek();
        if (token == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            Set<String> names = new HashSet<>();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (!names.add(name)) {
                    duplicates.add(name);
                }
                scanValue(reader, duplicates);
            }
            reader.endObject();
        } else if (token == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                scanValue(reader, duplicates);
            }
            reader.endArray();
        } else {
            reader.skipValue();
        }
    }
}
