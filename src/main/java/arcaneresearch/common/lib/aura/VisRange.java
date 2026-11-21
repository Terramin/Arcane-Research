package arcaneresearch.common.lib.aura;

/** Pure distance check for aura node visibility. */
public final class VisRange {

    private VisRange() {
    }

    /**
     * @return squared distance when the two nodes are in range, {@code -1} otherwise
     */
    public static float inRange(Object dimension1, Object dimension2, double distanceSquared, int range) {
        if (dimension1 == null || dimension2 == null || !dimension1.equals(dimension2)) {
            return -1.0f;
        }
        return distanceSquared > (double) range * range ? -1.0f : (float) distanceSquared;
    }
}
