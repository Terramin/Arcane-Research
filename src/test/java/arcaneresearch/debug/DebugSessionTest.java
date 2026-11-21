package arcaneresearch.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DebugSession: фильтры, счётчики и кольцевой буфер")
class DebugSessionTest {

    private final DebugSession session = DebugSession.get();

    @BeforeEach
    @AfterEach
    void resetSingleton() {
        session.stop();
        session.resetStats();
    }

    @Test
    void inactiveSessionDropsVerboseMessagesButKeepsInfoAndAbove() {
        assertFalse(session.isActive());
        assertFalse(session.isEnabled(LogLevel.TRACE, ProfileCategory.CORE));
        assertFalse(session.isEnabled(LogLevel.DEBUG, ProfileCategory.CORE));
        assertTrue(session.isEnabled(LogLevel.INFO, ProfileCategory.CORE));
        assertTrue(session.isEnabled(LogLevel.WARN, ProfileCategory.CORE));
        assertTrue(session.isEnabled(LogLevel.ERROR, ProfileCategory.CORE));
        assertFalse(session.isEnabled(LogLevel.OFF, ProfileCategory.CORE));
        assertFalse(session.isEnabled(null, ProfileCategory.CORE));
        assertFalse(session.isTimingEnabled(ProfileCategory.CORE));
    }

    @Test
    void activeSessionAppliesLevelThresholdAndCategory() {
        session.start(LogLevel.parseSet("DEBUG"), ProfileCategory.AURA);

        assertTrue(session.isActive());
        assertTrue(session.isEnabled(LogLevel.DEBUG, ProfileCategory.AURA));
        assertFalse(session.isEnabled(LogLevel.TRACE, ProfileCategory.AURA));
        assertFalse(session.isEnabled(LogLevel.DEBUG, ProfileCategory.WORLD));
        assertTrue(session.isEnabled(LogLevel.INFO, ProfileCategory.AURA));

        // Critical diagnostics are never filtered by category.
        assertTrue(session.isEnabled(LogLevel.WARN, ProfileCategory.WORLD));
        assertTrue(session.isEnabled(LogLevel.ERROR, ProfileCategory.WORLD));
    }

    @Test
    void allCategoryEnablesEverySubsystemForTiming() {
        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.ALL);

        assertTrue(session.isTimingEnabled(ProfileCategory.CORE));
        assertTrue(session.isTimingEnabled(ProfileCategory.RENDER));
        assertTrue(session.isEnabled(LogLevel.INFO, ProfileCategory.RENDER));
        assertFalse(session.isEnabled(LogLevel.DEBUG, ProfileCategory.RENDER));
    }

    @Test
    void stopClearsFiltersAndRestoresDefaults() {
        session.start(LogLevel.parseSet("TRACE"), ProfileCategory.NETWORK);
        session.stop();

        assertFalse(session.isActive());
        assertTrue(session.levels().isEmpty());
        assertTrue(session.categories().isEmpty());
        assertFalse(session.isEnabled(LogLevel.TRACE, ProfileCategory.NETWORK));
        assertTrue(session.isEnabled(LogLevel.INFO, ProfileCategory.NETWORK));
    }

    @Test
    void returnedFilterSetsAreDefensiveCopies() {
        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.AURA);
        session.levels().clear();
        session.categories().clear();

        assertEquals(EnumSet.of(LogLevel.INFO), session.levels());
        assertEquals(EnumSet.of(ProfileCategory.AURA), session.categories());
    }

    @Test
    void startResetsStatistics() {
        session.recordLog(LogLevel.INFO);
        session.recordTiming("probe", 1_000_000L);
        session.captureLine("old");

        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.CORE);

        assertEquals(0, session.capturedLineCount());
        assertTrue(session.capturedLines().isEmpty());
        assertTrue(session.statusLines().stream().anyMatch(line -> line.contains("Timings      : none")));
    }

    @Test
    void markOnceIsIdempotentAndBounded() {
        assertTrue(session.markOnce("key"));
        assertFalse(session.markOnce("key"));

        for (int i = 0; i < 4096; i++) {
            session.markOnce("other-" + i);
        }
        // The set is capped at 4096 entries; exceeding it evicts all keys.
        assertTrue(session.markOnce("key"));
    }

    @Test
    void captureKeepsOnlyLatestTwoThousandLinesInChronologicalOrder() {
        for (int i = 0; i < 2005; i++) {
            session.captureLine("line-" + i);
        }

        assertEquals(2005, session.capturedLineCount());
        List<String> lines = session.capturedLines();
        assertEquals(2000, lines.size());
        assertEquals("line-5", lines.get(0));
        assertEquals("line-2004", lines.get(lines.size() - 1));
    }

    @Test
    void timingStatsAccumulateCountTotalAverageMinAndMax() {
        DebugSession.TimingStats stats = new DebugSession.TimingStats();
        assertEquals("0 / 0ms / 0ms / 0ms / 0ms", stats.toString());

        stats.record(1_000_000L);
        stats.record(3_000_000L);

        assertEquals("2 / 4.00ms / 2.00ms / 1.00ms / 3.00ms", stats.toString());
    }

    @Test
    void statusLinesReportActiveFiltersAndCounters() {
        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.AURA);
        session.recordLog(LogLevel.INFO);
        session.recordLog(LogLevel.WARN);

        List<String> status = session.statusLines();

        assertTrue(status.stream().anyMatch(line -> line.contains("Status       : ACTIVE")));
        assertTrue(status.stream().anyMatch(line -> line.contains("Levels       : INFO")));
        assertTrue(status.stream().anyMatch(line -> line.contains("Profile type : AURA")));
        assertTrue(status.stream().anyMatch(line -> line.contains("INFO=1 WARN=1")));
    }
}
