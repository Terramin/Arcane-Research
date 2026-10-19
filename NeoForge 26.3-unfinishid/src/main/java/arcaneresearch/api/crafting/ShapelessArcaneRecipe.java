package arcaneresearch.api.crafting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.ItemsTC;
import arcaneresearch.init.ModRecipeTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// NEOFORGE_PORT: gameplay logic 1:1 from Forge api/crafting/ShapelessArcaneRecipe.java.
// 26.x changes only:
//   Identifier replaces Identifier; Ingredient.EMPTY is gone (empty entries are
//   skipped at parse, ingredients stay a plain List<Ingredient> via ArrayList).
//   assemble(IArcaneWorkbench) single-arg; canCraftInDimensions/getResultItem(
//   RegistryAccess)/getId removed from the vanilla interface (getId/getIngredients/
//   getResultItem kept as legacy helpers declared on IArcaneRecipe).
//   placementInfo() via PlacementInfo.create (mirrors vanilla ShapelessRecipe).
//   Matching algorithm (count + RecipeMatcher) VERBATIM, including the crystal check.
public class ShapelessArcaneRecipe implements IArcaneRecipe {

    private String research;
    private int vis;
    private AspectList crystals;
    private ItemStack result;
    private List<Ingredient> ingredients;
    private String group = "";
    private Identifier id;

    public ShapelessArcaneRecipe(Identifier group, String res, int vis, AspectList crystals, Block result, Object... recipe) {
        this(group, res, vis, crystals, new ItemStack(result), recipe);
    }

    public ShapelessArcaneRecipe(Identifier group, String res, int vis, AspectList crystals, Item result, Object... recipe) {
        this(group, res, vis, crystals, new ItemStack(result), recipe);
    }

    public ShapelessArcaneRecipe(Identifier group, String res, int vis, AspectList crystals, List<Ingredient> input, ItemStack result) {
        this.research = res;
        this.vis = vis;
        this.crystals = crystals;
        this.result = result.copy();
        this.ingredients = input;
        this.group = group != null ? group.toString() : "";

        this.id = group != null ? group : Identifier.fromNamespaceAndPath("arcaneresearch", "legacy_arcane_shapeless");
    }

    public ShapelessArcaneRecipe(Identifier group, String res, int vis, AspectList crystals, ItemStack result, Object[] recipe) {
        this.research = res;
        this.vis = vis;
        this.crystals = crystals;
        this.result = result.copy();

        this.ingredients = parseShapeless(recipe);
        this.group = group != null ? group.toString() : "";
        this.id = group != null ? group : Identifier.fromNamespaceAndPath("arcaneresearch", "legacy_arcane_shapeless");
    }

    private static List<Ingredient> parseShapeless(Object[] recipe) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (Object in : recipe) {
            if (in instanceof Collection) {
                for (Object o : (Collection<?>) in) {
                    Ingredient ing = ArcaneresearchApiHelper.getIngredient(o);
                    if (ing != null && !ing.isEmpty()) {
                        ingredients.add(ing);
                    }
                }
            } else {
                Ingredient ing = ArcaneresearchApiHelper.getIngredient(in);
                if (ing == null) {
                    throw new RuntimeException("Invalid shapeless arcane recipe entry: " + in);
                }
                if (!ing.isEmpty()) {
                    ingredients.add(ing);
                }
            }
        }
        return ingredients;
    }

    public ItemStack getCraftingResult(IArcaneWorkbench inv) {
        if (!(inv instanceof IArcaneWorkbench)) return ItemStack.EMPTY;
        return result.copy();
    }

    @Override
    public boolean matches(IArcaneWorkbench inv, Level level) {
        if (inv.getContainerSize() < 9) return false;
        if (crystals != null && inv.getContainerSize() >= 15) {
            for (Aspect aspect : crystals.getAspects()) {
                ItemStack cs = ArcaneresearchApiHelper.makeCrystal(aspect, crystals.getAmount(aspect));
                boolean b = false;
                for (int i = 0; i < 6; i++) {
                    ItemStack itemstack1 = inv.getItem(9 + i);
                    if (!itemstack1.isEmpty() && itemstack1.getItem() == ItemsTC.crystalEssence
                            && itemstack1.getCount() >= cs.getCount()
                            && ItemStack.isSameItemSameComponents(cs, itemstack1)) {
                        b = true;
                    }
                }
                if (!b) return false;
            }
        }
        if (!(inv instanceof IArcaneWorkbench)) return false;
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        return inputs.size() == ingredients.size() && RecipeMatcher.findMatches(inputs, ingredients) != null;
    }

    @Override
    public int getVis() {
        return vis;
    }

    @Override
    public String getResearch() {
        return research;
    }

    @Override
    public AspectList getCrystals() {
        return crystals;
    }

    @Override
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public ShapelessArcaneRecipe setGroup(Identifier location) {
        this.group = location != null ? location.toString() : "";
        return this;
    }

    public ShapelessArcaneRecipe setGroup(String group) {
        this.group = group != null ? group : "";
        return this;
    }

    @Override
    public ItemStack assemble(IArcaneWorkbench container) {
        return result.copy();
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeSerializer<arcaneresearch.common.lib.crafting.ShapelessArcaneRecipe> getSerializer() { // NEOFORGE_PORT: Recipe bound requires concrete type

        return arcaneresearch.common.lib.crafting.ShapelessArcaneRecipe.Serializer.INSTANCE;
    }

    @Override
    public RecipeType<IArcaneRecipe> getType() { // NEOFORGE_PORT: Recipe bound requires concrete type
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }
}
