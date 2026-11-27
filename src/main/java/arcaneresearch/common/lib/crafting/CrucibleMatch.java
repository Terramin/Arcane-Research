package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.CrucibleRecipe;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Uniform view over a datapack crucible recipe and a legacy API crucible recipe.
 * Keeps the "best match" selection independent of the recipe source.
 *
 * @param catalyst predicate testing whether a stack matches the recipe catalyst
 * @param aspects required aspects; null is normalised to an empty list
 * @param research research gate of the recipe
 * @param output supplier of the craft result
 * @param remover function subtracting the recipe cost from the crucible's aspects
 */
public record CrucibleMatch(Predicate<net.minecraft.world.item.ItemStack> catalyst,
                            AspectList aspects,
                            String research,
                            Supplier<net.minecraft.world.item.ItemStack> output,
                            UnaryOperator<AspectList> remover) {

    /** Legacy recipes may have null aspects; normalise to an empty list. */
    public CrucibleMatch {
        if (aspects == null) {
            aspects = new AspectList();
        }
    }

    /** @return true when the catalyst and all required aspects are available */
    public boolean matches(AspectList available, net.minecraft.world.item.ItemStack stack) {
        if (!catalyst.test(stack) || available == null) {
            return false;
        }
        for (Aspect aspect : aspects.getAspects()) {
            if (available.getAmount(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }

    /** @return total vis required by this recipe, used to pick the best match */
    public int visSize() {
        return aspects.visSize();
    }

    public AspectList removeAspects(AspectList available) {
        return remover.apply(available);
    }

    /** Wraps a datapack recipe loaded from JSON. */
    public static CrucibleMatch of(CrucibleRecipeType recipe) {
        return new CrucibleMatch(recipe::catalystMatches, recipe.getAspects(), recipe.getResearch(),
                recipe::getResultItem, recipe::removeMatchingAspects);
    }

    /** Wraps a recipe registered programmatically through the legacy API. */
    public static CrucibleMatch of(CrucibleRecipe recipe) {
        return new CrucibleMatch(recipe::catalystMatches, recipe.getAspects(), recipe.getResearch(),
                recipe::getRecipeOutput, recipe::removeMatching);
    }
}
