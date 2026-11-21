package arcaneresearch.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Теория: полнота реестра карт и aid")
class TheorycraftRegistryTest {

    private static final Pattern CARD = Pattern.compile("registerCard\\((?:[\\w.]+\\.)?(\\w+)\\.class\\)");
    private static final Pattern AID = Pattern.compile("registerAid\\(new\\s+(?:[\\w.]+\\.)?(\\w+)\\s*\\(");

    /** Nested classes inside AidPortal.java. */
    private static final Set<String> NESTED_AIDS = Set.of(
            "AidPortalEnd", "AidPortalNether", "AidPortalCrimson");

    @Test
    void everyRegisteredCardAndAidHasASourceFile() {
        String registry = source("arcaneresearch/common/lib/research/theorycraft/TheoryRegistry.java");
        Set<String> names = new HashSet<>();
        List<String> missing = new ArrayList<>();
        int cards = 0;
        int aids = 0;

        Matcher cardMatcher = CARD.matcher(registry);
        while (cardMatcher.find()) {
            cards++;
            if (names.add(cardMatcher.group(1)) && !findSource(cardMatcher.group(1))) {
                missing.add("card " + cardMatcher.group(1));
            }
        }
        Matcher aidMatcher = AID.matcher(registry);
        while (aidMatcher.find()) {
            aids++;
            String aid = aidMatcher.group(1);
            boolean exists = findSource(aid)
                    || (NESTED_AIDS.contains(aid) && findSource("AidPortal"));
            if (names.add(aid) && !exists) {
                missing.add("aid " + aid);
            }
        }

        assertTrue(cards > 30, "unexpectedly few theorycraft cards: " + cards);
        assertTrue(aids > 10, "unexpectedly few theorycraft aids: " + aids);
        assertTrue(missing.isEmpty(), "missing theorycraft classes: " + missing);
    }

    @Test
    void registryDoesNotDeclareTheSameClassTwice() {
        String registry = source("arcaneresearch/common/lib/research/theorycraft/TheoryRegistry.java");
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (Pattern pattern : List.of(CARD, AID)) {
            Matcher matcher = pattern.matcher(registry);
            while (matcher.find()) {
                if (!seen.add(matcher.group(1))) {
                    duplicates.add(matcher.group(1));
                }
            }
        }
        assertTrue(duplicates.isEmpty(), "duplicate theorycraft registration: " + duplicates);
    }

    private static boolean findSource(String simpleName) {
        Path root = ResourceFiles.root().getParent().resolve("java");
        try (var files = Files.walk(root)) {
            return files.anyMatch(path -> path.getFileName().toString().equals(simpleName + ".java"));
        } catch (Exception e) {
            throw new AssertionError("Cannot search for " + simpleName, e);
        }
    }

    private static String source(String relativePath) {
        Path file = ResourceFiles.root().getParent().resolve("java").resolve(relativePath);
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new AssertionError("Cannot read " + relativePath, e);
        }
    }
}
