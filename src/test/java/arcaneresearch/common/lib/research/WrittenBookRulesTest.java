package arcaneresearch.common.lib.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.lib.research.WrittenBookRules.KnowledgeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Книга таумономикон: правила переноса знаний")
class WrittenBookRulesTest {

    @Test
    void parsesTheoryAndObservationKeys() {
        var theory = WrittenBookRules.parseKnowledgeKey("T_AUROMANCY");
        assertEquals(KnowledgeType.THEORY, theory.type());
        assertEquals("AUROMANCY", theory.category());

        var observation = WrittenBookRules.parseKnowledgeKey("O_BASICS");
        assertEquals(KnowledgeType.OBSERVATION, observation.type());
        assertEquals("BASICS", observation.category());

        assertEquals("SOME_CATEGORY", WrittenBookRules.parseKnowledgeKey("T_SOME_CATEGORY").category());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"T", "O", "T_", "A_AUROMANCY", "_AUROMANCY", "t_AUROMANCY", "THEORY_AUROMANCY"})
    void rejectsMalformedKeys(String key) {
        assertNull(WrittenBookRules.parseKnowledgeKey(key));
    }

    @Test
    void onlyPageFlagIsTransferred() {
        assertTrue(WrittenBookRules.grantsPageFlag("PAGE"));
        assertTrue(WrittenBookRules.grantsPageFlag("RESEARCH,PAGE"));
        assertTrue(WrittenBookRules.grantsPageFlag("NOPE,PAGE,NOPE2"));
        assertFalse(WrittenBookRules.grantsPageFlag("RESEARCH"));
        assertFalse(WrittenBookRules.grantsPageFlag("page"));
        assertFalse(WrittenBookRules.grantsPageFlag(""));
        assertFalse(WrittenBookRules.grantsPageFlag(null));
    }

    @Test
    void storedKnowledgeNeverReducesTheReadersAmount() {
        assertEquals(3, WrittenBookRules.positiveDelta(5, 2));
        assertEquals(0, WrittenBookRules.positiveDelta(2, 5));
        assertEquals(0, WrittenBookRules.positiveDelta(5, 5));
    }
}
