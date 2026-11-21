package arcaneresearch.common.lib.crafting;

/** Pure symmetry/diminishing-return maths for infusion matrix surroundings. */
public final class InfusionStabilizationRules {

    private static final double DIMINISHING_FACTOR = 0.75;

    /** Effect of a stabilizer when {@code previousCount} of its type were already counted. */
    public static float diminishingReturns(float base, int previousCount) {
        return previousCount > 0
                ? (float) (base * Math.pow(DIMINISHING_FACTOR, previousCount))
                : base;
    }

    public record PairResult(float delta, boolean problem) {
    }

    /**
     * Resolves one mirrored pair of stabilizing blocks.
     *
     * @param matched         whether the pair is symmetric (checked on raw amounts by the caller,
     *                        before diminishing returns are applied to the first block)
     * @param customPenalty   whether the stabilizer reports an explicit symmetry penalty
     */
    public static PairResult pair(float first, float second, boolean matched,
                                  boolean customPenalty, float customPenaltyValue) {
        if (matched && !customPenalty) {
            return new PairResult(first, false);
        }
        if (customPenalty) {
            return new PairResult(-customPenaltyValue, true);
        }
        return new PairResult(-Math.max(first, second), true);
    }

    private InfusionStabilizationRules() {
    }
}
