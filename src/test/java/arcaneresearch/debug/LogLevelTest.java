package arcaneresearch.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LogLevel: разбор и активация уровней")
class LogLevelTest {

    @Nested
    @DisplayName("parse")
    class Parse {

        @ParameterizedTest
        @CsvSource({
                "trace, TRACE",
                "  DEBUG  , DEBUG",
                "All, TRACE",
                "5, TRACE",
                "4, DEBUG",
                "3, INFO",
                "2, WARN",
                "1, ERROR",
                "0, OFF"
        })
        void acceptsNamesAndNumericAliases(String token, LogLevel expected) {
            assertEquals(expected, LogLevel.parse(token));
        }

        @ParameterizedTest
        @ValueSource(strings = {"BOGUS", "6", "-1", "TRACE,DEBUG"})
        void rejectsUnknownTokens(String token) {
            assertNull(LogLevel.parse(token));
        }

        @Test
        void nullTokenIsRejected() {
            assertNull(LogLevel.parse(null));
        }
    }

    @Nested
    @DisplayName("parseSet")
    class ParseSet {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", " , "})
        void blankInputMeansNothingEnabled(String input) {
            assertEquals(EnumSet.noneOf(LogLevel.class), LogLevel.parseSet(input));
        }

        @Test
        void singleLevelIsAMinimumThreshold() {
            assertEquals(EnumSet.of(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR),
                    LogLevel.parseSet("DEBUG"));
            assertEquals(EnumSet.of(LogLevel.ERROR), LogLevel.parseSet("ERROR"));
        }

        @Test
        void multipleLevelsAreUnioned() {
            assertEquals(EnumSet.of(LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR),
                    LogLevel.parseSet("WARN, INFO"));
        }

        @Test
        void orderOfTokensDoesNotMatter() {
            assertEquals(LogLevel.parseSet("INFO,ERROR"), LogLevel.parseSet("ERROR,INFO"));
        }

        @Test
        void allEnablesEverythingExceptOff() {
            EnumSet<LogLevel> expected = EnumSet.complementOf(EnumSet.of(LogLevel.OFF));
            assertEquals(expected, LogLevel.parseSet("ALL"));
            assertEquals(expected, LogLevel.parseSet("5"));
        }

        @Test
        void offAlwaysYieldsEmptySet() {
            assertEquals(EnumSet.noneOf(LogLevel.class), LogLevel.parseSet("OFF"));
            assertEquals(EnumSet.noneOf(LogLevel.class), LogLevel.parseSet("ERROR,OFF"));
            assertEquals(EnumSet.noneOf(LogLevel.class), LogLevel.parseSet("OFF,ERROR"));
        }

        @Test
        void unknownTokenInvalidatesWholeSet() {
            assertNull(LogLevel.parseSet("DEBUG,UNKNOWN"));
        }
    }

    @Test
    void severityGrowsFromTraceToOff() {
        for (LogLevel level : LogLevel.values()) {
            if (level != LogLevel.TRACE) {
                assertTrue(level.severity() > LogLevel.values()[level.ordinal() - 1].severity());
            }
        }
    }

    @Test
    void namesContainsCommandValues() {
        String names = LogLevel.names();
        assertTrue(names.contains("TRACE"));
        assertTrue(names.contains("ALL"));
        assertTrue(names.contains("0-5"));
    }
}
