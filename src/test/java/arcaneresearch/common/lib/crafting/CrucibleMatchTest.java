package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тигель: поведение выбора рецепта")
class CrucibleMatchTest {

    private static CrucibleMatch match(AspectList required, String research) {
        UnaryOperator<AspectList> remover = available -> {
            AspectList result = available.copy();
            for (Aspect aspect : required.getAspects()) {
                result.remove(aspect, required.getAmount(aspect));
            }
            return result;
        };
        return new CrucibleMatch(stack -> true, required, research, () -> null, remover);
    }

    private static CrucibleMatch best(List<CrucibleMatch> matches) {
        return matches.stream()
                .max(Comparator.comparingInt(CrucibleMatch::visSize))
                .orElse(null);
    }

    @Test
    @DisplayName("match requires every aspect to be available")
    void aspectRequirement() {
        CrucibleMatch recipe = match(new AspectList().add(Aspect.AIR, 2).add(Aspect.FIRE, 3), "TEST");

        assertTrue(recipe.matches(new AspectList().add(Aspect.AIR, 2).add(Aspect.FIRE, 3), null));
        assertTrue(recipe.matches(new AspectList().add(Aspect.AIR, 5).add(Aspect.FIRE, 5), null));
        assertFalse(recipe.matches(new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 3), null));
        assertFalse(recipe.matches(new AspectList().add(Aspect.AIR, 2), null));
        assertFalse(recipe.matches(null, null));
    }

    @Test
    @DisplayName("catalyst predicate is respected independently of aspects")
    void catalystGate() {
        CrucibleMatch recipe = new CrucibleMatch(stack -> false,
                new AspectList().add(Aspect.AIR, 1), "TEST", () -> null,
                available -> available.copy());
        assertFalse(recipe.matches(new AspectList().add(Aspect.AIR, 100), null));
    }

    @Test
    @DisplayName("vis size selects the most expensive matching recipe")
    void bestByVis() {
        CrucibleMatch cheap = match(new AspectList().add(Aspect.AIR, 1), "CHEAP");
        CrucibleMatch expensive = match(new AspectList()
                .add(Aspect.AIR, 10).add(Aspect.FIRE, 10), "EXPENSIVE");

        assertEquals("EXPENSIVE", best(List.of(cheap, expensive)).research());
        assertEquals("EXPENSIVE", best(List.of(expensive, cheap)).research());
        assertEquals(20, expensive.visSize());
    }

    @Test
    @DisplayName("equal vis keeps the first recipe encountered")
    void equalVisKeepsFirst() {
        CrucibleMatch first = match(new AspectList().add(Aspect.AIR, 2), "FIRST");
        CrucibleMatch second = match(new AspectList().add(Aspect.FIRE, 2), "SECOND");

        assertEquals("FIRST", best(List.of(first, second)).research());
    }

    @Test
    @DisplayName("aspect removal subtracts only the recipe cost")
    void aspectRemoval() {
        CrucibleMatch recipe = match(new AspectList().add(Aspect.AIR, 2), "TEST");
        AspectList available = new AspectList()
                .add(Aspect.AIR, 5).add(Aspect.FIRE, 7);

        AspectList left = recipe.removeAspects(available);
        assertEquals(3, left.getAmount(Aspect.AIR));
        assertEquals(7, left.getAmount(Aspect.FIRE));
        assertEquals(5, available.getAmount(Aspect.AIR), "source list must not be mutated");
    }

    @Test
    @DisplayName("null aspects from a legacy recipe are normalized to empty")
    void nullAspects() {
        CrucibleMatch recipe = new CrucibleMatch(stack -> true, null, "TEST",
                () -> null, available -> available.copy());

        assertEquals(0, recipe.visSize());
        assertTrue(recipe.matches(new AspectList().add(Aspect.AIR, 1), null));
        assertNotNull(recipe.aspects());
    }

    @Test
    @DisplayName("recipes with no research key are never null in the catalog view")
    void researchKeyIsPreserved() {
        CrucibleMatch recipe = match(new AspectList().add(Aspect.AIR, 1), "");
        assertEquals("", recipe.research());
        assertNotNull(recipe.aspects());
    }
}
