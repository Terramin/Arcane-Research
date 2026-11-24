package arcaneresearch.client.renderers;

/** Shared position of the thaumometer standing on the deconstruction table. */
public final class DeconstructionTablePlacement {
    public static final float SCALE = 0.640625F;
    public static final float OFFSET = SCALE * 0.35F / 2.0F;
    public static final float X = 0.5F + OFFSET;
    public static final float Y = 1.024F + OFFSET;
    public static final float Z = 0.5F + OFFSET;

    private DeconstructionTablePlacement() {
    }
}
