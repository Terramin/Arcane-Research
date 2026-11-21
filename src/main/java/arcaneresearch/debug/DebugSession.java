package arcaneresearch.debug;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Thread-safe runtime state for the debug session.
 *
 * <p>The session is controlled entirely by the client-side
 * {@code /arcaneresearch debug} command. When no session is active the logger
 * behaves conservatively: informational messages and above are written, while
 * TRACE/DEBUG messages are discarded. Once a session is active, the selected
 * levels and profile type decide what passes through.</p>
 */
public final class DebugSession {
    private static final DebugSession INSTANCE = new DebugSession();

    /** Warnings and errors are never filtered by category. */
    private static final int ALWAYS_LOG_SEVERITY = LogLevel.WARN.severity();
    private static final int DEFAULT_LOG_SEVERITY = LogLevel.INFO.severity();
    private static final int MAX_CAPTURED_LINES = 2_000;
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private volatile boolean active;
    private volatile EnumSet<LogLevel> levels = EnumSet.noneOf(LogLevel.class);
    private volatile EnumSet<ProfileCategory> categories = EnumSet.noneOf(ProfileCategory.class);
    private volatile long startedAtNanos;

    private final Map<LogLevel, LongAdder> logCounts = new ConcurrentHashMap<>();
    private final Map<String, TimingStats> timings = new ConcurrentHashMap<>();
    private final Set<String> onceKeys = ConcurrentHashMap.newKeySet();
    private final String[] capturedLines = new String[MAX_CAPTURED_LINES];
    private int capturedCursor;
    private long capturedTotal;

    private DebugSession() {
        // Off by default. Optional JVM flags allow enabling debug before the
        // client command is available (e.g. to diagnose startup problems):
        //   -Darcaneresearch.debug=DEBUG -Darcaneresearch.debug.type=AURA
        String autoLevels = System.getProperty("arcaneresearch.debug", "");
        if (!autoLevels.isBlank()) {
            EnumSet<LogLevel> parsed = LogLevel.parseSet(autoLevels);
            ProfileCategory type = ProfileCategory.parse(System.getProperty("arcaneresearch.debug.type", "ALL"));
            if (parsed != null && !parsed.isEmpty() && type != null) {
                start(parsed, type);
            }
        }
    }

    public static DebugSession get() {
        return INSTANCE;
    }

    /** Starts (or reconfigures) the session and clears previous statistics. */
    public synchronized void start(EnumSet<LogLevel> levels, ProfileCategory type) {
        this.levels = levels.isEmpty() ? EnumSet.noneOf(LogLevel.class) : levels.clone();
        this.categories = type == ProfileCategory.ALL
                ? EnumSet.allOf(ProfileCategory.class)
                : EnumSet.of(type);
        this.startedAtNanos = System.nanoTime();
        this.active = !levels.isEmpty();
        resetStats();
    }

    public synchronized void stop() {
        this.active = false;
        this.levels = EnumSet.noneOf(LogLevel.class);
        this.categories = EnumSet.noneOf(ProfileCategory.class);
    }

    /** Clears counters and timings without changing the active filters. */
    public synchronized void resetStats() {
        logCounts.clear();
        timings.clear();
        onceKeys.clear();
        java.util.Arrays.fill(capturedLines, null);
        capturedCursor = 0;
        capturedTotal = 0;
    }

    public boolean isActive() {
        return active;
    }

    public EnumSet<LogLevel> levels() {
        return levels.clone();
    }

    public EnumSet<ProfileCategory> categories() {
        return categories.clone();
    }

    /** Decides whether a message should be written. */
    public boolean isEnabled(LogLevel level, ProfileCategory category) {
        if (level == null || level == LogLevel.OFF) {
            return false;
        }
        if (level.severity() >= ALWAYS_LOG_SEVERITY) {
            return true;
        }
        if (!active) {
            return level.severity() >= DEFAULT_LOG_SEVERITY;
        }
        if (!levels.contains(level)) {
            return false;
        }
        return categories.contains(ProfileCategory.ALL) || categories.contains(category);
    }

    /** Timing capture only happens inside an active session for the selected subsystem. */
    public boolean isTimingEnabled(ProfileCategory category) {
        return active && (categories.contains(ProfileCategory.ALL) || categories.contains(category));
    }

    public void recordLog(LogLevel level) {
        logCounts.computeIfAbsent(level, ignored -> new LongAdder()).increment();
    }

    /** Returns {@code true} the first time a given key is seen (bounded set). */
    public boolean markOnce(String key) {
        if (onceKeys.size() > 4096) {
            onceKeys.clear();
        }
        return onceKeys.add(key);
    }

    public void recordTiming(String name, long nanos) {
        timings.computeIfAbsent(name, ignored -> new TimingStats()).record(nanos);
    }

    /** Stores one formatted log line in a bounded ring buffer (latest lines win). */
    public synchronized void captureLine(String line) {
        capturedLines[capturedCursor] = line;
        capturedCursor = (capturedCursor + 1) % MAX_CAPTURED_LINES;
        capturedTotal++;
    }

    public long capturedLineCount() {
        return capturedTotal;
    }

    /** Returns the captured lines in chronological order. */
    public synchronized List<String> capturedLines() {
        List<String> result = new ArrayList<>();
        int available = (int) Math.min(capturedTotal, MAX_CAPTURED_LINES);
        for (int i = 0; i < available; i++) {
            int index = (capturedCursor - available + i + MAX_CAPTURED_LINES) % MAX_CAPTURED_LINES;
            String line = capturedLines[index];
            if (line != null) {
                result.add(line);
            }
        }
        return result;
    }

    /**
     * Writes the current session (status, timings and captured log lines) to
     * {@code logs/arcaneresearch-debug-<timestamp>.log}.
     *
     * @return the path of the created file
     */
    public Path writeLogFile() throws IOException {
        Path directory = Paths.get("logs");
        Files.createDirectories(directory);
        Path file = directory.resolve("arcaneresearch-debug-" + LocalDateTime.now().format(FILE_STAMP) + ".log");

        List<String> output = new ArrayList<>();
        output.add("=== Arcaneresearch debug session export ===");
        output.addAll(statusLines());
        output.add("=== Captured log lines (showing up to " + MAX_CAPTURED_LINES
                + " of " + capturedLineCount() + ") ===");
        output.addAll(capturedLines());
        Files.write(file, output, StandardCharsets.UTF_8);
        return file;
    }

    /** Human readable status used by the {@code debug status} subcommand. */
    public List<String> statusLines() {
        List<String> lines = new ArrayList<>();
        lines.add("Status       : " + (active ? "ACTIVE" : "INACTIVE"));
        if (active) {
            double seconds = (System.nanoTime() - startedAtNanos) / 1_000_000_000.0;
            lines.add("Uptime       : " + String.format(Locale.ROOT, "%.1f s", seconds));
            lines.add("Levels       : " + formatLevels());
            lines.add("Profile type : " + formatCategories());
        }
        String counts = java.util.Arrays.stream(LogLevel.values())
                .filter(level -> level != LogLevel.OFF)
                .map(level -> {
                    LongAdder adder = logCounts.get(level);
                    return level.name() + "=" + (adder == null ? 0 : adder.sum());
                })
                .collect(Collectors.joining(" "));
        lines.add("Log lines    : " + counts);
        lines.add("Captured     : " + capturedLineCount() + " (max " + MAX_CAPTURED_LINES + ")");
        if (timings.isEmpty()) {
            lines.add("Timings      : none");
        } else {
            lines.add("Timings      : name -> count / total / avg / min / max");
            timings.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> lines.add("  " + entry.getKey() + " -> " + entry.getValue()));
        }
        Runtime runtime = Runtime.getRuntime();
        lines.add("Memory       : used " + megabytes(runtime.totalMemory() - runtime.freeMemory())
                + " MB / committed " + megabytes(runtime.totalMemory())
                + " MB / max " + megabytes(runtime.maxMemory()) + " MB");
        return lines;
    }

    private String formatLevels() {
        EnumSet<LogLevel> snapshot = levels();
        return snapshot.isEmpty() ? "OFF" : snapshot.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private String formatCategories() {
        EnumSet<ProfileCategory> snapshot = categories();
        if (snapshot.contains(ProfileCategory.ALL)) {
            return ProfileCategory.ALL.name();
        }
        return snapshot.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private static long megabytes(long bytes) {
        return bytes / (1024L * 1024L);
    }

    /** Accumulated timing information for one named operation. */
    public static final class TimingStats {
        private long count;
        private long totalNanos;
        private long minNanos = Long.MAX_VALUE;
        private long maxNanos;

        public synchronized void record(long nanos) {
            count++;
            totalNanos += nanos;
            minNanos = Math.min(minNanos, nanos);
            maxNanos = Math.max(maxNanos, nanos);
        }

        @Override
        public synchronized String toString() {
            if (count == 0) {
                return "0 / 0ms / 0ms / 0ms / 0ms";
            }
            return count + " / " + millis(totalNanos)
                    + " / " + millis((double) totalNanos / count)
                    + " / " + millis(minNanos)
                    + " / " + millis(maxNanos);
        }

        private static String millis(double nanos) {
            return String.format(Locale.ROOT, "%.2fms", nanos / 1_000_000.0);
        }
    }
}
