package arcaneresearch.client.renderers.item;

/**
 * Pure geometry of the jar label in the centred item frame (-0.5..0.5).
 * Kept free of Minecraft types so the placement can be unit tested.
 */
public final class JarLabelPlacement {
    public static final float BLOCK_Y = -0.1F;
    public static final float BLOCK_FORWARD = 0.315F;
    public static final float BLOCK_HALF_SIZE = 0.25F;

    /** The block label sits 0.025 above the centre of its 0..0.75 body face. */
    private static final float VERTICAL_BIAS = 0.025F;
    /** Just outside the glass surface. */
    private static final float SURFACE_OFFSET = 0.0025F;
    private static final float WIDTH_MARGIN = 0.01F;

    private JarLabelPlacement() {
    }

    /** Baked quad bounds (0..1 block coordinates) of a flat jar side. */
    public record Face(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
    }

    /** Label centre and half-size in the centred frame. */
    public record Placement(float x, float y, float z, float half) {
    }

    /** The placement the block renderer uses, expressed in the +Z frame. */
    public static Placement fallback(boolean north) {
        return new Placement(0.0F, BLOCK_Y,
                north ? -BLOCK_FORWARD : BLOCK_FORWARD, BLOCK_HALF_SIZE);
    }

    /**
     * Places the label on a baked body face. Returns {@code null} when the face
     * is too narrow to hold a label.
     */
    public static Placement fromFace(Face face, boolean north) {
        float plane = north ? face.minZ() : face.maxZ();
        float cx = (face.minX() + face.maxX()) * 0.5F - 0.5F;
        float cy = (face.minY() + face.maxY()) * 0.5F - 0.5F + VERTICAL_BIAS;
        float cz = plane - 0.5F + (north ? -SURFACE_OFFSET : SURFACE_OFFSET);
        float half = Math.min(BLOCK_HALF_SIZE, (face.maxX() - face.minX()) * 0.5F - WIDTH_MARGIN);
        return half > 0.0F ? new Placement(cx, cy, cz, half) : null;
    }
}
