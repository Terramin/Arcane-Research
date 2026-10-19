package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModRecipeTypes;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
public class ShapelessArcaneRecipe implements IArcaneRecipe {
    private final ResourceLocation id;
    private final String group;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int visCost;
    private final AspectList crystals;
    private final String research;
    public ShapelessArcaneRecipe(ResourceLocation id, String group,
                                 NonNullList<Ingredient> ingredients, ItemStack result,
                                 int visCost, AspectList crystals, String research) {
        this.id = id;
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
        this.visCost = visCost;
        this.crystals = crystals;
        this.research = research;
    }
    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty()) {
                inputs.add(item);
            }
        }
        return inputs.size() == ingredients.size() && RecipeMatcher.findMatches(inputs, ingredients) != null;
    }
    @Override
    public ItemStack assemble(IArcaneWorkbench container, RegistryAccess registryAccess) {
        return result.copy();
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }
    @Override
    public ResourceLocation getId() {
        return id;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }
    @Override
    public String getGroup() {
        return group;
    }
    @Override
    public int getVis() {
        return visCost;
    }
    @Override
    public String getResearch() {
        return research;
    }
    @Override
    public AspectList getCrystals() {
        return crystals;
    }
    public static class Serializer implements RecipeSerializer<ShapelessArcaneRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "arcane_workbench_shapeless");
        private static NonNullList<Ingredient> itemsFromJson(JsonArray jsonArray) {
            NonNullList<Ingredient> list = NonNullList.create();
            for (int i = 0; i < jsonArray.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(jsonArray.get(i), false);
                if (!ingredient.isEmpty()) {
                    list.add(ingredient);
                }
            }
            return list;
        }
        @Override
        public ShapelessArcaneRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            String research = GsonHelper.getAsString(json, "research", "");
            int visCost = GsonHelper.getAsInt(json, "vis", 0);
            AspectList crystals = new AspectList();
            if (json.has("crystals")) {
                JsonObject crystalsJson = GsonHelper.getAsJsonObject(json, "crystals");
                for (String aspectName : crystalsJson.keySet()) {
                    Aspect aspect = Aspect.getAspect(aspectName);
                    if (aspect != null) {
                        crystals.add(aspect, crystalsJson.get(aspectName).getAsInt());
                    }
                }
            }
            NonNullList<Ingredient> ingredients = itemsFromJson(GsonHelper.getAsJsonArray(json, "ingredients"));
            if (ingredients.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless arcane recipe");
            }
            if (ingredients.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless arcane recipe. Max is 9");
            }
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new ShapelessArcaneRecipe(recipeId, group, ingredients, result, visCost, crystals, research);
        }
        @Override
        public @Nullable ShapelessArcaneRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            String research = buffer.readUtf();
            int visCost = buffer.readVarInt();
            AspectList crystals = new AspectList();
            int crystalCount = buffer.readVarInt();
            for (int i = 0; i < crystalCount; i++) {
                String aspectName = buffer.readUtf();
                int amount = buffer.readVarInt();
                Aspect aspect = Aspect.getAspect(aspectName);
                if (aspect != null) {
                    crystals.add(aspect, amount);
                }
            }
            int ingredientCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            for (int i = 0; i < ingredientCount; i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack result = buffer.readItem();
            return new ShapelessArcaneRecipe(recipeId, group, ingredients, result, visCost, crystals, research);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapelessArcaneRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.research);
            buffer.writeVarInt(recipe.visCost);
            Aspect[] aspects = recipe.crystals.getAspects();
            buffer.writeVarInt(aspects.length);
            for (Aspect aspect : aspects) {
                buffer.writeUtf(aspect.getTag());
                buffer.writeVarInt(recipe.crystals.getAmount(aspect));
            }
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
        }
    }
}
