package arcaneresearch.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;

import java.util.List;

public class InfusionRecipe implements IArcaneresearchRecipe {

    public AspectList aspects;

    public String research;

    protected NonNullList<Ingredient> components = NonNullList.create();

    public Ingredient sourceInput;

    public Object recipeOutput;

    public int instability;

    private String group = "";

    public InfusionRecipe(String research, Object outputResult, int instability,
                          AspectList aspects, Object centralItem, Object... recipe) {
        this.research = research;
        this.recipeOutput = outputResult;
        this.aspects = aspects;
        this.instability = instability;

        this.sourceInput = ArcaneresearchApiHelper.getIngredient(centralItem);
        if (sourceInput == null) {
            throw new RuntimeException("Invalid infusion central item: " + centralItem);
        }

        for (Object ingredient : recipe) {
            Ingredient ing = ArcaneresearchApiHelper.getIngredient(ingredient);
            if (ing != null) {
                components.add(ing);
            } else {
                StringBuilder error = new StringBuilder("Invalid infusion recipe: ");
                for (Object tmp : recipe) {
                    error.append(tmp).append(", ");
                }
                error.append(outputResult);
                throw new RuntimeException(error.toString());
            }
        }
    }

    public boolean matches(List<ItemStack> pedestalItems, ItemStack centralItem,
                          Level level, Player player) {
        if (getRecipeInput() == null) return false;

        if (!ArcaneresearchCapabilities.isResearchKnown(player, research)) {
            return false;
        }

        // NEOFORGE_PORT: Ingredient.EMPTY singleton removed -> isEmpty() check (same meaning:
        //   recipe has no central item requirement).
        boolean centralMatches = getRecipeInput() == null || getRecipeInput().isEmpty() ||
                                 getRecipeInput().test(centralItem);
        boolean componentsMatch = RecipeMatcher.findMatches(pedestalItems, getComponents()) != null;

        return centralMatches && componentsMatch;
    }

    @Override
    public String getResearch() {
        return research;
    }

    public Ingredient getRecipeInput() {
        return sourceInput;
    }

    public NonNullList<Ingredient> getComponents() {
        return components;
    }

    public Object getRecipeOutput() {
        return recipeOutput;
    }

    public AspectList getAspects() {
        return aspects;
    }

    public Object getRecipeOutput(Player player, ItemStack input, List<ItemStack> components) {
        return recipeOutput;
    }

    public AspectList getAspects(Player player, ItemStack input, List<ItemStack> components) {
        return aspects;
    }

    public int getInstability(Player player, ItemStack input, List<ItemStack> components) {
        return instability;
    }

    public String getGroup() {
        return group;
    }

    public InfusionRecipe setGroup(Identifier location) {
        this.group = location.toString();
        return this;
    }

    public InfusionRecipe setGroup(String group) {
        this.group = group;
        return this;
    }
}
