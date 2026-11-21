package arcaneresearch.debug;

import java.util.EnumSet;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Verbosity levels understood by the debug session.
 *
 * <p>Severity grows from {@link #TRACE} to {@link #ERROR}. {@link #OFF} is a
 * sentinel used by the {@code /arcaneresearch debug} command to stop a session
 * and is never used as an actual logging level.</p>
 */
public enum LogLevel {
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    OFF(5);

    private final int severity;

    LogLevel(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }

    /** Case-insensitive single level lookup. {@code ALL} maps to {@link #TRACE}. */
    public static LogLevel parse(String token) {
        if (token == null) {
            return null;
        }
        String value = token.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(value)) {
            return TRACE;
        }
        for (LogLevel level : values()) {
            if (level.name().equals(value)) {
                return level;
            }
        }
        // Numeric aliases: 0=OFF ... 5=TRACE (higher means more verbose).
        return switch (value) {
            case "0" -> OFF;
            case "1" -> ERROR;
            case "2" -> WARN;
            case "3" -> INFO;
            case "4" -> DEBUG;
            case "5" -> TRACE;
            default -> null;
        };
    }

    /**
     * Parses a comma separated list of levels.
     *
     * <p>A single level is treated as a conventional minimum threshold: it
     * enables itself and every more severe level. When several levels are
     * given their thresholds are unioned. {@code ALL} enables everything and
     * {@code OFF} yields an empty set (which stops the session).</p>
     *
     * @return the enabled levels, or {@code null} when a token is invalid
     */
    public static EnumSet<LogLevel> parseSet(String input) {
        EnumSet<LogLevel> result = EnumSet.noneOf(LogLevel.class);
        if (input == null || input.isBlank()) {
            return result;
        }
        for (String raw : input.split(",")) {
            String token = raw.trim();
            if (token.isEmpty()) {
                continue;
            }
            LogLevel level = parse(token);
            if (level == null) {
                return null;
            }
            if (level == OFF) {
                result.clear();
                return result;
            }
            for (LogLevel candidate : values()) {
                if (candidate != OFF && candidate.severity >= level.severity) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }

    /** Comma separated list of accepted command values, used for messages and suggestions. */
    public static String names() {
        return java.util.stream.Stream.concat(
                        java.util.Arrays.stream(values()).map(Enum::name),
                        java.util.stream.Stream.of("ALL", "0-5"))
                .collect(Collectors.joining(", "));
    }
}
