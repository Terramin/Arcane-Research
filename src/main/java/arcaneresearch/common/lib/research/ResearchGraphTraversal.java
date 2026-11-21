package arcaneresearch.common.lib.research;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pure expansion of the research dependency graph.
 *
 * <p>Parents are completed first, then stage requirements, then the entry itself,
 * then siblings. A visited set makes the traversal safe for cyclic or duplicated
 * references. Keys with a {@code @stage} suffix are normalized.</p>
 */
public final class ResearchGraphTraversal {

    public record Node(List<String> parents, List<String> siblings, List<String> requiredResearch) {
    }

    private ResearchGraphTraversal() {
    }

    /** Returns research keys in the order they should be completed. */
    public static List<String> plan(String root, Function<String, Node> lookup) {
        return plan(root, lookup, key -> true);
    }

    /**
     * @param expand only keys accepted by this predicate are granted and expanded
     */
    public static List<String> plan(String root, Function<String, Node> lookup, Predicate<String> expand) {
        Set<String> visited = new LinkedHashSet<>();
        List<String> order = new ArrayList<>();
        visit(root, lookup, expand, visited, order);
        return order;
    }

    private static void visit(String rawKey, Function<String, Node> lookup, Predicate<String> expand,
                              Set<String> visited, List<String> order) {
        String key = normalize(rawKey);
        if (key.isEmpty() || !visited.add(key) || !expand.test(key)) {
            return;
        }
        Node node = lookup.apply(key);
        if (node == null) {
            // Unknown keys (including '!' scan flags) are still granted, as before.
            order.add(key);
            return;
        }
        for (String parent : node.parents()) {
            visit(parent, lookup, expand, visited, order);
        }
        for (String required : node.requiredResearch()) {
            String normalized = normalize(required);
            if (!normalized.isEmpty() && visited.add(normalized)) {
                order.add(normalized);
            }
        }
        order.add(key);
        for (String sibling : node.siblings()) {
            visit(sibling, lookup, expand, visited, order);
        }
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String key = raw.startsWith("~") ? raw.substring(1) : raw;
        int at = key.indexOf('@');
        return (at >= 0 ? key.substring(0, at) : key).trim();
    }
}
