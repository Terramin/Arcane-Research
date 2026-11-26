package arcaneresearch.common.tiles.essentia;

import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Brain jar: robustness (NPE, hyper-allocation, extreme values)")
class JarBrainRobustnessTest {

    private static final float PI = (float) Math.PI;
    private static volatile double sink;

    @Test
    @DisplayName("extreme and invalid inputs never throw or leak NaN/Infinity")
    void extremeInputsAreSafe() {
        int[] extremes = {Integer.MIN_VALUE, -100, -1, 0, 1, 100, Integer.MAX_VALUE};

        for (int max : extremes) {
            for (int xp : extremes) {
                int clamped = JarBrainRules.clampXp(xp, max);
                assertTrue(clamped >= 0 && (max <= 0 || clamped <= max),
                        "bad clamp xp=" + xp + " max=" + max);

                float fill = JarBrainRules.fillPercent(xp, max);
                assertTrue(Float.isFinite(fill) && fill >= 0.0f && fill <= 1.0f,
                        "bad fill xp=" + xp + " max=" + max);

                int comparator = JarBrainRules.comparatorOutput(xp, max);
                assertTrue(comparator >= 0 && comparator <= 15,
                        "bad comparator xp=" + xp + " max=" + max);

                // All tick gates must be callable for any state without throwing.
                JarBrainRules.serverMayEat(xp, max, 0);
                JarBrainRules.serverMayPull(xp, max, 0, true);
                JarBrainRules.clientMayWatch(xp, max, true);
                JarBrainRules.clientMayPull(xp, max, 0, true);

                int dispensed = JarBrainRules.dispenseAmount(xp, max, false);
                assertTrue(dispensed >= 0 && (xp <= 0 || dispensed <= xp),
                        "bad dispense xp=" + xp + " max=" + max);
            }
        }

        float[] badAngles = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Float.MIN_VALUE, Float.MAX_VALUE};
        for (float angle : badAngles) {
            float wrapped = JarBrainRules.wrapAngle(angle);
            assertTrue(Float.isFinite(wrapped) && wrapped >= -PI && wrapped < PI,
                    "bad wrap: " + angle + " -> " + wrapped);
            assertTrue(Float.isFinite(JarBrainRules.chaseTarget(angle, angle, 0.04f)));
            assertTrue(Float.isFinite(JarBrainRules.bobY(angle)));
        }

        JarBrainRules.Pull far = JarBrainRules.orbPull(
                0, 0, 0, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        assertTrue(Double.isFinite(far.x()) && Double.isFinite(far.y()) && Double.isFinite(far.z()));

        JarBrainRules.Pull same = JarBrainRules.orbPull(
                Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE,
                Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        assertEquals(0.0, same.x(), 0.0);
        assertEquals(0.0, same.y(), 0.0);
        assertEquals(0.0, same.z(), 0.0);

        JarBrainRules.Pull nan = JarBrainRules.orbPull(0, 0, 0, Double.NaN, 0, 0);
        assertEquals(0.0, nan.x(), 0.0);
        assertEquals(0.0, nan.y(), 0.0);
        assertEquals(0.0, nan.z(), 0.0);
    }

    @Test
    @DisplayName("hot tick rules do not allocate per call (no hyper-allocation)")
    void hotRulesAreAllocationFree() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean() instanceof ThreadMXBean b ? b : null;
        Assumptions.assumeTrue(bean != null && bean.isThreadAllocatedMemorySupported(),
                "JVM does not expose per-thread allocation counters");

        long threadId = Thread.currentThread().getId();
        for (int i = 0; i < 200_000; i++) {
            sink += JarBrainRules.chaseTarget(i * 0.001f, 1.0f, JarBrainRules.ROTATION_SPEED);
            sink += JarBrainRules.wrapAngle(i * 0.01f);
        }

        long before = bean.getThreadAllocatedBytes(threadId);
        for (int i = 0; i < 1_000_000; i++) {
            sink += JarBrainRules.chaseTarget(i * 0.001f, 1.0f, JarBrainRules.ROTATION_SPEED);
            sink += JarBrainRules.wrapAngle(i * 0.01f);
        }
        long allocated = bean.getThreadAllocatedBytes(threadId) - before;

        assertTrue(allocated < 200_000L,
                "2M hot rule calls allocated " + allocated + " bytes; check for boxing/temporary objects");
        assertTrue(sink != 0.0, "results must be observed so JIT cannot eliminate the calls");
    }
}
