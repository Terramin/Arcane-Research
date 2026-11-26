package arcaneresearch.common.tiles.essentia;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Pure rules for the brain in a jar. No Minecraft types and no world state, so the
 * behaviour can be unit-tested directly. {@link TileJarBrain} and {@link
 * arcaneresearch.common.blocks.essentia.BlockJar} delegate their calculations here.
 *
 * <p>Every numeric constant is pinned by {@code JarBrainRulesTest}, so changing a
 * tuning value without updating the tests is caught immediately.</p>
 */
public final class JarBrainRules {

    /** Default XP capacity of a filled jar. */
    public static final int DEFAULT_MAX_XP = 2000;
    /** Ticks of pause after the jar is shaken; no orb can be absorbed during it. */
    public static final int EAT_DELAY = 40;
    /** Grace period before the first ambient brain sound may play. */
    public static final long INITIAL_SIGH_DELAY = 1500L;
    /** Minimum delay between two ambient brain sounds, in milliseconds. */
    public static final long SIGH_BASE_DELAY = 5000L;
    /** Random extra delay added to {@link #SIGH_BASE_DELAY}, in milliseconds. */
    public static final int SIGH_JITTER = 25000;
    /** Radius, in blocks, of the search area for experience orbs. */
    public static final double PULL_RADIUS = 8.0;
    /** Distance divisor of the orb pull falloff. */
    public static final double PULL_DIVISOR = 25.0;
    /** Horizontal component of the pull applied to an orb. */
    public static final double PULL_HORIZONTAL = 0.3;
    /** Vertical component of the pull, deliberately stronger than horizontal. */
    public static final double PULL_VERTICAL = 0.5;
    /** Half-size of the absorption box around the centre of the jar. */
    public static final double EAT_HALF_SIZE = 0.6;
    /** Fraction of the remaining angle covered by a single client tick. */
    public static final float ROTATION_SPEED = 0.04f;
    /** Radians added to the idle target direction on each client tick. */
    public static final float IDLE_DRIFT = 0.01f;
    /** Phase step of the vertical bob; one full period takes about 88 ticks. */
    public static final float BOB_STEP = 1.0f / 14.0f;
    /** Maximum vertical offset of the brain inside the jar. */
    public static final float BOB_AMPLITUDE = 0.03f;
    /** Number of comparator steps above the zero-signal state. */
    public static final int COMPARATOR_STEPS = 14;
    /** Upper bound on the XP released by a single shake (exclusive for nextInt). */
    public static final int ORB_VALUE_CAP = 64;

    private JarBrainRules() {
    }

    /**
     * Velocity delta added to an experience orb by one pull step.
     *
     * @param x horizontal X component
     * @param y vertical component (the strongest one)
     * @param z horizontal Z component
     */
    public record Pull(double x, double y, double z) {
    }

    /**
     * Inclusive bounds of an axis-aligned box.
     *
     * @param minX lower X corner; {@code minY}/{@code minZ} are analogous
     * @param maxX upper X corner; {@code maxY}/{@code maxZ} are analogous
     */
    public record Bounds(double minX, double minY, double minZ,
                         double maxX, double maxY, double maxZ) {
    }

    /** Server may look for and absorb orbs only while the jar is hungry and not paused. */
    public static boolean serverMayEat(int xp, int maxXp, int eatDelay) {
        return xp < maxXp && eatDelay == 0;
    }

    /**
     * Pull is only applied when an orb exists and the jar is allowed to eat.
     *
     * @param orbNearby whether an orb was found inside the enlarged search box
     */
    public static boolean serverMayPull(int xp, int maxXp, int eatDelay, boolean orbNearby) {
        return orbNearby && serverMayEat(xp, maxXp, eatDelay);
    }

    /**
     * The original update() only looks for a player when no orb was found and the jar
     * is not full; that also gates the occasional brain noise.
     */
    public static boolean clientMayWatch(int xp, int maxXp, boolean orbNearby) {
        return !(xp < maxXp && orbNearby);
    }

    /** Client-side pull uses the same gate as the server, keeping both views in sync. */
    public static boolean clientMayPull(int xp, int maxXp, int eatDelay, boolean orbNearby) {
        return serverMayPull(xp, maxXp, eatDelay, orbNearby);
    }

    /**
     * Clamps stored XP into {@code [0, maxXp]}. A non-positive maximum yields zero,
     * which keeps comparators and rendering free of NaN and Infinity.
     */
    public static int clampXp(int xp, int maxXp) {
        if (xp < 0) {
            return 0;
        }
        return Math.min(xp, Math.max(maxXp, 0));
    }

    /**
     * Fill ratio in {@code [0, 1]}, used for the brain scale and spare capacity checks.
     *
     * @param xp stored XP, possibly outside the legal range
     * @param maxXp capacity; values below one produce zero rather than a division error
     */
    public static float fillPercent(int xp, int maxXp) {
        if (maxXp <= 0) {
            return 0.0f;
        }
        return (float) clampXp(xp, maxXp) / maxXp;
    }

    /**
     * Comparator signal in {@code [0, 15]}: zero when empty, otherwise one plus
     * {@link #COMPARATOR_STEPS} steps scaled by the fill ratio.
     */
    public static int comparatorOutput(int xp, int maxXp) {
        if (xp <= 0 || maxXp <= 0) {
            return 0;
        }
        return 1 + (int) (fillPercent(xp, maxXp) * COMPARATOR_STEPS);
    }

    /** Normalizes an angle to [-PI, PI), like the four for-loops in the original. */
    public static float wrapAngle(float angle) {
        if (!Float.isFinite(angle)) {
            return 0.0f;
        }
        float twoPi = (float) (Math.PI * 2.0);
        angle %= twoPi;
        if (angle >= (float) Math.PI) {
            angle -= twoPi;
        }
        if (angle < -(float) Math.PI) {
            angle += twoPi;
        }
        return angle;
    }

    /** Turns towards a target by a fixed fraction of the shortest angular distance. */
    public static float chaseTarget(float current, float target, float speed) {
        float wrapped = wrapAngle(current);
        return wrapped + wrapAngle(target - wrapped) * speed;
    }

    /** Idle rotation: the persistent target drifts slowly while no player is watched. */
    public static float idleTarget(float target) {
        return wrapAngle(target + IDLE_DRIFT);
    }

    /** Advances the bob phase by one tick. */
    public static float advanceBob(float phase) {
        return phase + BOB_STEP;
    }

    /**
     * Vertical bob offset of the brain. Non-finite phases yield zero so a corrupted
     * phase can never make the renderer produce NaN vertices.
     */
    public static float bobY(float phase) {
        if (!Float.isFinite(phase)) {
            return 0.0f;
        }
        return (float) Math.sin(phase) * BOB_AMPLITUDE;
    }

    /**
     * Pull toward the jar centre. The divisor 25 and the (1 - distance)^2 falloff
     * are taken verbatim from the original; the zero-distance guard avoids a NaN.
     */
    public static Pull orbPull(double cx, double cy, double cz,
                               double ox, double oy, double oz) {
        double dx = (cx - ox) / PULL_DIVISOR;
        double dy = (cy - oy) / PULL_DIVISOR;
        double dz = (cz - oz) / PULL_DIVISOR;
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || !Double.isFinite(dz)) {
            return new Pull(0.0, 0.0, 0.0);
        }
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double strength = 1.0 - dist;
        if (strength <= 0.0 || dist <= 1e-6) {
            return new Pull(0.0, 0.0, 0.0);
        }
        strength *= strength;
        return new Pull(
                dx / dist * strength * PULL_HORIZONTAL,
                dy / dist * strength * PULL_VERTICAL,
                dz / dist * strength * PULL_HORIZONTAL);
    }

    /** eat box: block-centre +/- 0.6, i.e. block corners -0.1 .. +1.1. */
    public static Bounds eatBox(double cx, double cy, double cz) {
        return new Bounds(cx - EAT_HALF_SIZE, cy - EAT_HALF_SIZE, cz - EAT_HALF_SIZE,
                cx + EAT_HALF_SIZE, cy + EAT_HALF_SIZE, cz + EAT_HALF_SIZE);
    }

    /** pull box: block bounds expanded by 8, i.e. -8 .. +9 from the block corner. */
    public static Bounds pullBox(double blockX, double blockY, double blockZ) {
        return new Bounds(blockX - PULL_RADIUS, blockY - PULL_RADIUS, blockZ - PULL_RADIUS,
                blockX + 1.0 + PULL_RADIUS, blockY + 1.0 + PULL_RADIUS, blockZ + 1.0 + PULL_RADIUS);
    }

    /** The original waits 5s plus a random 0..25s between brain noises. */
    public static long nextSigh(long now, IntSupplier jitter) {
        return now + SIGH_BASE_DELAY + jitter.getAsInt();
    }

    /** Bound of nextInt() used when shaking the jar loose: min(xp + 1, 64). */
    public static int orbValueCap(int xp) {
        return Math.min(Math.max(xp, 0) + 1, ORB_VALUE_CAP);
    }

    /** Splits an XP amount into orb values, matching ExperienceOrb.getExperienceValue. */
    public static int[] splitOrbValues(int xp) {
        if (xp <= 0) {
            return new int[0];
        }
        List<Integer> parts = new ArrayList<>();
        int remaining = xp;
        while (remaining > 0) {
            int part = orbSplit(remaining);
            parts.add(part);
            remaining -= part;
        }
        int[] result = new int[parts.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = parts.get(i);
        }
        return result;
    }

    /** Bytecode-verified table from Minecraft 1.20.1 ExperienceOrb#getExperienceValue. */
    private static int orbSplit(int xp) {
        if (xp >= 2477) return 2477;
        if (xp >= 1237) return 1237;
        if (xp >= 617) return 617;
        if (xp >= 307) return 307;
        if (xp >= 149) return 149;
        if (xp >= 73) return 73;
        if (xp >= 37) return 37;
        if (xp >= 17) return 17;
        if (xp >= 7) return 7;
        if (xp >= 3) return 3;
        return 1;
    }

    /** Vanilla XP thresholds, used by dispenseXP. */
    public static int xpForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        }
        if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    /**
     * Amount of XP to hand out when a jar is used directly.
     *
     * @param xp stored XP, must be positive for a non-zero result
     * @param level current player level; only used when {@code all} is false
     * @param all when true, release everything instead of just the next level
     */
    public static int dispenseAmount(int xp, int level, boolean all) {
        if (xp <= 0) {
            return 0;
        }
        if (all) {
            return xp;
        }
        int needed = xpForLevel(level + 1) - xpForLevel(level);
        return Math.min(xp, Math.max(needed, 0));
    }
}
