package arcaneresearch.client.renderers;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Jar: coordinate frame of the item label")
class JarLabelPoseTest {
    private static final float EPS = 1.0E-5F;

    @Test
    void labelIsNotCentredTwice() {
        // ItemRenderer.render applies translate(-0.5,-0.5,-0.5) before BEWLR.
        Matrix4f itemPose = new Matrix4f().translate(-0.5F, -0.5F, -0.5F);

        // Baked body face centre (0.5, 0.4, 0.8125) lands at the centred point.
        Vector3f body = new Vector3f(0.5F, 0.4F, 0.8125F).mulPosition(itemPose);
        assertEquals(0.0F, body.x, EPS);
        assertEquals(-0.1F, body.y, EPS);
        assertEquals(0.3125F, body.z, EPS);

        // The renderer must undo that centring before feeding its centred
        // label coordinates; without it the label sits half a block away.
        Matrix4f labelPose = new Matrix4f(itemPose).translate(0.5F, 0.5F, 0.5F);
        Vector3f label = new Vector3f(0.0F, -0.1F, 0.315F).mulPosition(labelPose);
        assertEquals(0.0F, label.x, EPS);
        assertEquals(-0.1F, label.y, EPS);
        assertEquals(0.315F, label.z, EPS);
        assertEquals(0.0025F, label.z - body.z, EPS);
    }
}
