package arcaneresearch.common.lib.crafting;

/**
 * Pure stability rules for the infusion matrix.
 *
 * <p>No Minecraft types are referenced: the tile entity owns the world state and
 * delegates only these calculations here so they can be verified by unit tests.</p>
 */
public final class InfusionStabilityRules {

    public static final int MIN_STABILITY = -100;
    public static final int INSTABILITY_ROLL_BOUND = 1500;
    public static final int INSTABILITY_EVENT_COUNT = 24;

    /** Stability tiers, ordered from most to least stable. */
    public enum Tier {
        VERY_STABLE(5.0f),
        STABLE(6.0f),
        UNSTABLE(7.0f),
        VERY_UNSTABLE(8.0f);

        private final float divisor;

        Tier(float divisor) {
            this.divisor = divisor;
        }

        /** Divisor applied to the recipe instability to get the loss per cycle. */
        public float divisor() {
            return divisor;
        }
    }

    /** Outcomes of the 24-sided instability roll. */
    public enum Outcome {
        EJECT_0, WARP, ZAP, ZAP_ALL, EJECT_1, EJECT_2, EJECT_3, EJECT_4,
        HARM, EJECT_5, HARM_ALL, EXPLODE
    }

    private InfusionStabilityRules() {
    }

    public static Tier tier(float stability, int stabilityCap) {
        if (stability > (float) stabilityCap / 2) {
            return Tier.VERY_STABLE;
        }
        if (stability >= 0.0f) {
            return Tier.STABLE;
        }
        if (stability > -25.0f) {
            return Tier.UNSTABLE;
        }
        return Tier.VERY_UNSTABLE;
    }

    public static float lossPerCycle(int recipeInstability, float stability, int stabilityCap) {
        return recipeInstability / tier(stability, stabilityCap).divisor();
    }

    /** Applies one crafting cycle and clamps the result to the legal range. */
    public static float nextStability(float stability, float loss, float replenish, int stabilityCap) {
        return Math.max(MIN_STABILITY, Math.min(stabilityCap, stability - loss + replenish));
    }

    /** The tile rolls {@code 0..1499}; a negative stability makes low rolls dangerous. */
    public static boolean shouldDestabilize(float stability, int roll) {
        return stability < 0.0f && roll >= 0 && roll < INSTABILITY_ROLL_BOUND
                && roll <= Math.abs(stability);
    }

    public static Outcome outcome(int roll) {
        if (roll < 0 || roll >= INSTABILITY_EVENT_COUNT) {
            throw new IllegalArgumentException("instability roll out of range: " + roll);
        }
        return switch (roll) {
            case 0, 1, 2, 3 -> Outcome.EJECT_0;
            case 4, 5, 6 -> Outcome.WARP;
            case 7, 8, 9 -> Outcome.ZAP;
            case 10, 11 -> Outcome.ZAP_ALL;
            case 12, 13 -> Outcome.EJECT_1;
            case 14, 15 -> Outcome.EJECT_2;
            case 16 -> Outcome.EJECT_3;
            case 17 -> Outcome.EJECT_4;
            case 18, 19 -> Outcome.HARM;
            case 20, 21 -> Outcome.EJECT_5;
            case 22 -> Outcome.HARM_ALL;
            case 23 -> Outcome.EXPLODE;
            default -> throw new IllegalStateException("unreachable roll: " + roll);
        };
    }
}
