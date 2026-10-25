package arcaneresearch.api.crafting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.ItemsTC;
import arcaneresearch.init.ModRecipeTypes;
import java.util.HashMap;
import java.util.Map;
public class ShapedArcaneRecipe implements IArcaneRecipe {
    private String research;
    private int vis;
    private AspectList crystals;
    private ItemStack result;
    private NonNullList<Ingredient> ingredients;
    private int width;
    private int height;
    private String group = "";
    private ResourceLocation id;
    public ShapedArcaneRecipe(ResourceLocation group, String res, int vis, AspectList crystals, Block result, Object... recipe) {
        this(group, res, vis, crystals, new ItemStack(result), recipe);
    }
    public ShapedArcaneRecipe(ResourceLocation group, String res, int vis, AspectList crystals, Item result, Object... recipe) {
        this(group, res, vis, crystals, new ItemStack(result), recipe);
    }
    public ShapedArcaneRecipe(ResourceLocation group, String res, int vis, AspectList crystals, ItemStack result, Object... recipe) {
        this.research = res;
        this.vis = vis;
        this.crystals = crystals;
        this.result = result.copy();
        ParsedShaped parsed = parseShaped(recipe);
        this.width = parsed.width;
        this.height = parsed.height;
        this.ingredients = parsed.ingredients;
        this.group = group != null ? group.toString() : "";
        this.id = group != null ? group : new ResourceLocation("arcaneresearch", "legacy_arcane_shaped");
    }
    public ShapedArcaneRecipe(ResourceLocation group, String res, int vis, AspectList crystals, ItemStack result,
                              int width, int height, NonNullList<Ingredient> ingredients) {
        this.research = res;
        this.vis = vis;
        this.crystals = crystals;
        this.result = result.copy();
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.group = group != null ? group.toString() : "";
        this.id = group != null ? group : new ResourceLocation("arcaneresearch", "legacy_arcane_shaped");
    }
    private static final class ParsedShaped {
        final int width;
        final int height;
        final NonNullList<Ingredient> ingredients;
        ParsedShaped(int width, int height, NonNullList<Ingredient> ingredients) {
            this.width = width;
            this.height = height;
            this.ingredients = ingredients;
        }
    }
    private static ParsedShaped parseShaped(Object... recipe) {
        StringBuilder pattern = new StringBuilder();
        int width = 0;
        int height = 0;
        int idx = 0;
        if (recipe[idx] instanceof String[]) {
            String[] rows = (String[]) recipe[idx++];
            for (String row : rows) {
                height++;
                width = row.length();
                pattern.append(row);
            }
        } else {
            while (idx < recipe.length && recipe[idx] instanceof String) {
                String row = (String) recipe[idx++];
                height++;
                width = row.length();
                pattern.append(row);
            }
        }
        Map<Character, Ingredient> key = new HashMap<>();
        while (idx < recipe.length) {
            Character chr = (Character) recipe[idx++];
            Object in = recipe[idx++];
            Ingredient ing = ArcaneresearchApiHelper.getIngredient(in);
            if (ing == null) {
                throw new RuntimeException("Invalid shaped arcane recipe key entry: '" + chr + "' -> " + in);
            }
            key.put(chr, ing);
        }
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int i = 0; i < width * height; i++) {
            char c = pattern.charAt(i);
            ingredients.set(i, c == ' ' ? Ingredient.EMPTY : key.getOrDefault(c, Ingredient.EMPTY));
        }
        return new ParsedShaped(width, height, ingredients);
    }
    public ItemStack getCraftingResult(IArcaneWorkbench inv) {
        if (!(inv instanceof IArcaneWorkbench)) return ItemStack.EMPTY;
        return result.copy();
    }
    @Override
    public boolean matches(IArcaneWorkbench inv, Level level) {
        if (inv.getContainerSize() < 15) return false;
        if (crystals != null && inv.getContainerSize() >= 15) {
            for (Aspect aspect : crystals.getAspects()) {
                ItemStack cs = ArcaneresearchApiHelper.makeCrystal(aspect, crystals.getAmount(aspect));
                boolean b = false;
                for (int i = 0; i < 6; i++) {
                    ItemStack itemstack1 = inv.getItem(9 + i);
                    if (!itemstack1.isEmpty() && itemstack1.getItem() == ItemsTC.crystalEssence
                            && itemstack1.getCount() >= cs.getCount()
                            && ItemStack.isSameItemSameTags(cs, itemstack1)) {
                        b = true;
                    }
                }
                if (!b) return false;
            }
        }
        if (!(inv instanceof IArcaneWorkbench)) return false;
        for (int x = 0; x <= 3 - width; x++) {
            for (int y = 0; y <= 3 - height; y++) {
                if (matchesAt(inv, x, y, true) || matchesAt(inv, x, y, false)) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean matchesAt(IArcaneWorkbench inv, int offsetX, int offsetY, boolean mirrored) {
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
                if (!ingredient.test(inv.getItem(x + y * 3))) {
                    return false;
                }
            }
        }
        return true;
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
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }
    @Override
    public String getGroup() {
        return group;
    }
    public ShapedArcaneRecipe setGroup(ResourceLocation location) {
        this.group = location != null ? location.toString() : "";
        return this;
    }
    public ShapedArcaneRecipe setGroup(String group) {
        this.group = group != null ? group : "";
        return this;
    }
    @Override
    public ItemStack assemble(IArcaneWorkbench container, RegistryAccess registryAccess) {
        return result.copy();
    }
    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w >= width && h >= height;
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
        return arcaneresearch.common.lib.crafting.ShapedArcaneRecipe.Serializer.INSTANCE;
    }
    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ARCANE_WORKBENCH.get();
    }
}
