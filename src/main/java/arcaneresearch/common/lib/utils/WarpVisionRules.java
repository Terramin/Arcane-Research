package arcaneresearch.common.lib.utils;

/**
 * Pure cone check used by the Death Gaze warp effect.
 *
 * <p>The original passes {@code 0.75F} as an <em>aperture in radians</em>: the
 * cone half-angle is 0.375 rad and an additional round cap limits the target
 * to the effect range. Treating 0.75 as a cosine would widen the cone from
 * roughly 43° to 82°.
 */
public final class WarpVisionRules {

    private WarpVisionRules() {
    }

    /**
     * Tests whether {@code target} lies inside the cone with apex {@code apex}
     * and axis ending at {@code axisEnd}.
     *
     * @param target target point (x, y, z)
     * @param apex cone apex, normally the player's eyes
     * @param axisEnd point one range away along the look direction
     * @param aperture full aperture in radians
     * @return true when the point is inside both the infinite cone and its round cap
     */
    public static boolean isLyingInCone(double[] target, double[] apex, double[] axisEnd,
                                        float aperture) {
        double[] apexToTarget = difference(apex, target);
        double[] axis = difference(apex, axisEnd);
        double axisLength = magnitude(axis);
        double targetLength = magnitude(apexToTarget);
        if (axisLength <= 0.0 || targetLength <= 0.0) {
            return false;
        }
        double dot = dotProduct(apexToTarget, axis);
        double cosine = dot / targetLength / axisLength;
        if (cosine <= Math.cos(aperture / 2.0)) {
            return false;
        }
        // Round cap: the projection onto the axis must be shorter than the range.
        return dot / axisLength < axisLength;
    }

    private static double[] difference(double[] a, double[] b) {
        return new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    }

    private static double dotProduct(double[] a, double[] b) {
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    private static double magnitude(double[] a) {
        return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
    }
}
