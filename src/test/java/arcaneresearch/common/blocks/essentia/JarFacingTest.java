package arcaneresearch.common.blocks.essentia;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Jar: ориентация этикетки по сторонам света")
class JarFacingTest {

    @Test
    void yawPutsLabelOnTheSideFacingThePlayer() {
        assertEquals(JarFacing.NORTH, JarFacing.fromYaw(0.0f));
        assertEquals(JarFacing.EAST, JarFacing.fromYaw(90.0f));
        assertEquals(JarFacing.SOUTH, JarFacing.fromYaw(180.0f));
        assertEquals(JarFacing.WEST, JarFacing.fromYaw(270.0f));
        assertEquals(JarFacing.WEST, JarFacing.fromYaw(-90.0f));
    }

    @Test
    void yawRoundsToNearestSide() {
        assertEquals(JarFacing.NORTH, JarFacing.fromYaw(44.0f));
        assertEquals(JarFacing.EAST, JarFacing.fromYaw(46.0f));
    }

    @Test
    void labelQuadIsPlacedOnTheStoredFacing() {
        // The label quad is drawn on +Z, so 0 deg = SOUTH (no global X flip).
        assertEquals(0, JarFacing.labelYawDegrees(JarFacing.SOUTH));
        assertEquals(180, JarFacing.labelYawDegrees(JarFacing.NORTH));
        assertEquals(270, JarFacing.labelYawDegrees(JarFacing.WEST));
        assertEquals(90, JarFacing.labelYawDegrees(JarFacing.EAST));
    }
}
