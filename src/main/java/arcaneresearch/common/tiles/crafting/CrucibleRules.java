package arcaneresearch.common.tiles.crafting;

/**
 * Pure rules for the alchemy crucible. No Minecraft types are referenced, so the
 * heat curve, water level and spill timing can be verified by unit tests.
 */
public final class CrucibleRules {

    /** Maximum heat the crucible can reach. */
    public static final int HEAT_MAX = 200;
    /** Heat at which the crucible starts boiling and can smelt items. */
    public static final int BOIL_HEAT = 151;
    /** Cooling step that triggers a sync. */
    public static final int COOL_SYNC_HEAT = 149;
    /** Water drained by one successful craft. */
    public static final int WATER_PER_CRAFT = 50;
    /** Vis limit before the crucible starts spilling. */
    public static final int ASPECT_LIMIT = 500;
    /** Water tank size. */
    public static final int TANK_CAPACITY = 1000;
    /** Ticks between scheduled spills. */
    public static final int SPILL_INTERVAL = 100;
    /** Counter value after a successful craft. */
    public static final long COUNTER_AFTER_CRAFT = -250L;
    /** Counter value after dissolving an item. */
    public static final long COUNTER_AFTER_ABSORB = -150L;
    /** Counter value of a freshly created crucible. */
    public static final long COUNTER_INITIAL = -100L;

    private CrucibleRules() {
    }

    /** @return heat change for one tick with the given water/heat state */
    public static int heatDelta(boolean hasWater, boolean heatSource, int heat) {
        if (hasWater) {
            if (heatSource && heat < HEAT_MAX) {
                return 1;
            }
            if (!heatSource && heat > 0) {
                return -1;
            }
        } else if (heat > 0) {
            return -1;
        }
        return 0;
    }

    /**
     * @return whether crossing a visual threshold requires a sync. Cooling to 149
     * only syncs when the crucible has water and no heat source, as in the original.
     */
    public static boolean shouldSyncHeat(int previous, int heat, boolean hasWater, boolean heatSource) {
        return (previous < BOIL_HEAT && heat >= BOIL_HEAT)
                || (hasWater && !heatSource && heat == COOL_SYNC_HEAT);
    }

    /** @return whether the crucible is hot enough to smelt */
    public static boolean isHeated(int heat) {
        return heat >= BOIL_HEAT;
    }

    /** @return whether the aspect overflow requires an immediate spill */
    public static boolean shouldSpill(int vis) {
        return vis > ASPECT_LIMIT;
    }

    /** Original fluid-height formula, including its 1.001/0.9999 quirks. */
    public static float fluidHeight(int water, int vis) {
        float base = 0.3f + 0.5f * Math.min(Math.max(water, 0), TANK_CAPACITY) / (float) TANK_CAPACITY;
        float out = base + Math.max(vis, 0) / (float) ASPECT_LIMIT * (1.0f - base);
        if (out > 1.0f) {
            out = 1.001f;
        }
        if (out == 1.0f) {
            out = 0.9999f;
        }
        return out;
    }

    /** Pollution added to the aura when one aspect is spilled. */
    public static float spillPollution(boolean flux) {
        return flux ? 1.0f : 0.25f;
    }
}
