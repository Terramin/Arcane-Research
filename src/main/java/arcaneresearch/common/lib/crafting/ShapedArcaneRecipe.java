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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModRecipeTypes;
import javax.annotation.Nullable;
import java.util.Map;
public class ShapedArcaneRecipe implements IArcaneRecipe {
    private final ResourceLocation id;
    private final String group;
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final int visCost;
    private final AspectList crystals;
    private final String research;
    public ShapedArcaneRecipe(ResourceLocation id, String group, int width, int height,
                              NonNullList<Ingredient> ingredients, ItemStack result,
                              int visCost, AspectList crystals, String research) {
        this.id = id;
        this.group = group;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
        this.visCost = visCost;
        this.crystals = crystals;
        this.research = research;
    }
    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {
        for (int x = 0; x <= 3 - width; x++) {
            for (int y = 0; y <= 3 - height; y++) {
                if (matches(container, x, y, true) || matches(container, x, y, false)) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean matches(IArcaneWorkbench container, int offsetX, int offsetY, boolean mirrored) {
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                int checkX = x - offsetX;
                int checkY = y - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (checkX >= 0 && checkY >= 0 && checkX < width && checkY < height) {
                    if (mirrored) {
                        ingredient = ingredients.get(width - checkX - 1 + checkY * width);
                    } else {
                        ingredient = ingredients.get(checkX + checkY * width);
                    }
                }
                if (!ingredient.test(container.getItem(x + y * 3))) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public ItemStack assemble(IArcaneWorkbench container, RegistryAccess registryAccess) {
        return result.copy();
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.width && height >= this.height;
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
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
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
    public static class Serializer implements RecipeSerializer<ShapedArcaneRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "arcane_workbench_shaped");
        private static Map<String, Ingredient> keyFromJson(JsonObject json) {
            Map<String, Ingredient> map = new java.util.HashMap<>();
            for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonParseException("Invalid key entry: '" + entry.getKey() + "' is not a single character symbol");
                }
                if (" ".equals(entry.getKey())) {
                    throw new JsonParseException("Invalid key entry: ' ' is a reserved symbol");
                }
                map.put(entry.getKey(), Ingredient.fromJson(entry.getValue(), false));
            }
            map.put(" ", Ingredient.EMPTY);
            return map;
        }
        private static String[] patternFromJson(JsonArray jsonArray) {
            String[] pattern = new String[jsonArray.size()];
            if (pattern.length > 3) {
                throw new JsonParseException("Invalid pattern: too many rows, 3 is maximum");
            }
            if (pattern.length == 0) {
                throw new JsonParseException("Invalid pattern: empty pattern not allowed");
            }
            for (int i = 0; i < pattern.length; i++) {
                String row = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
                if (row.length() > 3) {
                    throw new JsonParseException("Invalid pattern: too many columns, 3 is maximum");
                }
                if (i > 0 && pattern[0].length() != row.length()) {
                    throw new JsonParseException("Invalid pattern: each row must be the same width");
                }
                pattern[i] = row;
            }
            return pattern;
        }
        private static String[] shrink(String... pattern) {
            int firstNonSpace = Integer.MAX_VALUE;
            int lastNonSpace = 0;
            int firstNonEmptyRow = 0;
            int lastNonEmptyRow = pattern.length - 1;
            for (int row = 0; row < pattern.length; row++) {
                String rowStr = pattern[row];
                boolean empty = true;
                for (int col = 0; col < rowStr.length(); col++) {
                    if (rowStr.charAt(col) != ' ') {
                        firstNonSpace = Math.min(firstNonSpace, col);
                        lastNonSpace = Math.max(lastNonSpace, col);
                        empty = false;
                    }
                }
                if (!empty) {
                    if (firstNonEmptyRow == 0 && row > 0) {
                        boolean allEmpty = true;
                        for (int r = 0; r < row; r++) {
                            if (!pattern[r].trim().isEmpty()) {
                                allEmpty = false;
                                break;
                            }
                        }
                        if (allEmpty) firstNonEmptyRow = row;
                    }
                    lastNonEmptyRow = row;
                }
            }
            if (firstNonSpace == Integer.MAX_VALUE) {
                return new String[0];
            }
            String[] shrunk = new String[lastNonEmptyRow - firstNonEmptyRow + 1];
            for (int i = 0; i < shrunk.length; i++) {
                shrunk[i] = pattern[firstNonEmptyRow + i].substring(firstNonSpace, lastNonSpace + 1);
            }
            return shrunk;
        }
        private static NonNullList<Ingredient> dissolvePattern(String[] pattern, Map<String, Ingredient> key, int width, int height) {
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    String s = pattern[row].substring(col, col + 1);
                    Ingredient ingredient = key.get(s);
                    if (ingredient == null) {
                        if (!" ".equals(s)) {
                            throw new JsonParseException("Pattern references symbol '" + s + "' but it's not defined in the key");
                        }
                        ingredient = Ingredient.EMPTY;
                    }
                    ingredients.set(col + row * width, ingredient);
                }
            }
            return ingredients;
        }
        @Override
        public ShapedArcaneRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
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
            Map<String, Ingredient> key = keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            String[] pattern = shrink(patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int width = pattern[0].length();
            int height = pattern.length;
            NonNullList<Ingredient> ingredients = dissolvePattern(pattern, key, width, height);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new ShapedArcaneRecipe(recipeId, group, width, height, ingredients, result, visCost, crystals, research);
        }
        @Override
        public @Nullable ShapedArcaneRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
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
            int width = buffer.readVarInt();
            int height = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                ingredients.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack result = buffer.readItem();
            return new ShapedArcaneRecipe(recipeId, group, width, height, ingredients, result, visCost, crystals, research);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedArcaneRecipe recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.research);
            buffer.writeVarInt(recipe.visCost);
            Aspect[] aspects = recipe.crystals.getAspects();
            buffer.writeVarInt(aspects.length);
            for (Aspect aspect : aspects) {
                buffer.writeUtf(aspect.getTag());
                buffer.writeVarInt(recipe.crystals.getAmount(aspect));
            }
            buffer.writeVarInt(recipe.width);
            buffer.writeVarInt(recipe.height);
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
        }
    }
}
