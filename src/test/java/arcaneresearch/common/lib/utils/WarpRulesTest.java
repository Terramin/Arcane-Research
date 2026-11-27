package arcaneresearch.common.lib.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Warp: чистые правила счётчика")
class WarpRulesTest {

    @Test
    @DisplayName("вычет кастуется после вычитания, как в 1.12.2")
    void counterCastsWholeSubtraction() {
        assertEquals(35, WarpRules.nextCounter(50, 0));
        assertEquals(11, WarpRules.nextCounter(20, 0));
        assertEquals(45, WarpRules.nextCounter(50, 10));
    }

    @Test
    @DisplayName("старый порядок приведения давал на 1 больше")
    void oldCastWasOneTooHigh() {
        int old = 50 - (int) Math.max(5.0, Math.sqrt(50) * 2.0);
        assertEquals(36, old);
        assertEquals(35, WarpRules.nextCounter(50, 0));
    }

    @Test
    @DisplayName("минимум вычета — 5, даже при большом warp от экипировки")
    void minimumDrainIsFive() {
        assertEquals(45, WarpRules.nextCounter(50, 10));
        assertEquals(95, WarpRules.nextCounter(100, 100));
    }

    @Test
    @DisplayName("малый счётчик может уйти в минус — как в оригинале")
    void smallCounterCanGoNegative() {
        assertEquals(-1, WarpRules.nextCounter(4, 0));
    }
}
