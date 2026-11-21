package arcaneresearch.common.lib.crafting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.crafting.InfusionStabilityRules.Outcome;
import arcaneresearch.common.lib.crafting.InfusionStabilityRules.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Инфузионный алтарь: чистые правила стабильности")
class InfusionStabilityRulesTest {

    private static final int CAP = 25;

    @Test
    void tiersUseTheSameBoundariesAsTheTileEntity() {
        assertEquals(Tier.VERY_STABLE, InfusionStabilityRules.tier(13.0f, CAP));
        assertEquals(Tier.STABLE, InfusionStabilityRules.tier(12.5f, CAP));
        assertEquals(Tier.STABLE, InfusionStabilityRules.tier(0.0f, CAP));
        assertEquals(Tier.UNSTABLE, InfusionStabilityRules.tier(-0.01f, CAP));
        assertEquals(Tier.UNSTABLE, InfusionStabilityRules.tier(-24.9f, CAP));
        assertEquals(Tier.VERY_UNSTABLE, InfusionStabilityRules.tier(-25.0f, CAP));
        assertEquals(Tier.VERY_UNSTABLE, InfusionStabilityRules.tier(-100.0f, CAP));
    }

    @Test
    void lossPerCycleUsesTheTierDivisor() {
        assertEquals(2.0f, InfusionStabilityRules.lossPerCycle(10, 20.0f, CAP), 1.0e-5f);
        assertEquals(10.0f / 6.0f, InfusionStabilityRules.lossPerCycle(10, 0.0f, CAP), 1.0e-5f);
        assertEquals(10.0f / 7.0f, InfusionStabilityRules.lossPerCycle(10, -10.0f, CAP), 1.0e-5f);
        assertEquals(1.25f, InfusionStabilityRules.lossPerCycle(10, -30.0f, CAP), 1.0e-5f);
        assertEquals(0.0f, InfusionStabilityRules.lossPerCycle(0, 0.0f, CAP), 1.0e-5f);
    }

    @Test
    void cycleResultIsClampedToTheLegalRange() {
        assertEquals(-5.0f, InfusionStabilityRules.nextStability(20.0f, 30.0f, 5.0f, CAP), 1.0e-5f);
        assertEquals(-100.0f, InfusionStabilityRules.nextStability(0.0f, 200.0f, 0.0f, CAP), 1.0e-5f);
        assertEquals(CAP, InfusionStabilityRules.nextStability(20.0f, 0.0f, 100.0f, CAP), 1.0e-5f);
        assertEquals(4.0f, InfusionStabilityRules.nextStability(2.0f, 1.0f, 3.0f, CAP), 1.0e-5f);
    }

    @Test
    void instabilityOnlyRollsWhileNegativeAndUsesInclusiveUpperBound() {
        for (int roll = 0; roll < 1500; roll++) {
            assertFalse(InfusionStabilityRules.shouldDestabilize(0.0f, roll));
            assertFalse(InfusionStabilityRules.shouldDestabilize(5.0f, roll));
        }
        for (int roll = 0; roll <= 10; roll++) {
            assertTrue(InfusionStabilityRules.shouldDestabilize(-10.0f, roll));
        }
        assertFalse(InfusionStabilityRules.shouldDestabilize(-10.0f, 11));
        assertTrue(InfusionStabilityRules.shouldDestabilize(-100.0f, 100));
        assertFalse(InfusionStabilityRules.shouldDestabilize(-100.0f, 101));
        assertTrue(InfusionStabilityRules.shouldDestabilize(-1500.0f, 1499));
        assertFalse(InfusionStabilityRules.shouldDestabilize(-10.0f, -1));
        assertFalse(InfusionStabilityRules.shouldDestabilize(-10.0f, 1500));
    }

    @Test
    void allTwentyFourInstabilityOutcomesAreMapped() {
        Outcome[] expected = {
                Outcome.EJECT_0, Outcome.EJECT_0, Outcome.EJECT_0, Outcome.EJECT_0,
                Outcome.WARP, Outcome.WARP, Outcome.WARP,
                Outcome.ZAP, Outcome.ZAP, Outcome.ZAP,
                Outcome.ZAP_ALL, Outcome.ZAP_ALL,
                Outcome.EJECT_1, Outcome.EJECT_1,
                Outcome.EJECT_2, Outcome.EJECT_2,
                Outcome.EJECT_3, Outcome.EJECT_4,
                Outcome.HARM, Outcome.HARM,
                Outcome.EJECT_5, Outcome.EJECT_5,
                Outcome.HARM_ALL, Outcome.EXPLODE
        };

        for (int roll = 0; roll < expected.length; roll++) {
            assertEquals(expected[roll], InfusionStabilityRules.outcome(roll), "roll " + roll);
        }
        assertThrows(IllegalArgumentException.class, () -> InfusionStabilityRules.outcome(-1));
        assertThrows(IllegalArgumentException.class, () -> InfusionStabilityRules.outcome(24));
    }

    @Test
    void repeatedStabilizersLoseAQuarterEachTime() {
        assertEquals(0.1f, InfusionStabilizationRules.diminishingReturns(0.1f, 0), 1.0e-6f);
        assertEquals(0.075f, InfusionStabilizationRules.diminishingReturns(0.1f, 1), 1.0e-6f);
        assertEquals(0.05625f, InfusionStabilizationRules.diminishingReturns(0.1f, 2), 1.0e-6f);
        assertEquals(0.0421875f, InfusionStabilizationRules.diminishingReturns(0.1f, 3), 1.0e-6f);
    }

    @Test
    void mirroredPairsAddStabilityAndMismatchesPenalizeIt() {
        var symmetric = InfusionStabilizationRules.pair(0.1f, 0.1f, true, false, 0.0f);
        assertEquals(0.1f, symmetric.delta(), 1.0e-6f);
        assertFalse(symmetric.problem());

        // Diminishing returns change the first amount, but the pair stays matched.
        var diminished = InfusionStabilizationRules.pair(0.075f, 0.1f, true, false, 0.0f);
        assertEquals(0.075f, diminished.delta(), 1.0e-6f);
        assertFalse(diminished.problem());

        var mismatched = InfusionStabilizationRules.pair(0.1f, 0.3f, false, false, 0.0f);
        assertEquals(-0.3f, mismatched.delta(), 1.0e-6f);
        assertTrue(mismatched.problem());

        var differentBlocks = InfusionStabilizationRules.pair(0.1f, 0.1f, false, false, 0.0f);
        assertEquals(-0.1f, differentBlocks.delta(), 1.0e-6f);
        assertTrue(differentBlocks.problem());

        var custom = InfusionStabilizationRules.pair(0.1f, 0.1f, true, true, 0.7f);
        assertEquals(-0.7f, custom.delta(), 1.0e-6f);
        assertTrue(custom.problem());
    }
}
