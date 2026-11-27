package arcaneresearch.common.tiles.misc;

/**
 * Pure tuning values for nitor flame and core particles. No Minecraft types are
 * referenced so the numbers can be verified by unit tests.
 */
public final class NitorRules {

    /** Ticks between core particle bursts. */
    public static final int CORE_INTERVAL = 10;
    /** Centre offset of the flame particles. */
    public static final double FLAME_Y = 0.45;
    /** Centre offset of the core particle. */
    public static final double CORE_Y = 0.49;
    /** Gaussian spread of the flame position. */
    public static final double FLAME_POS_JITTER = 0.025;
    /** Gaussian spread of the horizontal flame velocity. */
    public static final double FLAME_VX_JITTER = 0.0025;
    /** Upward speed of a flame particle. */
    public static final double FLAME_VY_SPEED = 0.06;
    /** Minimum lifetime of a flame particle. */
    public static final int FLAME_AGE = 10;
    /** Extra random lifetime of a flame particle. */
    public static final int FLAME_AGE_JITTER = 5;
    /** Lifetime of a core particle. */
    public static final int CORE_AGE = 10;
    /** Flame particle alpha. */
    public static final float FLAME_ALPHA = 0.66f;
    /** Collision box corners, matching the original AxisAlignedBB. */
    public static final double SHAPE_MIN = 0.33000001311302185;
    public static final double SHAPE_MAX = 0.6600000262260437;

    private NitorRules() {
    }

    /** Particle render categories used by the nitor flame and core. */
    public enum RenderKind {
        ADDITIVE,
        NORMAL,
        ADDITIVE_NO_DEPTH,
        NORMAL_NO_DEPTH
    }

    /**
     * Selects a particle render type. Submerged particles marked as fluid-visible skip
     * the depth test so they remain visible when the viewer is in air above water.
     *
     * @param layer particle layer from {@code FXGeneric}
     * @param submerged whether the particle is currently inside a fluid
     * @param visibleInFluid whether this particle opted into fluid visibility
     * @return the render category to use
     */
    public static RenderKind renderKind(int layer, boolean submerged, boolean visibleInFluid) {
        if (visibleInFluid && submerged) {
            return layer == 1 ? RenderKind.NORMAL_NO_DEPTH : RenderKind.ADDITIVE_NO_DEPTH;
        }
        return switch (layer) {
            case 0, 4 -> RenderKind.ADDITIVE;
            case 2 -> RenderKind.ADDITIVE_NO_DEPTH;
            case 3 -> RenderKind.NORMAL_NO_DEPTH;
            default -> RenderKind.NORMAL;
        };
    }

    /** @return whether the core particle should be drawn on this tick counter */
    public static boolean shouldDrawCore(int count) {
        return count % CORE_INTERVAL == 0;
    }

    /** Gaussian position jitter of a flame particle. */
    public static double flameOffset(double gaussian) {
        return gaussian * FLAME_POS_JITTER;
    }

    /** Gaussian horizontal velocity of a flame particle. */
    public static double flameVelocity(double gaussian) {
        return gaussian * FLAME_VX_JITTER;
    }

    /** Upward velocity of a flame particle, uniform in [0, 0.06). */
    public static double flameRise(float random) {
        return random * FLAME_VY_SPEED;
    }
}
