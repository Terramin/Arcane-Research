package arcaneresearch.api.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import arcaneresearch.api.aspects.AspectList;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 26.x Recipe<T extends RecipeInput> requires group()/showNotification()/
// placementInfo()/recipeBookCategory()/getSerializer()/getType() plus single-arg
// assemble(T). The legacy getGroup()/getResearch()/getVis()/getCrystals() API is kept
// 1:1; group() bridges to getGroup(), showNotification() is false (research toasts are
// handled by our own system), recipeBookCategory() is CRAFTING_MISC (arcane recipes are
// never meaningfully displayed in the vanilla book).
// Legacy helpers kept (NOT vanilla overrides — Recipe no longer declares them):
//   getId() (nullable; codecs cannot see the holder key, dynamic recipes set real ids),
//   getIngredients() (flattened present ingredients, for aspect generation),
//   getResultItem() (no-arg, for aspect generation),
//   getRemainingItems(IArcaneWorkbench) (defaults to all-EMPTY, mirroring the old
//   Forge Recipe default).
public interface IArcaneRecipe extends Recipe<IArcaneWorkbench>, IArcaneresearchRecipe {

    int getVis();

    @Override
    String getResearch();

    AspectList getCrystals();

    @Override
    default String getGroup() {
        return IArcaneresearchRecipe.super.getGroup();
    }

    @Override
    default String group() {
        String group = getGroup();
        return group != null ? group : "";
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    Identifier getId();

    List<Ingredient> getIngredients();

    ItemStack getResultItem();

    default List<ItemStack> getRemainingItems(IArcaneWorkbench inv) {
        List<ItemStack> remaining = new ArrayList<>(inv.size());
        for (int i = 0; i < inv.size(); i++) {
            remaining.add(ItemStack.EMPTY);
        }
        return remaining;
    }
}
