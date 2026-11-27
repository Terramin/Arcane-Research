package arcaneresearch.common.items.consumables;

/**
 * Pure tuning values for alumentum. Kept separate from Minecraft classes so the
 * numbers can be verified by unit tests.
 */
public final class AlumentumRules {

    public static final int STACK_SIZE = 64;
    /** Burn time as a furnace fuel, in ticks. */
    public static final int BURN_TIME = 4800;
    /** Throw velocity passed to shootFromRotation (overridden to 0.75 in the entity). */
    public static final float THROW_POWER = 2.0f;
    public static final float THROW_INACCURACY = 0.4f;
    public static final float THROW_ANGLE = -5.0f;
    /** Actual projectile velocity, forced by EntityAlumentum.shoot. */
    public static final float VELOCITY = 0.75f;
    public static final float GRAVITY = 0.03f;
    public static final float EXPLOSION_RADIUS = 1.1f;
    public static final boolean EXPLOSION_FLAMING = true;
    public static final float SOUND_VOLUME = 0.3f;
    public static final float SOUND_PITCH_BASE = 0.4f;
    public static final float SOUND_PITCH_SPREAD = 0.4f;
    public static final int FX_SAMPLES = 3;
    public static final float FX_JITTER = 0.0125f;

    private AlumentumRules() {
    }

    /** Pitch formula used for the throw sound, in {@code [0.8, 1.2)}. */
    public static float soundPitch(float random) {
        return SOUND_PITCH_BASE / (random * SOUND_PITCH_SPREAD + 0.8f);
    }

    /** Interpolation weight for the n-th trail sample behind the projectile. */
    public static double trailCoefficient(int sample) {
        return sample / (double) FX_SAMPLES;
    }

    /** Random velocity jitter of a trail particle, in {@code [-0.00625, 0.00625)}. */
    public static float trailJitter(float random) {
        return FX_JITTER * (random - 0.5f);
    }
}
