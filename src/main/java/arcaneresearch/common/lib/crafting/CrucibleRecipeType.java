package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneresearchRecipe;
import arcaneresearch.init.ModRecipeTypes;
import javax.annotation.Nullable;
public class CrucibleRecipeType implements Recipe<Container>, IArcaneresearchRecipe {
    private final ResourceLocation id;
    private final String group;
    private final Ingredient catalyst;
    private final AspectList aspects;
    private final ItemStack result;
    private final String research;
    public CrucibleRecipeType(ResourceLocation id, String group, Ingredient catalyst,
                              AspectList aspects, ItemStack result, String research) {
        this.id = id;
        this.group = group;
        this.catalyst = catalyst;
        this.aspects = aspects;
        this.result = result;
        this.research = research;
    }
    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }
    public boolean matchesCrucible(AspectList crucibleAspects, ItemStack catalystStack) {
        if (!catalyst.test(catalystStack)) return false;
        if (crucibleAspects == null) return false;
        for (Aspect aspect : aspects.getAspects()) {
            if (crucibleAspects.getAmount(aspect) < aspects.getAmount(aspect)) {
                return false;
            }
        }
        return true;
    }
    public boolean catalystMatches(ItemStack catalystStack) {
        return catalyst.test(catalystStack);
    }
    public AspectList removeMatchingAspects(AspectList crucibleAspects) {
        AspectList result = crucibleAspects.copy();
        for (Aspect aspect : aspects.getAspects()) {
            result.remove(aspect, aspects.getAmount(aspect));
        }
        return result;
    }
    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result;
    }
    public ItemStack getResultItem() {
        return result.copy();
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
        return ModRecipeTypes.CRUCIBLE.get();
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, catalyst);
    }
    @Override
    public String getGroup() {
        return group;
    }
    @Override
    public String getResearch() {
        return research;
    }
    public Ingredient getCatalyst() {
        return catalyst;
    }
    public AspectList getAspects() {
        return aspects;
    }
    public static class Serializer implements RecipeSerializer<CrucibleRecipeType> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "crucible");
        @Override
        public CrucibleRecipeType fromJson(ResourceLocation recipeId, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            String research = GsonHelper.getAsString(json, "research", "");
            JsonElement catalystJson = json.has("catalyst") ? json.get("catalyst") : json.get("ingredient");
            Ingredient catalyst = catalystJson != null ? Ingredient.fromJson(catalystJson, false) : Ingredient.EMPTY;
            AspectList aspects = new AspectList();
            if (json.has("aspects")) {
                JsonObject aspectsJson = GsonHelper.getAsJsonObject(json, "aspects");
                for (String aspectName : aspectsJson.keySet()) {
                    Aspect aspect = Aspect.getAspect(aspectName);
                    if (aspect != null) {
                        aspects.add(aspect, aspectsJson.get(aspectName).getAsInt());
                    }
                }
            }
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            return new CrucibleRecipeType(recipeId, group, catalyst, aspects, result, research);
        }
        @Override
        public @Nullable CrucibleRecipeType fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            String research = buffer.readUtf();
            Ingredient catalyst = Ingredient.fromNetwork(buffer);
            AspectList aspects = new AspectList();
            int aspectCount = buffer.readVarInt();
            for (int i = 0; i < aspectCount; i++) {
                String aspectName = buffer.readUtf();
                int amount = buffer.readVarInt();
                Aspect aspect = Aspect.getAspect(aspectName);
                if (aspect != null) {
                    aspects.add(aspect, amount);
                }
            }
            ItemStack result = buffer.readItem();
            return new CrucibleRecipeType(recipeId, group, catalyst, aspects, result, research);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrucibleRecipeType recipe) {
            buffer.writeUtf(recipe.group);
            buffer.writeUtf(recipe.research);
            recipe.catalyst.toNetwork(buffer);
            Aspect[] aspectArray = recipe.aspects.getAspects();
            buffer.writeVarInt(aspectArray.length);
            for (Aspect aspect : aspectArray) {
                buffer.writeUtf(aspect.getTag());
                buffer.writeVarInt(recipe.aspects.getAmount(aspect));
            }
            buffer.writeItem(recipe.result);
        }
    }
}
