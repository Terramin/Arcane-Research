package arcaneresearch.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Шейдеры: json, GLSL и регистрация")
class ShaderResourcesTest {

    private static final String CORE = "assets/arcaneresearch/shaders/core";
    private static final String PROGRAM = "assets/arcaneresearch/shaders/program";
    private static final String POST = "assets/arcaneresearch/shaders/post";
    private static final Set<String> VANILLA_PROGRAMS = Set.of("blit", "blur", "color_convolve");

    @Test
    void coreShadersHaveVertexAndFragmentSources() {
        List<Path> configs = RenderSources.jsonFiles(CORE);
        assertEquals(3, configs.size());

        for (Path config : configs) {
            String name = config.getFileName().toString().replace(".json", "");
            JsonObject json = RenderSources.json(CORE + "/" + config.getFileName());
            assertEquals("arcaneresearch:" + name, json.get("vertex").getAsString());
            assertEquals("arcaneresearch:" + name, json.get("fragment").getAsString());
            assertTrue(RenderSources.resourceExists(CORE + "/" + name + ".vsh"), name + " lacks .vsh");
            assertTrue(RenderSources.resourceExists(CORE + "/" + name + ".fsh"), name + " lacks .fsh");
        }
    }

    @Test
    void shaderUniformDeclarationsMatchTheirValueArrays() {
        for (Path config : allShaderConfigs()) {
            JsonObject json = RenderSources.json(relativeToResources(config));
            JsonArray uniforms = json.getAsJsonArray("uniforms");
            assertFalse(uniforms == null || uniforms.isEmpty());

            Set<String> names = new HashSet<>();
            for (JsonElement element : uniforms) {
                JsonObject uniform = element.getAsJsonObject();
                String name = uniform.get("name").getAsString();
                assertTrue(names.add(name), "duplicate uniform " + name);
                assertFalse(uniform.get("type").getAsString().isBlank());
                int count = uniform.get("count").getAsInt();
                JsonArray values = uniform.getAsJsonArray("values");
                assertEquals(count, values.size(), name + ": count/values mismatch");
            }
        }
    }

    @Test
    void registeredCoreShadersAreDeclaredInTheResourceTree() {
        String registration = RenderSources.compact(
                RenderSources.source("arcaneresearch/client/fx/FxWaveShaders.java"));

        for (Path config : RenderSources.jsonFiles(CORE)) {
            String name = config.getFileName().toString().replace(".json", "");
            assertTrue(registration.contains("\"" + name + "\"")
                            || registration.contains("\"arcaneresearch:" + name + "\""),
                    "FxWaveShaders does not register " + name);
        }
    }

    @Test
    void postPassesResolveToProgramsAndDeclaredTargets() {
        for (Path config : RenderSources.jsonFiles(POST)) {
            JsonObject json = RenderSources.json(POST + "/" + config.getFileName());
            Set<String> targets = json.getAsJsonArray("targets").asList().stream()
                    .map(JsonElement::getAsString).collect(Collectors.toSet());
            assertFalse(targets.isEmpty());

            for (JsonElement element : json.getAsJsonArray("passes")) {
                JsonObject pass = element.getAsJsonObject();
                String name = pass.get("name").getAsString();
                if (!VANILLA_PROGRAMS.contains(name)) {
                    String path = name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
                    assertTrue(RenderSources.resourceExists(PROGRAM + "/" + path + ".json"),
                            config.getFileName() + ": missing program " + name);
                }
                for (String targetField : List.of("intarget", "outtarget")) {
                    String target = pass.get(targetField).getAsString();
                    assertTrue(targets.contains(target) || target.equals("minecraft:main"),
                            config.getFileName() + ": undeclared target " + target);
                }
            }
        }
    }

    @Test
    void modProgramShadersHaveMatchingGlslFiles() {
        for (Path config : RenderSources.jsonFiles(PROGRAM)) {
            JsonObject json = RenderSources.json(PROGRAM + "/" + config.getFileName());
            String name = config.getFileName().toString().replace(".json", "");
            for (String stage : List.of("vertex", "fragment")) {
                String reference = json.get(stage).getAsString();
                String local = reference.contains(":")
                        ? reference.substring(reference.indexOf(':') + 1) : reference;
                if (VANILLA_PROGRAMS.contains(local)) {
                    continue;
                }
                String extension = stage.equals("vertex") ? ".vsh" : ".fsh";
                assertTrue(RenderSources.resourceExists("assets/arcaneresearch/shaders/program/" + local + extension),
                        name + ": missing " + stage + " source " + local + extension);
            }
        }
    }

    @Test
    void everyPostShaderFileIsReferencedFromClientCode() {
        Set<String> referenced = new HashSet<>();
        Pattern postShader = Pattern.compile("\"(shaders/post/[\\w.]+\\.json)\"");
        for (String file : List.of("arcaneresearch/client/lib/events/TCShaderHandler.java",
                "arcaneresearch/client/lib/events/PortalWarpFx.java")) {
            Matcher matcher = postShader.matcher(RenderSources.compact(RenderSources.source(file)));
            while (matcher.find()) {
                referenced.add(matcher.group(1));
            }
        }
        Set<String> existing = RenderSources.jsonFiles(POST).stream()
                .map(path -> "shaders/post/" + path.getFileName())
                .collect(Collectors.toSet());

        assertEquals(existing, referenced);
    }

    @Test
    void uniformsSetFromJavaAreDeclaredInShaderJsons() {
        Pattern usedPattern = Pattern.compile("(?:safeGetUniform|getUniform)\\(\"([\\w]+)\"\\)");
        Set<String> used = new HashSet<>();
        for (Path file : RenderSources.javaFiles("arcaneresearch/client")) {
            Matcher matcher = usedPattern.matcher(RenderSources.compact(
                    RenderSources.source(relativeToJava(file))));
            while (matcher.find()) {
                used.add(matcher.group(1));
            }
        }
        Set<String> declared = new HashSet<>();
        for (Path config : allShaderConfigs()) {
            JsonArray uniforms = RenderSources.json(relativeToResources(config)).getAsJsonArray("uniforms");
            if (uniforms == null) {
                continue;
            }
            for (JsonElement element : uniforms) {
                declared.add(element.getAsJsonObject().get("name").getAsString());
            }
        }

        assertTrue(used.size() >= 6, "uniform scan found too few names: " + used);
        Set<String> unknown = new HashSet<>(used);
        unknown.removeAll(declared);
        assertTrue(unknown.isEmpty(), "Java sets undeclared uniforms: " + unknown);
    }

    private static List<Path> allShaderConfigs() {
        List<Path> result = new ArrayList<>(RenderSources.jsonFiles(CORE));
        result.addAll(RenderSources.jsonFiles(PROGRAM));
        return result;
    }

    private static String relativeToResources(Path file) {
        String raw = file.toString().replace('\\', '/');
        int index = raw.indexOf("/assets/arcaneresearch/shaders/");
        return "assets/arcaneresearch/shaders/" + raw.substring(index + "/assets/arcaneresearch/shaders/".length());
    }

    private static String relativeToJava(Path file) {
        return file.toString().replace('\\', '/').substring(
                file.toString().replace('\\', '/').indexOf("/arcaneresearch/") + 1);
    }
}
