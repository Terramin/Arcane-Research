package arcaneresearch.common.lib.research;

/** Pure parsing rules used when a written Thaumonomicon transfers stored knowledge. */
public final class WrittenBookRules {

    public enum KnowledgeType {
        THEORY, OBSERVATION
    }

    public record KnowledgeKey(KnowledgeType type, String category) {
    }

    private WrittenBookRules() {
    }

    /**
     * Stored knowledge keys use the legacy format {@code T_CATEGORY} / {@code O_CATEGORY}.
     *
     * @return the parsed key, or {@code null} for malformed/empty prefixes
     */
    public static KnowledgeKey parseKnowledgeKey(String key) {
        if (key == null || key.length() < 3) {
            return null;
        }
        int split = key.indexOf('_');
        if (split <= 0) {
            return null;
        }
        KnowledgeType type = switch (key.substring(0, split)) {
            case "T" -> KnowledgeType.THEORY;
            case "O" -> KnowledgeType.OBSERVATION;
            default -> null;
        };
        return type == null ? null : new KnowledgeKey(type, key.substring(split + 1));
    }

    /** Only the {@code PAGE} flag is transferred; unknown tokens are ignored. */
    public static boolean grantsPageFlag(String flags) {
        if (flags == null || flags.isEmpty()) {
            return false;
        }
        for (String token : flags.split(",")) {
            if ("PAGE".equals(token)) {
                return true;
            }
        }
        return false;
    }

    /** A stored amount never reduces the reader's current knowledge. */
    public static int positiveDelta(int storedAmount, int currentAmount) {
        return Math.max(0, storedAmount - currentAmount);
    }
}
