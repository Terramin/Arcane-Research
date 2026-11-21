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

@DisplayName("Логирование: фильтры, захват строк и тайминги")
class ArcaneLoggerTest {

    private final DebugSession session = DebugSession.get();
    private final ArcaneLogger logger = ArcaneLogger.forCategory(ProfileCategory.AURA);

    @BeforeEach
    @AfterEach
    void reset() {
        session.stop();
        session.resetStats();
    }

    @Test
    void inactiveSessionDropsVerboseMessages() {
        logger.trace("hidden {}", 1);
        logger.debug("hidden");
        logger.info("visible info");
        logger.warn("visible warn");
        logger.error("visible error");

        assertTrue(session.capturedLines().isEmpty());
        assertEquals("TRACE=0 DEBUG=0 INFO=1 WARN=1 ERROR=1", counterLine());
    }

    @Test
    void activeSessionCapturesFormattedLineWithASingleCategoryTag() {
        session.start(EnumSet.of(LogLevel.DEBUG), ProfileCategory.AURA);

        logger.debug("value={} count={}", 42, 3);

        List<String> lines = session.capturedLines();
        assertEquals(1, lines.size());
        String line = lines.get(0);
        assertTrue(line.contains("[DEBUG]"), line);
        assertTrue(line.contains("[AURA]"), line);
        assertTrue(line.contains("value=42 count=3"), line);
        assertEquals(1, line.split("\\[AURA\\]", -1).length - 1, "category tag duplicated: " + line);
    }

    @Test
    void categoryMismatchStillLetsCriticalMessagesThrough() {
        session.start(EnumSet.of(LogLevel.ERROR), ProfileCategory.WORLD);

        logger.warn("critical");
        assertEquals(1, session.capturedLines().size());

        logger.error("critical too");
        assertEquals(2, session.capturedLines().size());
    }

    @Test
    void onceLogsOnlyTheFirstOccurrencePerKeyAndCategory() {
        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.ALL);

        logger.once("startup", LogLevel.INFO, "first");
        logger.once("startup", LogLevel.INFO, "second");
        ArcaneLogger.forCategory(ProfileCategory.WORLD).once("startup", LogLevel.INFO, "other category");

        List<String> lines = session.capturedLines();
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("first"));
        assertTrue(lines.get(1).contains("other category"));
    }

    @Test
    void timerRecordsOnlyWhileItsCategoryIsActive() {
        try (ArcaneLogger.Timing ignored = logger.timer("aura.idle")) {
            // no work
        }
        assertTrue(session.statusLines().stream().anyMatch(line -> line.contains("Timings      : none")));

        session.start(EnumSet.of(LogLevel.INFO), ProfileCategory.AURA);
        try (ArcaneLogger.Timing ignored = logger.timer("aura.tick")) {
            // no work
        }
        assertTrue(session.statusLines().stream().anyMatch(line -> line.contains("aura.tick")));
    }

    @Test
    void throwableIsAppendedToTheCapturedLine() {
        session.start(EnumSet.of(LogLevel.ERROR), ProfileCategory.AURA);

        logger.error("failed", new IllegalStateException("boom"));

        String line = session.capturedLines().get(0);
        assertTrue(line.contains("failed"), line);
        assertTrue(line.contains("IllegalStateException"), line);
        assertTrue(line.contains("boom"), line);
    }

    @Test
    void loggerCarriesItsCategory() {
        assertEquals(ProfileCategory.AURA, logger.category());
        assertFalse(ArcaneLogger.forCategory(ProfileCategory.CORE).isEnabled(LogLevel.DEBUG));
    }

    private String counterLine() {
        return session.statusLines().stream()
                .filter(line -> line.startsWith("Log lines    : "))
                .map(line -> line.substring("Log lines    : ".length()))
                .findFirst().orElse("");
    }
}
