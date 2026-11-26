package arcaneresearch.common.tiles.essentia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Brain jar: pure rules")
class JarBrainRulesTest {

    private static final float PI = (float) Math.PI;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    @Nested
    @DisplayName("XP limits and comparator")
    class Limits {

        @Test
        @DisplayName("XP is clamped to the mutable maximum, and never negative")
        void clampXp() {
            assertEquals(0, JarBrainRules.clampXp(0, 2000));
            assertEquals(123, JarBrainRules.clampXp(123, 2000));
            assertEquals(2000, JarBrainRules.clampXp(2000, 2000));
            assertEquals(2000, JarBrainRules.clampXp(5000, 2000));
            assertEquals(0, JarBrainRules.clampXp(-7, 2000));
            assertEquals(100, JarBrainRules.clampXp(999, 100));
        }

        @Test
        @DisplayName("non-positive maximum cannot produce NaN or Infinity")
        void clampWithNonPositiveMax() {
            assertEquals(0, JarBrainRules.clampXp(500, 0));
            assertEquals(0, JarBrainRules.clampXp(500, -10));
            assertEquals(0.0f, JarBrainRules.fillPercent(500, 0), 0.0f);
            assertEquals(0.0f, JarBrainRules.fillPercent(500, -10), 0.0f);
            assertEquals(0, JarBrainRules.comparatorOutput(500, 0));
            assertEquals(0, JarBrainRules.comparatorOutput(500, -10));
        }

        @Test
        @DisplayName("fill percent is linear up to the cap")
        void fillPercent() {
            assertEquals(0.0f, JarBrainRules.fillPercent(0, 2000), 1e-6f);
            assertEquals(0.25f, JarBrainRules.fillPercent(500, 2000), 1e-6f);
            assertEquals(0.5f, JarBrainRules.fillPercent(1000, 2000), 1e-6f);
            assertEquals(0.75f, JarBrainRules.fillPercent(1500, 2000), 1e-6f);
            assertEquals(1.0f, JarBrainRules.fillPercent(2000, 2000), 1e-6f);
            assertEquals(1.0f, JarBrainRules.fillPercent(9000, 2000), 1e-6f);
        }

        @Test
        @DisplayName("comparator follows the smooth 0..15 curve")
        void comparatorCurve() {
            assertEquals(0, JarBrainRules.comparatorOutput(0, 2000));
            assertEquals(1, JarBrainRules.comparatorOutput(1, 2000));
            assertEquals(4, JarBrainRules.comparatorOutput(500, 2000));
            assertEquals(8, JarBrainRules.comparatorOutput(1000, 2000));
            assertEquals(11, JarBrainRules.comparatorOutput(1500, 2000));
            assertEquals(15, JarBrainRules.comparatorOutput(2000, 2000));
            assertEquals(15, JarBrainRules.comparatorOutput(9999, 2000));
        }

        @Test
        @DisplayName("comparator never leaves 0..15 for any XP and maximum")
        void comparatorStaysInRange() {
            for (int max : new int[]{1, 7, 100, 2000, 100000}) {
                for (int xp = -5; xp <= max + 5; xp++) {
                    int out = JarBrainRules.comparatorOutput(xp, max);
                    assertTrue(out >= 0 && out <= 15, "out=" + out + " xp=" + xp + " max=" + max);
                }
            }
        }

        @Test
        @DisplayName("comparator is monotone in stored XP")
        void comparatorIsMonotone() {
            int previous = 0;
            for (int xp = 0; xp <= 2000; xp++) {
                int out = JarBrainRules.comparatorOutput(xp, 2000);
                assertTrue(out >= previous, "dropped at xp=" + xp);
                previous = out;
            }
        }

        @Test
        @DisplayName("constants match the expected jar-brain values")
        void constantsMatchOriginal() {
            assertEquals(2000, JarBrainRules.DEFAULT_MAX_XP);
            assertEquals(40, JarBrainRules.EAT_DELAY);
            assertEquals(1500L, JarBrainRules.INITIAL_SIGH_DELAY);
            assertEquals(5000L, JarBrainRules.SIGH_BASE_DELAY);
            assertEquals(25000, JarBrainRules.SIGH_JITTER);
            assertEquals(8.0, JarBrainRules.PULL_RADIUS);
            assertEquals(25.0, JarBrainRules.PULL_DIVISOR);
            assertEquals(0.6, JarBrainRules.EAT_HALF_SIZE);
            assertEquals(0.04f, JarBrainRules.ROTATION_SPEED);
            assertEquals(0.01f, JarBrainRules.IDLE_DRIFT);
            assertEquals(0.03f, JarBrainRules.BOB_AMPLITUDE);
            assertEquals(14, JarBrainRules.COMPARATOR_STEPS);
            assertEquals(64, JarBrainRules.ORB_VALUE_CAP);
            assertEquals(1.0f / 14.0f, JarBrainRules.BOB_STEP, 1e-7f);
        }
    }

    @Nested
    @DisplayName("Rotation and bob")
    class Rotation {

        @Test
        @DisplayName("wrapAngle keeps angles inside [-PI, PI)")
        void wrapKeepsRange() {
            float[] inputs = {0, PI, -PI, TWO_PI, -TWO_PI, 3 * PI, -3 * PI, 1e6f, -1e6f};
            for (float angle : inputs) {
                float wrapped = JarBrainRules.wrapAngle(angle);
                assertTrue(wrapped >= -PI && wrapped < PI, angle + " -> " + wrapped);
            }
        }

        @Test
        @DisplayName("wrapAngle preserves angles differing by full turns")
        void wrapIsPeriodic() {
            float base = 1.234f;
            float wrapped = JarBrainRules.wrapAngle(base);
            for (int k = -5; k <= 5; k++) {
                assertEquals(wrapped, JarBrainRules.wrapAngle(base + k * TWO_PI), 1e-5f);
            }
        }

        @Test
        @DisplayName("boundary angles wrap to -PI")
        void wrapBoundary() {
            assertEquals(-PI, JarBrainRules.wrapAngle(PI), 1e-6f);
            assertEquals(-PI, JarBrainRules.wrapAngle(-PI), 1e-6f);
            assertEquals(0.0f, JarBrainRules.wrapAngle(TWO_PI), 1e-6f);
        }

        @Test
        @DisplayName("non-finite angles never poison the renderer")
        void wrapNonFinite() {
            assertEquals(0.0f, JarBrainRules.wrapAngle(Float.NaN), 0.0f);
            assertEquals(0.0f, JarBrainRules.wrapAngle(Float.POSITIVE_INFINITY), 0.0f);
            assertEquals(0.0f, JarBrainRules.wrapAngle(Float.NEGATIVE_INFINITY), 0.0f);
        }

        @Test
        @DisplayName("chaseTarget moves exactly 4% of the angular distance")
        void chaseSpeed() {
            assertEquals(0.04f, JarBrainRules.chaseTarget(0.0f, 1.0f, 0.04f), 1e-6f);
            assertEquals(-0.04f, JarBrainRules.chaseTarget(0.0f, -1.0f, 0.04f), 1e-6f);
        }

        @Test
        @DisplayName("chaseTarget takes the shortest path across the +/-PI seam")
        void chaseUsesShortestArc() {
            float moved = JarBrainRules.chaseTarget(3.0f, -3.0f, 0.04f);
            assertTrue(moved > 3.0f, "should rotate forward across the seam: " + moved);
            assertTrue(moved < PI + 0.05f, "must not overshoot wildly: " + moved);

            float opposite = JarBrainRules.chaseTarget(-3.0f, 3.0f, 0.04f);
            assertTrue(opposite < -3.0f, "should rotate backward across the seam: " + opposite);
        }

        @Test
        @DisplayName("chaseTarget converges without overshooting")
        void chaseConverges() {
            float current = 0.0f;
            float target = 2.5f;
            for (int i = 0; i < 500; i++) {
                float next = JarBrainRules.chaseTarget(current, target, 0.04f);
                assertTrue(Math.abs(next - target) <= Math.abs(current - target) + 1e-6f,
                        "overshot at step " + i);
                current = next;
            }
            assertEquals(target, current, 1e-5f);
        }

        @Test
        @DisplayName("idle target drifts by 0.01 radians per tick and stays wrapped")
        void idleDrift() {
            assertEquals(0.01f, JarBrainRules.idleTarget(0.0f), 1e-6f);
            assertEquals(-PI + 0.01f, JarBrainRules.idleTarget(PI), 1e-5f);
            float target = 0.0f;
            for (int i = 0; i < 2000; i++) {
                target = JarBrainRules.idleTarget(target);
                assertTrue(target >= -PI && target < PI);
            }
        }

        @Test
        @DisplayName("bob is a sine with 0.03 amplitude and 1/14 step")
        void bobShape() {
            assertEquals(0.0f, JarBrainRules.bobY(0.0f), 1e-6f);
            assertEquals(0.03f, JarBrainRules.bobY(PI / 2), 1e-6f);
            assertEquals(0.0f, JarBrainRules.bobY(PI), 1e-6f);
            assertEquals(-0.03f, JarBrainRules.bobY(3 * PI / 2), 1e-6f);

            float phase = 0.0f;
            for (int i = 0; i < 14; i++) {
                phase = JarBrainRules.advanceBob(phase);
            }
            assertEquals(1.0f, phase, 1e-5f);

            phase = 0.0f;
            for (int i = 0; i < 10000; i++) {
                phase = JarBrainRules.advanceBob(phase);
                assertTrue(Math.abs(JarBrainRules.bobY(phase)) <= 0.03f + 1e-6f);
            }
        }

        @Test
        @DisplayName("bob period is about 88 ticks, matching the original")
        void bobPeriod() {
            float phase = 0.0f;
            for (int i = 0; i < 88; i++) {
                phase = JarBrainRules.advanceBob(phase);
            }
            assertEquals(TWO_PI, phase, 0.01f);
        }
    }

    @Nested
    @DisplayName("XP orb pull and boxes")
    class Pull {

        @Test
        @DisplayName("orb is pulled toward the jar centre")
        void pullDirection() {
            JarBrainRules.Pull pull = JarBrainRules.orbPull(0.5, 0.5, 0.5, 2.5, 0.5, 0.5);
            assertTrue(pull.x() < 0, "x=" + pull.x());
            assertEquals(0.0, pull.y(), 1e-9);
            assertEquals(0.0, pull.z(), 1e-9);
        }

        @Test
        @DisplayName("pull magnitude matches (1-d)^2 * 0.3/0.5/0.3 formula")
        void pullMagnitude() {
            JarBrainRules.Pull pull = JarBrainRules.orbPull(0.5, 0.5, 0.5, 1.5, 1.5, 0.5);
            assertEquals(-0.18885, pull.x(), 1e-4);
            assertEquals(-0.31475, pull.y(), 1e-4);
            assertEquals(0.0, pull.z(), 1e-9);
        }

        @Test
        @DisplayName("vertical pull is stronger than horizontal")
        void verticalIsStronger() {
            JarBrainRules.Pull x = JarBrainRules.orbPull(0.5, 0.5, 0.5, 2.5, 0.5, 0.5);
            JarBrainRules.Pull y = JarBrainRules.orbPull(0.5, 0.5, 0.5, 0.5, 2.5, 0.5);
            assertEquals(0.5 / 0.3, Math.abs(y.y()) / Math.abs(x.x()), 1e-6);
        }

        @Test
        @DisplayName("zero distance produces zero pull instead of NaN")
        void pullAtCentre() {
            JarBrainRules.Pull pull = JarBrainRules.orbPull(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
            assertEquals(0.0, pull.x(), 0.0);
            assertEquals(0.0, pull.y(), 0.0);
            assertEquals(0.0, pull.z(), 0.0);
        }

        @Test
        @DisplayName("pull vanishes beyond the 25-block falloff")
        void pullOutsideRange() {
            JarBrainRules.Pull pull = JarBrainRules.orbPull(0, 0, 0, 26, 0, 0);
            assertEquals(0.0, pull.x(), 0.0);
            assertEquals(0.0, pull.y(), 0.0);
            assertEquals(0.0, pull.z(), 0.0);
        }

        @Test
        @DisplayName("pull is mirror-symmetric around the jar")
        void pullSymmetry() {
            JarBrainRules.Pull right = JarBrainRules.orbPull(0, 0, 0, 2, 0, 0);
            JarBrainRules.Pull left = JarBrainRules.orbPull(0, 0, 0, -2, 0, 0);
            assertEquals(-right.x(), left.x(), 1e-9);
        }

        @Test
        @DisplayName("pull weakens with distance")
        void pullWeakens() {
            double near = Math.abs(JarBrainRules.orbPull(0, 0, 0, 1, 0, 0).x());
            double far = Math.abs(JarBrainRules.orbPull(0, 0, 0, 5, 0, 0).x());
            assertTrue(near > far, near + " <= " + far);
        }

        @Test
        @DisplayName("search box is the original block bounds expanded by 8")
        void pullBoxBounds() {
            JarBrainRules.Bounds b = JarBrainRules.pullBox(10, 20, 30);
            assertEquals(2, b.minX(), 1e-9);
            assertEquals(12, b.minY(), 1e-9);
            assertEquals(22, b.minZ(), 1e-9);
            assertEquals(19, b.maxX(), 1e-9);
            assertEquals(29, b.maxY(), 1e-9);
            assertEquals(39, b.maxZ(), 1e-9);
        }

        @Test
        @DisplayName("eat box is centre +/- 0.6, i.e. block corners -0.1 .. +1.1")
        void eatBoxBounds() {
            JarBrainRules.Bounds b = JarBrainRules.eatBox(10.5, 20.5, 30.5);
            assertEquals(9.9, b.minX(), 1e-9);
            assertEquals(19.9, b.minY(), 1e-9);
            assertEquals(29.9, b.minZ(), 1e-9);
            assertEquals(11.1, b.maxX(), 1e-9);
            assertEquals(21.1, b.maxY(), 1e-9);
            assertEquals(31.1, b.maxZ(), 1e-9);
        }
    }

    @Nested
    @DisplayName("Sigh timing and XP dispensing")
    class Timing {

        @Test
        @DisplayName("sigh delay is 5s plus a random 0..25s")
        void sighDelayRange() {
            long now = 1_000L;
            for (long seed = 0; seed < 50; seed++) {
                final long fixedSeed = seed;
                long next = JarBrainRules.nextSigh(now, () -> new Random(fixedSeed).nextInt(25000));
                assertTrue(next >= now + 5000L && next < now + 30000L,
                        "seed=" + seed + " -> " + next);
            }
        }

        @Test
        @DisplayName("sigh delay is deterministic for a given seed")
        void sighDeterministic() {
            assertEquals(JarBrainRules.nextSigh(100L, () -> new Random(7).nextInt(25000)),
                    JarBrainRules.nextSigh(100L, () -> new Random(7).nextInt(25000)));
        }

        @Test
        @DisplayName("XP bound for shaking is capped at 64")
        void orbValueCap() {
            assertEquals(1, JarBrainRules.orbValueCap(0));
            assertEquals(2, JarBrainRules.orbValueCap(1));
            assertEquals(6, JarBrainRules.orbValueCap(5));
            assertEquals(64, JarBrainRules.orbValueCap(63));
            assertEquals(64, JarBrainRules.orbValueCap(64));
            assertEquals(64, JarBrainRules.orbValueCap(2000));
            assertEquals(1, JarBrainRules.orbValueCap(-1));
        }

        @Test
        @DisplayName("orb splitting conserves XP using the vanilla orb values")
        void orbSplitting() {
            Set<Integer> allowed = Set.of(1, 3, 7, 17, 37, 73, 149, 307, 617, 1237, 2477);

            assertArrayEquals(new int[0], JarBrainRules.splitOrbValues(0));
            assertArrayEquals(new int[]{1}, JarBrainRules.splitOrbValues(1));
            assertArrayEquals(new int[]{3}, JarBrainRules.splitOrbValues(3));
            assertArrayEquals(new int[]{7, 3}, JarBrainRules.splitOrbValues(10));
            assertArrayEquals(new int[]{73, 17, 7, 3}, JarBrainRules.splitOrbValues(100));
            assertArrayEquals(new int[]{1237, 617, 73, 73}, JarBrainRules.splitOrbValues(2000));

            for (int xp = 1; xp <= 2000; xp++) {
                int[] parts = JarBrainRules.splitOrbValues(xp);
                int sum = 0;
                for (int part : parts) {
                    assertTrue(allowed.contains(part), "unexpected orb value " + part + " at xp=" + xp);
                    sum += part;
                }
                assertEquals(xp, sum, "split lost XP at " + xp);
            }
        }

        @Test
        @DisplayName("XP thresholds match the vanilla level table")
        void levelTable() {
            assertEquals(0, JarBrainRules.xpForLevel(0));
            assertEquals(7, JarBrainRules.xpForLevel(1));
            assertEquals(352, JarBrainRules.xpForLevel(16));
            assertEquals(394, JarBrainRules.xpForLevel(17));
            assertEquals(1507, JarBrainRules.xpForLevel(31));
            assertEquals(1628, JarBrainRules.xpForLevel(32));
            assertEquals(0, JarBrainRules.xpForLevel(-3));
        }

        @Test
        @DisplayName("level thresholds grow monotonically")
        void levelTableMonotone() {
            for (int level = 1; level <= 200; level++) {
                assertTrue(JarBrainRules.xpForLevel(level) > JarBrainRules.xpForLevel(level - 1),
                        "not monotone at level " + level);
            }
        }

        @Test
        @DisplayName("dispensing is limited by XP and by the next level")
        void dispenseAmount() {
            assertEquals(100, JarBrainRules.dispenseAmount(100, 0, true));
            assertEquals(7, JarBrainRules.dispenseAmount(100, 0, false));
            assertEquals(3, JarBrainRules.dispenseAmount(3, 0, false));
            assertEquals(0, JarBrainRules.dispenseAmount(0, 0, false));
            assertEquals(0, JarBrainRules.dispenseAmount(-5, 0, true));
            assertEquals(0, JarBrainRules.dispenseAmount(100, -1, false));
        }
    }
}
