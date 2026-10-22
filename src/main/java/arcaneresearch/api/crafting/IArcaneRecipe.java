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
            ItemStack stack = inv.getItem(i);
            net.minecraft.world.item.ItemStackTemplate remainder = stack.isEmpty() ? null : stack.getCraftingRemainder();
            remaining.add(remainder != null ? remainder.create() : ItemStack.EMPTY);
        }
        return remaining;
    }
}
