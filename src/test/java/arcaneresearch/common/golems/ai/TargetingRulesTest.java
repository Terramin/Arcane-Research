package arcaneresearch.common.golems.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.golems.ai.TargetingRules.Facts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Целеуказание: чистые правила выбора цели")
class TargetingRulesTest {

    /** Test builder with sane defaults so each test only overrides what matters. */
    private static final class FactsBuilder {
        private boolean alive = true;
        private boolean self;
        private boolean canAttack = true;
        private boolean hasTeam;
        private boolean sameTeam;
        private boolean friendly;
        private boolean hasOwner;
        private boolean targetOwned;
        private boolean sameOwner;
        private boolean targetIsPlayer;
        private boolean targetIsOwner;
        private boolean invulnerablePlayer;
        private boolean excludeCreative;
        private boolean spectator;
        private boolean lineOfSight;
        private boolean checkSight;

        FactsBuilder alive(boolean value) { alive = value; return this; }
        FactsBuilder self(boolean value) { self = value; return this; }
        FactsBuilder canAttack(boolean value) { canAttack = value; return this; }
        FactsBuilder friendly(boolean value) { friendly = value; return this; }
        FactsBuilder hasOwner(boolean value) { hasOwner = value; return this; }
        FactsBuilder targetOwned(boolean value) { targetOwned = value; return this; }
        FactsBuilder sameOwner(boolean value) { sameOwner = value; return this; }
        FactsBuilder targetIsPlayer(boolean value) { targetIsPlayer = value; return this; }
        FactsBuilder targetIsOwner(boolean value) { targetIsOwner = value; return this; }
        FactsBuilder invulnerablePlayer(boolean value) { invulnerablePlayer = value; return this; }
        FactsBuilder excludeCreative(boolean value) { excludeCreative = value; return this; }
        FactsBuilder spectator(boolean value) { spectator = value; return this; }
        FactsBuilder lineOfSight(boolean value) { lineOfSight = value; return this; }
        FactsBuilder checkSight(boolean value) { checkSight = value; return this; }

        Facts build() {
            return new Facts(alive, self, canAttack, hasTeam, sameTeam, friendly,
                    hasOwner, targetOwned, sameOwner, targetIsPlayer, targetIsOwner,
                    invulnerablePlayer, excludeCreative, spectator, lineOfSight, checkSight);
        }
    }

    private static FactsBuilder builder() {
        return new FactsBuilder();
    }

    @Test
    void invalidTargetsAreRejectedEarly() {
        assertFalse(TargetingRules.goodTarget(builder().alive(false).build()));
        assertFalse(TargetingRules.goodTarget(builder().self(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().canAttack(false).build()));
        assertTrue(TargetingRules.goodTarget(builder().build()));
    }

    @Test
    void friendlyTeamRulesMatchTheOriginalConditions() {
        assertTrue(TargetingRules.friendlyTargetingValid(false, false, false));
        assertFalse(TargetingRules.friendlyTargetingValid(true, true, false));
        assertFalse(TargetingRules.friendlyTargetingValid(true, false, true));
        assertTrue(TargetingRules.friendlyTargetingValid(true, true, true));
        assertTrue(TargetingRules.friendlyTargetingValid(true, false, false));
    }

    @Test
    void ownedTargetsRespectTheOwnerAndFriendlyMode() {
        assertFalse(TargetingRules.goodTarget(builder().hasOwner(true).targetOwned(true)
                .sameOwner(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().hasOwner(true).friendly(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().hasOwner(true).targetOwned(true)
                .friendly(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().hasOwner(true).targetIsPlayer(true)
                .targetIsOwner(true).build()));
        assertTrue(TargetingRules.goodTarget(builder().hasOwner(true).targetOwned(true).build()));
    }

    @Test
    void creativeSpectatorsAndSightAreHandled() {
        assertFalse(TargetingRules.goodTarget(builder().targetIsPlayer(true)
                .invulnerablePlayer(true).build()));
        assertTrue(TargetingRules.goodTarget(builder().targetIsPlayer(true)
                .invulnerablePlayer(true).excludeCreative(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().spectator(true).build()));
        assertFalse(TargetingRules.goodTarget(builder().checkSight(true).lineOfSight(false).build()));
        assertTrue(TargetingRules.goodTarget(builder().checkSight(true).lineOfSight(true).build()));
    }
}
