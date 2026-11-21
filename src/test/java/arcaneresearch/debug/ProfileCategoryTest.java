package arcaneresearch.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProfileCategory: разбор категорий профилирования")
class ProfileCategoryTest {

    @ParameterizedTest
    @ValueSource(strings = {"aura", " AURA ", "AuRa"})
    void parseIsCaseInsensitive(String token) {
        assertEquals(ProfileCategory.AURA, ProfileCategory.parse(token));
    }

    @Test
    void parseRejectsUnknownAndNull() {
        assertNull(ProfileCategory.parse("NOPE"));
        assertNull(ProfileCategory.parse(null));
    }

    @Test
    void allMatchesEverythingButNeverEqualsOtherCategories() {
        for (ProfileCategory category : ProfileCategory.values()) {
            assertTrue(ProfileCategory.ALL.matches(category));
        }
        assertTrue(ProfileCategory.AURA.matches(ProfileCategory.AURA));
        assertFalse(ProfileCategory.AURA.matches(ProfileCategory.WORLD));
        assertFalse(ProfileCategory.ALL.equals(ProfileCategory.AURA));
    }

    @Test
    void everyCategoryHasADescription() {
        Arrays.stream(ProfileCategory.values()).forEach(category ->
                assertTrue(category.description() != null && !category.description().isBlank()));
    }

    @Test
    void namesListsEveryCategory() {
        String names = ProfileCategory.names();
        Arrays.stream(ProfileCategory.values()).forEach(category ->
                assertTrue(names.contains(category.name()), "missing " + category.name()));
    }
}
