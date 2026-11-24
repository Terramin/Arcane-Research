package arcaneresearch.client.renderers;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Deconstruction table: thaumometer placement")
class DeconstructionTablePlacementTest {
    private static final float EPS = 1.0E-6F;

    @Test
    void thaumometerStandsAboveTheTableTop() {
        assertEquals(0.640625F, DeconstructionTablePlacement.SCALE, EPS);
        assertTrue(DeconstructionTablePlacement.Y > 1.0F);
        assertEquals(0.5F + DeconstructionTablePlacement.OFFSET, DeconstructionTablePlacement.X, EPS);
        assertEquals(0.5F + DeconstructionTablePlacement.OFFSET, DeconstructionTablePlacement.Z, EPS);
    }

    @Test
    void itemUsesTheSameCoordinatesAsTheBlock() {
        // Block renderer: the pose originates at the block corner.
        Matrix4f blockPose = new Matrix4f()
                .translate(DeconstructionTablePlacement.X, DeconstructionTablePlacement.Y,
                        DeconstructionTablePlacement.Z)
                .scale(DeconstructionTablePlacement.SCALE);
        Vector3f fromBlock = new Vector3f(0.0F, 0.0F, 0.0F).mulPosition(blockPose);

        // Item renderer: ItemRenderer applies translate(-0.5) before BEWLR,
        // which the renderer cancels before using the block coordinates.
        Matrix4f itemPose = new Matrix4f().translate(-0.5F, -0.5F, -0.5F)
                .translate(0.5F, 0.5F, 0.5F)
                .translate(DeconstructionTablePlacement.X, DeconstructionTablePlacement.Y,
                        DeconstructionTablePlacement.Z)
                .scale(DeconstructionTablePlacement.SCALE);
        Vector3f fromItem = new Vector3f(0.0F, 0.0F, 0.0F).mulPosition(itemPose);

        assertEquals(fromBlock.x, fromItem.x, EPS);
        assertEquals(fromBlock.y, fromItem.y, EPS);
        assertEquals(fromBlock.z, fromItem.z, EPS);
        assertEquals(DeconstructionTablePlacement.X, fromItem.x, EPS);
        assertEquals(DeconstructionTablePlacement.Y, fromItem.y, EPS);
        assertEquals(DeconstructionTablePlacement.Z, fromItem.z, EPS);
    }
}
