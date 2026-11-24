package arcaneresearch.common.lib.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Фрагменты знаний: правила выпадения")
class KnowledgeFragmentDropsTest {

    @Test
    @DisplayName("культист роняет фрагмент в 10% случаев")
    void cultistDropUsesTenPercentThreshold() {
        assertTrue(KnowledgeFragmentDrops.cultistDrop(0.0F));
        assertTrue(KnowledgeFragmentDrops.cultistDrop(0.099F));
        assertFalse(KnowledgeFragmentDrops.cultistDrop(0.1F));
        assertFalse(KnowledgeFragmentDrops.cultistDrop(1.0F));
        assertFalse(KnowledgeFragmentDrops.cultistDrop(-0.01F));
    }

    @Test
    @DisplayName("пороги сундуков соответствуют весам оригинала")
    void chestChancesMatchOriginalWeights() {
        assertTrue(KnowledgeFragmentDrops.roll(0.19F,
                KnowledgeFragmentDrops.STRONGHOLD_LIBRARY_CHANCE));
        assertFalse(KnowledgeFragmentDrops.roll(0.20F,
                KnowledgeFragmentDrops.STRONGHOLD_LIBRARY_CHANCE));
        assertTrue(KnowledgeFragmentDrops.roll(0.03F,
                KnowledgeFragmentDrops.OTHER_CHEST_CHANCE));
        assertFalse(KnowledgeFragmentDrops.roll(0.04F,
                KnowledgeFragmentDrops.OTHER_CHEST_CHANCE));
    }

    @Test
    @DisplayName("диапазоны количества фрагментов соответствуют оригиналу")
    void countsMatchOriginal() {
        assertEquals(3, KnowledgeFragmentDrops.LIBRARY_MIN);
        assertEquals(6, KnowledgeFragmentDrops.LIBRARY_MAX);
        assertEquals(1, KnowledgeFragmentDrops.OTHER_MIN);
        assertEquals(2, KnowledgeFragmentDrops.OTHER_MAX);
    }
}
