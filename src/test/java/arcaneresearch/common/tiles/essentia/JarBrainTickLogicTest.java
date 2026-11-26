package arcaneresearch.common.tiles.essentia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Brain jar: client and server tick decisions")
class JarBrainTickLogicTest {

    @Test
    @DisplayName("server tick pulls and eats only while hungry and not paused")
    void serverTickDecision() {
        assertTrue(JarBrainRules.serverMayEat(0, 2000, 0));
        assertTrue(JarBrainRules.serverMayEat(1999, 2000, 0));
        assertFalse(JarBrainRules.serverMayEat(2000, 2000, 0));
        assertFalse(JarBrainRules.serverMayEat(2001, 2000, 0));
        assertFalse(JarBrainRules.serverMayEat(0, 2000, 1));
        assertFalse(JarBrainRules.serverMayEat(0, 2000, 40));

        assertTrue(JarBrainRules.serverMayPull(0, 2000, 0, true));
        assertFalse(JarBrainRules.serverMayPull(0, 2000, 0, false));
        assertFalse(JarBrainRules.serverMayPull(0, 2000, 1, true));
        assertFalse(JarBrainRules.serverMayPull(2000, 2000, 0, true));
        assertFalse(JarBrainRules.serverMayPull(-1, -5, 0, true));

        assertTrue(JarBrainRules.serverMayEat(99, 100, 0));
        assertFalse(JarBrainRules.serverMayEat(100, 100, 0));
    }

    @Test
    @DisplayName("client tick watches a player and sighs only when no orb is nearby")
    void clientTickDecision() {
        assertTrue(JarBrainRules.clientMayWatch(0, 2000, false));
        assertFalse(JarBrainRules.clientMayWatch(0, 2000, true));
        // A full jar never finds an orb, so the original still watches the player.
        assertTrue(JarBrainRules.clientMayWatch(2000, 2000, true));
        assertTrue(JarBrainRules.clientMayWatch(0, 2000, true) == false);

        assertTrue(JarBrainRules.clientMayPull(0, 2000, 0, true));
        assertFalse(JarBrainRules.clientMayPull(0, 2000, 1, true));
        assertFalse(JarBrainRules.clientMayPull(0, 2000, 0, false));
        assertFalse(JarBrainRules.clientMayPull(2000, 2000, 0, true));

        assertFalse(JarBrainRules.clientMayWatch(99, 100, true));
        assertTrue(JarBrainRules.clientMayWatch(100, 100, true));
    }

    @Test
    @DisplayName("client and server gates agree on pulling for every state")
    void bothTicksUseTheSamePullGate() {
        int[] maxes = {0, 1, 100, 2000};
        int[] xps = {-5, 0, 1, 50, 99, 100, 2000};
        int[] delays = {0, 1, 40};

        for (int max : maxes) {
            for (int xp : xps) {
                for (int delay : delays) {
                    for (boolean orb : new boolean[]{false, true}) {
                        boolean serverPull = JarBrainRules.serverMayPull(xp, max, delay, orb);
                        boolean clientPull = JarBrainRules.clientMayPull(xp, max, delay, orb);
                        assertEquals(serverPull, clientPull,
                                "xp=" + xp + " max=" + max + " delay=" + delay + " orb=" + orb);

                        boolean watch = JarBrainRules.clientMayWatch(xp, max, orb);
                        assertEquals(!(xp < max && orb), watch,
                                "watch gate changed at xp=" + xp + " max=" + max + " orb=" + orb);

                        if (serverPull) {
                            assertTrue(JarBrainRules.serverMayEat(xp, max, delay));
                        }
                    }
                }
            }
        }
    }
}
