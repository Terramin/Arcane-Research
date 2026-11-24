package arcaneresearch.client.renderers.item;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Jar: label placement on the item model")
class JarLabelPlacementTest {
    private static final float EPS = 1.0E-6F;

    private static void assertPlacement(JarLabelPlacement.Placement actual,
                                        float x, float y, float z, float half) {
        assertEquals(x, actual.x(), EPS, "x");
        assertEquals(y, actual.y(), EPS, "y");
        assertEquals(z, actual.z(), EPS, "z");
        assertEquals(half, actual.half(), EPS, "half");
    }

    @Test
    void fallbackUsesTheBlockRendererValues() {
        assertPlacement(JarLabelPlacement.fallback(false), 0.0F, -0.1F, 0.315F, 0.25F);
        assertPlacement(JarLabelPlacement.fallback(true), 0.0F, -0.1F, -0.315F, 0.25F);
    }

    @Test
    void bakedBodyFaceGivesTheSameSpotAsTheBlock() {
        // Body element [3,0,3]..[13,12,13] -> 0.1875..0.8125 in block coords.
        JarLabelPlacement.Face south = new JarLabelPlacement.Face(
                0.1875F, 0.8125F, 0.0F, 0.75F, 0.8125F, 0.8125F);
        assertPlacement(JarLabelPlacement.fromFace(south, false),
                0.0F, -0.1F, 0.315F, 0.25F);

        JarLabelPlacement.Face north = new JarLabelPlacement.Face(
                0.1875F, 0.8125F, 0.0F, 0.75F, 0.1875F, 0.1875F);
        assertPlacement(JarLabelPlacement.fromFace(north, true),
                0.0F, -0.1F, -0.315F, 0.25F);
    }

    @Test
    void narrowFaceClampsTheLabelWidth() {
        JarLabelPlacement.Face narrow = new JarLabelPlacement.Face(
                0.3F, 0.7F, 0.0F, 0.75F, 0.8125F, 0.8125F);
        assertPlacement(JarLabelPlacement.fromFace(narrow, false),
                0.0F, -0.1F, 0.315F, 0.19F);
    }

    @Test
    void degenerateFaceIsRejected() {
        JarLabelPlacement.Face flat = new JarLabelPlacement.Face(
                0.5F, 0.5F, 0.0F, 0.75F, 0.8125F, 0.8125F);
        assertNull(JarLabelPlacement.fromFace(flat, false));
    }
}
