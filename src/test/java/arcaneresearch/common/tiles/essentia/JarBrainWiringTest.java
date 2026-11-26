package arcaneresearch.common.tiles.essentia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Source-level regression guards for the jar-brain bugs that were found during
 * the port review. No Minecraft classes are loaded.
 */
@DisplayName("Brain jar: regression wiring")
class JarBrainWiringTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()))
            .getParent().resolve("java");

    private static String source(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return fail("Cannot read source " + relativePath, e);
        }
    }

    private static final String TILE = "arcaneresearch/common/tiles/essentia/TileJarBrain.java";
    private static final String RENDER = "arcaneresearch/client/renderers/tile/JarBrainRenderer.java";
    private static final String BLOCK = "arcaneresearch/common/blocks/essentia/BlockJar.java";

    @Test
    @DisplayName("renderer converts radians to degrees before rotating")
    void rendererConvertsRadians() {
        String source = source(RENDER).replaceAll("\\s+", "");
        assertTrue(source.contains("rotationDegrees((float)Math.toDegrees(rotation))"),
                "brainRotation is in radians; feeding it to rotationDegrees() was the P0 bug");
        assertFalse(source.contains("rotationDegrees(rotation)"));
    }

    @Test
    @DisplayName("client tick pulls orbs and decrements eatDelay")
    void clientTickMatchesOriginalUpdate() {
        String source = source(TILE).replaceAll("\\s+", "");
        assertTrue(source.contains("pullOrb(pos,orb)"), "client-side orb pull must stay");
        assertTrue(source.contains("tile.eatDelay--"), "eatDelay must tick down on the client too");
    }

    @Test
    @DisplayName("sigh timing delegates to the testable rules")
    void sighUsesRules() {
        String source = source(TILE).replaceAll("\\s+", "");
        assertTrue(source.contains("JarBrainRules.nextSigh("));
        assertFalse(source.contains("nextInt(25000)"));
        assertFalse(source.contains("1500L"));
    }

    @Test
    @DisplayName("pull/eat boxes are cached, not rebuilt every tick")
    void boxesAreCached() {
        String source = source(TILE).replaceAll("\\s+", "");
        int count = 0;
        int index = 0;
        while ((index = source.indexOf("newAABB(", index)) >= 0) {
            count++;
            index += "newAABB(".length();
        }
        assertEquals(3, count, "exactly two cached boxes plus one shared getter initialization expected");
        assertTrue(source.contains("if(box==null)"));
        assertTrue(source.contains("if(eat==null)"));
    }

    @Test
    @DisplayName("jar brain no longer duplicates magic numbers from the original")
    void calculationsAreCentralized() {
        String tile = source(TILE).replaceAll("\\s+", "");
        assertFalse(tile.contains("0.04f"));
        assertFalse(tile.contains("0.01f"));
        assertFalse(tile.contains("1.0f/14.0f"));
        assertFalse(tile.contains("inflate(8"));

        String block = source(BLOCK).replaceAll("\\s+", "");
        assertTrue(block.contains("JarBrainRules.orbValueCap("));
        assertTrue(block.contains("JarBrainRules.splitOrbValues("));
        assertTrue(block.contains("JarBrainRules.comparatorOutput("));
        assertFalse(block.contains("Math.min(brain.xp+1,64)"));
    }
}
