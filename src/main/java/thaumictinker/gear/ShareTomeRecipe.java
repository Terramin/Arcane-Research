package thaumictinker.gear;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

 





 
public class ShareTomeRecipe extends ShapedRecipe {

    public ShareTomeRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height,
                           NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification) {
        super(id, group, category, width, height, ingredients, result, showNotification);
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        @Nonnull
        public ShareTomeRecipe fromJson(@Nonnull ResourceLocation id, @Nonnull JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                    GsonHelper.getAsString(json, "category", (String) null), CraftingBookCategory.MISC);
            JsonObject keyJson = GsonHelper.getAsJsonObject(json, "key");
            java.util.Map<String, Ingredient> key = new java.util.HashMap<>();
            for (java.util.Map.Entry<String, JsonElement> entry : keyJson.entrySet()) {
                if (entry.getKey().length() != 1) {
                    throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey()
                            + "' is an invalid symbol for an ingredient pattern.");
                }
                key.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
            }
            String[] pattern = shrink(patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
            int width = pattern[0].length();
            int height = pattern.length;
            NonNullList<Ingredient> ingredients = dissolvePattern(pattern, key, width, height);
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            boolean notification = GsonHelper.getAsBoolean(json, "show_notification", true);
            return new ShareTomeRecipe(id, group, category, width, height, ingredients, result, notification);
        }

        @Override
        @Nonnull
        public ShareTomeRecipe fromNetwork(@Nonnull ResourceLocation id, @Nonnull FriendlyByteBuf buf) {
            int width = buf.readVarInt();
            int height = buf.readVarInt();
            String group = buf.readUtf();
            CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int index = 0; index < ingredients.size(); index++) {
                ingredients.set(index, Ingredient.fromNetwork(buf));
            }
            ItemStack result = buf.readItem();
            boolean notification = buf.readBoolean();
            return new ShareTomeRecipe(id, group, category, width, height, ingredients, result, notification);
        }

        
        

        private static String[] patternFromJson(JsonArray array) throws JsonSyntaxException {
            String[] lines = new String[array.size()];
            if (lines.length > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
            }
            for (int row = 0; row < lines.length; row++) {
                lines[row] = GsonHelper.convertToString(array.get(row), "pattern[" + row + "]");
            }
            return lines;
        }

        private static String[] shrink(String[] rows) {
            int firstRow = 0;
            int lastRow = rows.length - 1;
            while (firstRow <= lastRow && rows[firstRow].trim().isEmpty()) {
                firstRow++;
            }
            while (lastRow >= firstRow && rows[lastRow].trim().isEmpty()) {
                lastRow--;
            }
            if (firstRow > lastRow) {
                return new String[] {""};
            }
            int firstColumn = Integer.MAX_VALUE;
            int lastColumn = -1;
            for (int row = firstRow; row <= lastRow; row++) {
                String line = rows[row];
                for (int column = 0; column < line.length(); column++) {
                    if (line.charAt(column) != ' ') {
                        firstColumn = Math.min(firstColumn, column);
                        lastColumn = Math.max(lastColumn, column);
                    }
                }
            }
            if (lastColumn < firstColumn) {
                return new String[] {""};
            }
            String[] shrunk = new String[lastRow - firstRow + 1];
            for (int row = firstRow; row <= lastRow; row++) {
                StringBuilder line = new StringBuilder(rows[row]);
                while (line.length() < lastColumn + 1) {
                    line.append(' ');
                }
                shrunk[row - firstRow] = line.substring(firstColumn, lastColumn + 1);
            }
            return shrunk;
        }

        private static NonNullList<Ingredient> dissolvePattern(String[] pattern,
                java.util.Map<String, Ingredient> key, int width, int height) {
            NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
            for (int row = 0; row < pattern.length; row++) {
                String line = pattern[row];
                for (int column = 0; column < line.length(); column++) {
                    char symbol = line.charAt(column);
                    if (symbol == ' ') {
                        continue;
                    }
                    Ingredient ingredient = key.get(String.valueOf(symbol));
                    if (ingredient == null) {
                        throw new JsonSyntaxException("Pattern references symbol '" + symbol
                                + "' but it is not defined in the key");
                    }
                    ingredients.set(row * width + column, ingredient);
                }
            }
            return ingredients;
        }
    }
}
