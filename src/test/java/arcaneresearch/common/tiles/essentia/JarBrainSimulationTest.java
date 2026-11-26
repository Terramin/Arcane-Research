package arcaneresearch.common.tiles.essentia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Brain jar: simulation and randomized invariants")
class JarBrainSimulationTest {

    private static final float PI = (float) Math.PI;

    /** Deliberately minimal stand-in for an XP orb; only the rules under test are real. */
    private static final class Orb {
        double x;
        double y;
        double z;
        double vx;
        double vy;
        double vz;
        final int value;

        Orb(double x, double y, double z, int value) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
        }
    }

    /** Tiny model of TileJarBrain that uses the real rules for every calculation. */
    private static final class Sim {
        final double cx;
        final double cy;
        final double cz;
        final int max;
        final Random random;
        final List<Orb> orbs = new ArrayList<>();
        int xp;
        int eatDelay;
        int absorbed;

        Sim(double cx, double cy, double cz, int max, Random random) {
            this.cx = cx;
            this.cy = cy;
            this.cz = cz;
            this.max = max;
            this.random = random;
        }

        void tick(int orbValue) {
            if (orbValue > 0) {
                orbs.add(new Orb(
                        cx + (random.nextDouble() - 0.5) * 3.0,
                        cy + (random.nextDouble() - 0.5) * 3.0,
                        cz + (random.nextDouble() - 0.5) * 3.0,
                        orbValue));
            }

            if (eatDelay > 0) {
                eatDelay--;
                return;
            }
            if (xp >= max) {
                return;
            }

            JarBrainRules.Bounds eatBox = JarBrainRules.eatBox(cx, cy, cz);
            Iterator<Orb> it = orbs.iterator();
            while (it.hasNext()) {
                Orb orb = it.next();
                JarBrainRules.Pull pull = JarBrainRules.orbPull(cx, cy, cz, orb.x, orb.y, orb.z);
                if (!Double.isFinite(pull.x()) || !Double.isFinite(pull.y()) || !Double.isFinite(pull.z())) {
                    throw new AssertionError("non-finite pull: " + pull);
                }

                orb.vx = (orb.vx + pull.x()) * 0.8;
                orb.vy = (orb.vy + pull.y()) * 0.8;
                orb.vz = (orb.vz + pull.z()) * 0.8;
                orb.x += orb.vx;
                orb.y += orb.vy;
                orb.z += orb.vz;
                if (!Double.isFinite(orb.x) || !Double.isFinite(orb.y) || !Double.isFinite(orb.z)) {
                    throw new AssertionError("non-finite orb position");
                }

                if (orb.x >= eatBox.minX() && orb.x <= eatBox.maxX()
                        && orb.y >= eatBox.minY() && orb.y <= eatBox.maxY()
                        && orb.z >= eatBox.minZ() && orb.z <= eatBox.maxZ()) {
                    xp = JarBrainRules.clampXp(xp + orb.value, max);
                    absorbed++;
                    it.remove();
                }
            }
        }
    }

    @Test
    @DisplayName("1000-tick simulation: orbs are pulled in, absorbed, capped, then ignored while full")
    void simulatedLifecycle() {
        Random random = new Random(20261127L);
        Sim sim = new Sim(10.5, 4.5, -3.5, JarBrainRules.DEFAULT_MAX_XP, random);

        for (int tick = 0; tick < 1000; tick++) {
            sim.tick(25);

            assertTrue(sim.xp >= 0 && sim.xp <= sim.max, "xp out of range at tick " + tick);
            float fill = JarBrainRules.fillPercent(sim.xp, sim.max);
            assertTrue(fill >= 0.0f && fill <= 1.0f && Float.isFinite(fill), "fill at tick " + tick);
            int comparator = JarBrainRules.comparatorOutput(sim.xp, sim.max);
            assertTrue(comparator >= 0 && comparator <= 15, "comparator at tick " + tick);
        }

        assertTrue(sim.absorbed > 0, "the brain never ate anything");
        assertEquals(sim.max, sim.xp, "a long enough simulation must fill the jar");
        assertEquals(15, JarBrainRules.comparatorOutput(sim.xp, sim.max));

        int absorbedWhenFull = sim.absorbed;
        for (int tick = 0; tick < 50; tick++) {
            sim.tick(1000);
        }
        assertEquals(absorbedWhenFull, sim.absorbed, "a full jar must ignore further orbs");
        assertEquals(sim.max, sim.xp, "a full jar must not overfill");

        // Shaking the jar loose drops at most 64 XP and pauses eating for 40 ticks.
        int before = sim.xp;
        int bound = JarBrainRules.orbValueCap(sim.xp);
        assertTrue(bound <= 64);
        int shaken = random.nextInt(bound);
        sim.xp = JarBrainRules.clampXp(sim.xp - shaken, sim.max);
        sim.eatDelay = JarBrainRules.EAT_DELAY;

        assertTrue(sim.xp < before);
        assertTrue(shaken <= 64);
        assertTrue(JarBrainRules.comparatorOutput(sim.xp, sim.max) < 15);
        assertTrue(JarBrainRules.fillPercent(sim.xp, sim.max) < 1.0f);

        while (sim.eatDelay > 0) {
            sim.tick(25);
        }
        assertEquals(0, sim.eatDelay, "eatDelay must expire");
        assertEquals(before - shaken, sim.xp, "no XP may be absorbed while eatDelay is active");
    }

    @Test
    @DisplayName("20000 randomized operations keep every rule finite and in range")
    void randomizedInvariants() {
        Random random = new Random(1234567L);
        Set<Integer> orbValues = Set.of(1, 3, 7, 17, 37, 73, 149, 307, 617, 1237, 2477);

        for (int i = 0; i < 20_000; i++) {
            int max = random.nextInt(5000);
            int xp = random.nextInt(10000) - 100;
            int clamped = JarBrainRules.clampXp(xp, max);
            assertTrue(clamped >= 0 && clamped <= Math.max(max, 0), "clamp at i=" + i);

            float fill = JarBrainRules.fillPercent(xp, max);
            assertTrue(Float.isFinite(fill) && fill >= 0.0f && fill <= 1.0f, "fill at i=" + i);

            int comparator = JarBrainRules.comparatorOutput(xp, max);
            assertTrue(comparator >= 0 && comparator <= 15, "comparator at i=" + i);

            float angle = (random.nextFloat() - 0.5f) * 1_000_000.0f;
            float wrapped = JarBrainRules.wrapAngle(angle);
            assertTrue(Float.isFinite(wrapped) && wrapped >= -PI && wrapped < PI, "wrap at i=" + i);

            float target = (random.nextFloat() - 0.5f) * 100.0f;
            float chased = JarBrainRules.chaseTarget(wrapped, target, JarBrainRules.ROTATION_SPEED);
            assertTrue(Float.isFinite(chased), "chase at i=" + i);

            float phase = JarBrainRules.advanceBob(random.nextFloat() * 1000.0f);
            float bob = JarBrainRules.bobY(phase);
            assertTrue(Float.isFinite(bob) && Math.abs(bob) <= 0.03f + 1e-6f, "bob at i=" + i);

            double ox = (random.nextDouble() - 0.5) * 100.0;
            double oy = (random.nextDouble() - 0.5) * 100.0;
            double oz = (random.nextDouble() - 0.5) * 100.0;
            JarBrainRules.Pull pull = JarBrainRules.orbPull(0, 0, 0, ox, oy, oz);
            assertTrue(Math.abs(pull.x()) <= 0.3 + 1e-9, "pull x at i=" + i);
            assertTrue(Math.abs(pull.y()) <= 0.5 + 1e-9, "pull y at i=" + i);
            assertTrue(Math.abs(pull.z()) <= 0.3 + 1e-9, "pull z at i=" + i);

            int amount = random.nextInt(5000) + 1;
            int[] parts = JarBrainRules.splitOrbValues(amount);
            int sum = 0;
            for (int part : parts) {
                assertTrue(orbValues.contains(part), "orb value at i=" + i);
                sum += part;
            }
            assertEquals(amount, sum, "split at i=" + i);

            int dispensed = JarBrainRules.dispenseAmount(amount, random.nextInt(60), random.nextBoolean());
            assertTrue(dispensed >= 0 && dispensed <= amount, "dispense at i=" + i);
        }
    }
}
