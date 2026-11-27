package arcaneresearch.common.lib.research;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pure parser for research requirement keys. Supports the same syntax as the
 * progress capability: {@code A&&B}, {@code A||B} and {@code A@stage}.
 */
public final class ResearchKeyRules {

    /** Read-only view of a player's research progress. */
    public interface State {
        /** @return whether the entry has been discovered at all */
        boolean known(String key);

        /** @return whether the entry has been fully completed */
        boolean complete(String key);

        /** @return current stage of the entry, or 0 when unknown */
        int stage(String key);
    }

    private ResearchKeyRules() {
    }

    private static int stageOf(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** @return whether the requirement key is satisfied by the given state */
    public static boolean matches(String key, State state) {
        if (key == null) {
            return false;
        }
        if (key.isEmpty()) {
            return true;
        }
        if (key.contains("&&")) {
            for (String part : key.split("&&")) {
                if (!matches(part, state)) {
                    return false;
                }
            }
            return true;
        }
        if (key.contains("||")) {
            for (String part : key.split("\\|\\|")) {
                if (matches(part, state)) {
                    return true;
                }
            }
            return false;
        }
        if (key.contains("@")) {
            String[] parts = key.split("@");
            int requiredStage = parts.length > 1 ? stageOf(parts[1], 0) : 0;
            return state.known(parts[0]) && state.stage(parts[0]) >= requiredStage;
        }
        return state.complete(key);
    }

    /** Convenience overload for tests and callers without a full {@link State}. */
    public static boolean matches(String key, Predicate<String> known,
                                  Predicate<String> complete, Function<String, Integer> stage) {
        return matches(key, new State() {
            @Override
            public boolean known(String value) {
                return known.test(value);
            }

            @Override
            public boolean complete(String value) {
                return complete.test(value);
            }

            @Override
            public int stage(String value) {
                return stage.apply(value);
            }
        });
    }
}
