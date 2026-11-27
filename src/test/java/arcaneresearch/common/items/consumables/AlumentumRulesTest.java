package arcaneresearch.common.items.consumables;

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

@DisplayName("Алюментум: правила и регрессии")
class AlumentumRulesTest {

    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", Path.of("src", "main", "resources").toString()))
            .getParent().resolve("java");

    private static String source(String relativePath) {
        try {
            return Files.readString(JAVA.resolve(relativePath), StandardCharsets.UTF_8)
                    .replaceAll("\\s+", "");
        } catch (IOException e) {
            return fail("Cannot read source " + relativePath, e);
        }
    }

    @Test
    @DisplayName("tuning constants match the original item and projectile")
    void constants() {
        assertEquals(64, AlumentumRules.STACK_SIZE);
        assertEquals(4800, AlumentumRules.BURN_TIME);
        assertEquals(2.0f, AlumentumRules.THROW_POWER);
        assertEquals(0.4f, AlumentumRules.THROW_INACCURACY);
        assertEquals(-5.0f, AlumentumRules.THROW_ANGLE);
        assertEquals(0.75f, AlumentumRules.VELOCITY);
        assertEquals(0.03f, AlumentumRules.GRAVITY);
        assertEquals(1.1f, AlumentumRules.EXPLOSION_RADIUS);
        assertTrue(AlumentumRules.EXPLOSION_FLAMING);
        assertEquals(0.3f, AlumentumRules.SOUND_VOLUME);
        assertEquals(3, AlumentumRules.FX_SAMPLES);
        assertEquals(0.0125f, AlumentumRules.FX_JITTER);
    }

    @Test
    @DisplayName("throw sound pitch is finite and decreases with the random value")
    void soundPitch() {
        assertEquals(0.5f, AlumentumRules.soundPitch(0.0f), 1e-6f);
        assertTrue(AlumentumRules.soundPitch(0.5f) < AlumentumRules.soundPitch(0.0f));
        assertTrue(AlumentumRules.soundPitch(1.0f) > 0.0f);
        assertTrue(Float.isNaN(AlumentumRules.soundPitch(Float.NaN)));
    }

    @Test
    @DisplayName("trail samples interpolate evenly behind the projectile")
    void trailSamples() {
        assertEquals(0.0, AlumentumRules.trailCoefficient(0), 1e-9);
        assertEquals(1.0 / 3.0, AlumentumRules.trailCoefficient(1), 1e-9);
        assertEquals(2.0 / 3.0, AlumentumRules.trailCoefficient(2), 1e-9);
        assertEquals(-0.00625f, AlumentumRules.trailJitter(0.0f), 1e-6f);
        assertEquals(0.0f, AlumentumRules.trailJitter(0.5f), 1e-6f);
        assertEquals(0.00625f, AlumentumRules.trailJitter(1.0f), 1e-6f);
    }

    @Test
    @DisplayName("explosion keeps the flaming flag from the original")
    void explosionIsFlaming() {
        String entity = source("arcaneresearch/common/entities/projectile/EntityAlumentum.java");
        assertTrue(entity.contains("AlumentumRules.EXPLOSION_FLAMING"));
        assertTrue(entity.contains("Level.ExplosionInteraction.BLOCK"));
        assertFalse(entity.contains("explode(this,getX(),getY(),getZ(),1.1f,Level.ExplosionInteraction.BLOCK)"),
                "the 6-argument overload silently disables fire");
    }

    @Test
    @DisplayName("item delegates tuning to the rules class")
    void itemUsesRules() {
        String item = source("arcaneresearch/common/items/consumables/ItemAlumentum.java");
        assertTrue(item.contains("AlumentumRules.STACK_SIZE"));
        assertTrue(item.contains("AlumentumRules.BURN_TIME"));
        assertTrue(item.contains("AlumentumRules.soundPitch("));
        assertTrue(item.contains("AlumentumRules.THROW_ANGLE"));
    }
}
