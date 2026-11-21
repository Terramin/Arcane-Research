package arcaneresearch.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Central logging facade for Arcaneresearch.
 *
 * <p>All mod logging should go through this class instead of creating ad-hoc
 * loggers. It provides:</p>
 * <ul>
 *   <li>one consistently named SLF4J logger ({@code arcaneresearch});</li>
 *   <li>category aware filtering driven by {@link DebugSession};</li>
 *   <li>SLF4J style parameterized messages and throwable handling;</li>
 *   <li>lightweight timing and "log once" helpers for profiling.</li>
 * </ul>
 */
public final class ArcaneLogger {
    private static final Logger ROOT = LoggerFactory.getLogger("arcaneresearch");
    private static final Object[] NO_ARGS = new Object[0];
    private static final DateTimeFormatter CAPTURE_TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final ProfileCategory category;

    private ArcaneLogger(ProfileCategory category) {
        this.category = category;
    }

    public static ArcaneLogger forCategory(ProfileCategory category) {
        return new ArcaneLogger(category);
    }

    public ProfileCategory category() {
        return category;
    }

    public boolean isEnabled(LogLevel level) {
        return DebugSession.get().isEnabled(level, category);
    }

    // ------------------------------------------------------------------ TRACE
    public void trace(String message) {
        log(LogLevel.TRACE, message, NO_ARGS, null);
    }

    public void trace(String format, Object argument) {
        log(LogLevel.TRACE, format, new Object[]{argument}, null);
    }

    public void trace(String format, Object first, Object second) {
        log(LogLevel.TRACE, format, new Object[]{first, second}, null);
    }

    public void trace(String format, Object... arguments) {
        log(LogLevel.TRACE, format, arguments, null);
    }

    public void trace(String message, Throwable throwable) {
        log(LogLevel.TRACE, message, NO_ARGS, throwable);
    }

    // ------------------------------------------------------------------ DEBUG
    public void debug(String message) {
        log(LogLevel.DEBUG, message, NO_ARGS, null);
    }

    public void debug(String format, Object argument) {
        log(LogLevel.DEBUG, format, new Object[]{argument}, null);
    }

    public void debug(String format, Object first, Object second) {
        log(LogLevel.DEBUG, format, new Object[]{first, second}, null);
    }

    public void debug(String format, Object... arguments) {
        log(LogLevel.DEBUG, format, arguments, null);
    }

    public void debug(String message, Throwable throwable) {
        log(LogLevel.DEBUG, message, NO_ARGS, throwable);
    }

    // ------------------------------------------------------------------- INFO
    public void info(String message) {
        log(LogLevel.INFO, message, NO_ARGS, null);
    }

    public void info(String format, Object argument) {
        log(LogLevel.INFO, format, new Object[]{argument}, null);
    }

    public void info(String format, Object first, Object second) {
        log(LogLevel.INFO, format, new Object[]{first, second}, null);
    }

    public void info(String format, Object... arguments) {
        log(LogLevel.INFO, format, arguments, null);
    }

    public void info(String message, Throwable throwable) {
        log(LogLevel.INFO, message, NO_ARGS, throwable);
    }

    // ------------------------------------------------------------------- WARN
    public void warn(String message) {
        log(LogLevel.WARN, message, NO_ARGS, null);
    }

    public void warn(String format, Object argument) {
        log(LogLevel.WARN, format, new Object[]{argument}, null);
    }

    public void warn(String format, Object first, Object second) {
        log(LogLevel.WARN, format, new Object[]{first, second}, null);
    }

    public void warn(String format, Object... arguments) {
        log(LogLevel.WARN, format, arguments, null);
    }

    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, NO_ARGS, throwable);
    }

    // ------------------------------------------------------------------ ERROR
    public void error(String message) {
        log(LogLevel.ERROR, message, NO_ARGS, null);
    }

    public void error(String format, Object argument) {
        log(LogLevel.ERROR, format, new Object[]{argument}, null);
    }

    public void error(String format, Object first, Object second) {
        log(LogLevel.ERROR, format, new Object[]{first, second}, null);
    }

    public void error(String format, Object... arguments) {
        log(LogLevel.ERROR, format, arguments, null);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, NO_ARGS, throwable);
    }

    /** Logs a message only once per session (useful inside hot tick loops). */
    public void once(String key, LogLevel level, String format, Object... arguments) {
        if (DebugSession.get().markOnce(category.name() + ':' + key)) {
            log(level, format, arguments, null);
        }
    }

    /**
     * Starts a named timing measurement. Use with try-with-resources:
     * <pre>{@code
     * try (ArcaneLogger.Timing ignored = LOGGER.timer("aura.tick")) {
     *     ...
     * }
     * }</pre>
     * The result is only recorded while a matching debug session is active.
     */
    public Timing timer(String name) {
        return new Timing(name);
    }

    private void log(LogLevel level, String format, Object[] arguments, Throwable throwable) {
        DebugSession session = DebugSession.get();
        if (!session.isEnabled(level, category)) {
            return;
        }
        String message = format;
        if (session.isActive() && level.severity() <= LogLevel.DEBUG.severity()) {
            message = '[' + category.name() + "] " + format;
        }
        Object[] finalArguments;
        if (throwable == null) {
            finalArguments = arguments;
        } else {
            finalArguments = Arrays.copyOf(arguments, arguments.length + 1);
            finalArguments[arguments.length] = throwable;
        }
        switch (level) {
            case TRACE -> ROOT.trace(message, finalArguments);
            case DEBUG -> ROOT.debug(message, finalArguments);
            case INFO -> ROOT.info(message, finalArguments);
            case WARN -> ROOT.warn(message, finalArguments);
            case ERROR -> ROOT.error(message, finalArguments);
            case OFF -> {
                return;
            }
        }
        session.recordLog(level);
        if (session.isActive()) {
            session.captureLine(formatCapturedLine(level, message, finalArguments));
        }
    }

    private String formatCapturedLine(LogLevel level, String message, Object[] arguments) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(message, arguments);
        StringBuilder line = new StringBuilder()
                .append('[').append(LocalTime.now().format(CAPTURE_TIME)).append("] ")
                .append('[').append(Thread.currentThread().getName()).append("] ")
                .append('[').append(level.name()).append("] ")
                .append('[').append(category.name()).append("] ")
                .append(tuple.getMessage());
        Throwable throwable = tuple.getThrowable();
        if (throwable != null) {
            StringWriter writer = new StringWriter();
            throwable.printStackTrace(new PrintWriter(writer));
            line.append(System.lineSeparator()).append(writer);
        }
        return line.toString();
    }

    /** Auto-closable timing handle. Never throws from {@link #close()}. */
    public final class Timing implements AutoCloseable {
        private final String name;
        private final long startedAtNanos = System.nanoTime();
        private boolean closed;

        private Timing(String name) {
            this.name = name;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            DebugSession session = DebugSession.get();
            if (session.isTimingEnabled(category)) {
                session.recordTiming(name, System.nanoTime() - startedAtNanos);
            }
        }
    }
}
