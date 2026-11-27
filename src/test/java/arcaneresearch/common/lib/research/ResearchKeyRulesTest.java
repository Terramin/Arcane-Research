package arcaneresearch.common.lib.research;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Исследование: парser ключей требований")
class ResearchKeyRulesTest {

    private static final Set<String> KNOWN = Set.of("A", "B", "C", "SEALCOLLECT", "MINDBIOTHAUMIC");
    private static final Set<String> COMPLETE = Set.of("A", "B", "SEALCOLLECT", "MINDBIOTHAUMIC");
    private static final Map<String, Integer> STAGES = new HashMap<>();

    static {
        STAGES.put("A", 2);
        STAGES.put("B", 0);
        STAGES.put("SEALCOLLECT", 1);
        STAGES.put("MINDBIOTHAUMIC", 1);
    }

    private static final ResearchKeyRules.State STATE = new ResearchKeyRules.State() {
        @Override
        public boolean known(String key) {
            return KNOWN.contains(key);
        }

        @Override
        public boolean complete(String key) {
            return COMPLETE.contains(key);
        }

        @Override
        public int stage(String key) {
            return STAGES.getOrDefault(key, 0);
        }
    };

    @Test
    @DisplayName("empty key is always satisfied, null is never")
    void emptyAndNull() {
        assertTrue(ResearchKeyRules.matches("", STATE));
        assertFalse(ResearchKeyRules.matches(null, STATE));
    }

    @Test
    @DisplayName("plain keys require a completed research entry")
    void plainKeys() {
        assertTrue(ResearchKeyRules.matches("A", STATE));
        assertTrue(ResearchKeyRules.matches("B", STATE));
        assertFalse(ResearchKeyRules.matches("C", STATE), "known but not complete");
        assertFalse(ResearchKeyRules.matches("MISSING", STATE));
    }

    @Test
    @DisplayName("&& requires every part")
    void conjunction() {
        assertTrue(ResearchKeyRules.matches("SEALCOLLECT&&MINDBIOTHAUMIC", STATE));
        assertFalse(ResearchKeyRules.matches("SEALCOLLECT&&C", STATE));
        assertFalse(ResearchKeyRules.matches("SEALCOLLECT&&MISSING", STATE));
    }

    @Test
    @DisplayName("|| accepts any part")
    void disjunction() {
        assertTrue(ResearchKeyRules.matches("A||MISSING", STATE));
        assertTrue(ResearchKeyRules.matches("MISSING||B", STATE));
        assertFalse(ResearchKeyRules.matches("MISSING||C", STATE));
    }

    @Test
    @DisplayName("@stage requires knowledge and a sufficient stage")
    void stages() {
        assertTrue(ResearchKeyRules.matches("A@1", STATE));
        assertTrue(ResearchKeyRules.matches("A@2", STATE));
        assertFalse(ResearchKeyRules.matches("A@3", STATE));
        assertFalse(ResearchKeyRules.matches("MISSING@1", STATE));
        assertTrue(ResearchKeyRules.matches("B@0", STATE));
    }

    @Test
    @DisplayName("nested and malformed keys are handled without throwing")
    void nestedKeys() {
        assertFalse(ResearchKeyRules.matches("(A&&B)||MISSING", STATE));
        assertTrue(ResearchKeyRules.matches("A@not-a-number", STATE));
        assertFalse(ResearchKeyRules.matches("A&&MISSING", STATE));
        assertTrue(ResearchKeyRules.matches("A||A", STATE));
    }

    @Test
    @DisplayName("convenience overload uses the provided predicates")
    void convenienceOverload() {
        assertTrue(ResearchKeyRules.matches("A", KNOWN::contains, COMPLETE::contains,
                key -> STAGES.getOrDefault(key, 0)));
        assertFalse(ResearchKeyRules.matches("C", KNOWN::contains, COMPLETE::contains,
                key -> STAGES.getOrDefault(key, 0)));
    }
}
