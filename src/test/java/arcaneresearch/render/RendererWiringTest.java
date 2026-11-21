package arcaneresearch.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.render.RenderSources.Registration;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Рендеры: связанность регистраций и ресурсов")
class RendererWiringTest {

    /** Generic base class, never registered on its own. */
    private static final Set<String> BASE_RENDERERS = Set.of("ArcaneresearchProjectileRenderer");

    @Test
    void entityRendererAssignmentsAreUnambiguous() {
        Map<String, Set<String>> byEntity = RenderSources.entityRenderers().stream().collect(
                Collectors.groupingBy(Registration::field,
                        Collectors.mapping(Registration::renderer, Collectors.toSet())));

        assertTrue(byEntity.size() > 50, "too few entity renderers: " + byEntity.size());
        Set<String> conflicts = byEntity.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        assertTrue(conflicts.isEmpty(), "entities registered with conflicting renderers: " + conflicts);
    }

    @Test
    void blockEntityRendererAssignmentsAreUnambiguous() {
        Map<String, Set<String>> byTile = RenderSources.blockEntityRenderers().stream().collect(
                Collectors.groupingBy(Registration::field,
                        Collectors.mapping(Registration::renderer, Collectors.toSet())));
        Set<String> conflicts = byTile.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        assertTrue(byTile.size() > 30, "too few block entity renderers: " + byTile.size());
        assertTrue(conflicts.isEmpty(), "block entities with conflicting renderers: " + conflicts);
    }

    @Test
    void everyRegisteredEntityExists() {
        Set<String> known = RenderSources.registeredEntityFields();
        Set<String> missing = RenderSources.entityRenderers().stream()
                .map(Registration::field)
                .filter(field -> !known.contains(field))
                .collect(Collectors.toSet());

        assertTrue(missing.isEmpty(), "unregistered entities with renderers: " + missing);
    }

    @Test
    void everyRegisteredBlockEntityExists() {
        Set<String> known = RenderSources.registeredBlockEntityFields();
        Set<String> missing = RenderSources.blockEntityRenderers().stream()
                .map(Registration::field)
                .filter(field -> !field.equals("TILE") && !known.contains(field))
                .collect(Collectors.toSet());

        assertTrue(missing.isEmpty(), "unregistered block entities with renderers: " + missing);
    }

    @Test
    void everyRegisteredRendererClassExists() {
        Set<String> missing = new HashSet<>();
        for (Registration registration : RenderSources.entityRenderers()) {
            checkRendererClass(registration.renderer(), missing);
        }
        for (Registration registration : RenderSources.blockEntityRenderers()) {
            checkRendererClass(registration.renderer(), missing);
        }
        assertTrue(missing.isEmpty(), "missing renderer classes: " + missing);
    }

    @Test
    void registeredRenderersReferenceExistingModTextures() {
        Pattern modTexture = Pattern.compile(
                "fromNamespaceAndPath\\((?:Arcaneresearch\\.)?MODID\\s*,\\s*\"(textures/[\\w/.-]+\\.png)\"");
        Set<String> missing = new HashSet<>();

        for (Registration registration : RenderSources.entityRenderers()) {
            checkTextures(registration.renderer(), modTexture, missing);
        }
        for (Registration registration : RenderSources.blockEntityRenderers()) {
            checkTextures(registration.renderer(), modTexture, missing);
        }
        assertTrue(missing.isEmpty(), "missing mod textures: " + missing);
    }

    @Test
    void everyConcreteRendererIsRegistered() {
        Set<String> registered = RenderSources.entityRenderers().stream()
                .map(Registration::renderer).collect(Collectors.toSet());
        registered.addAll(RenderSources.blockEntityRenderers().stream()
                .map(Registration::renderer).toList());
        Set<String> orphans = new HashSet<>();

        for (String directory : new String[]{"arcaneresearch/client/renderers/entity",
                "arcaneresearch/client/renderers/tile"}) {
            for (Path file : RenderSources.javaFiles(directory)) {
                String name = file.getFileName().toString();
                if (!name.endsWith("Renderer.java")) {
                    continue;
                }
                String simpleName = name.substring(0, name.length() - ".java".length());
                if (!registered.contains(simpleName) && !BASE_RENDERERS.contains(simpleName)) {
                    orphans.add(simpleName);
                }
            }
        }
        assertTrue(orphans.isEmpty(), "renderer classes never registered: " + orphans);
    }

    private static void checkRendererClass(String renderer, Set<String> missing) {
        if (renderer.equals("ThrownItemRenderer")) {
            return; // vanilla
        }
        if (RenderSources.rendererSource(renderer) == null) {
            missing.add(renderer);
        }
    }

    private static void checkTextures(String renderer, Pattern pattern, Set<String> missing) {
        String source = RenderSources.rendererSource(renderer);
        if (source == null) {
            return;
        }
        Matcher matcher = pattern.matcher(RenderSources.compact(source));
        while (matcher.find()) {
            String relative = "assets/arcaneresearch/" + matcher.group(1);
            if (!RenderSources.resourceExists(relative)) {
                missing.add(renderer + " -> " + matcher.group(1));
            }
        }
    }
}
