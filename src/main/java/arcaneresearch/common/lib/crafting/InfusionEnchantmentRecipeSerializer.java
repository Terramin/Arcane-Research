package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.enchantment.EnumInfusionEnchantment;
import javax.annotation.Nullable;
public class InfusionEnchantmentRecipeSerializer implements RecipeSerializer<InfusionEnchantmentRecipe> {
    public static final InfusionEnchantmentRecipeSerializer INSTANCE = new InfusionEnchantmentRecipeSerializer();
    public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "infusion_enchantment");
    @Override
    public InfusionEnchantmentRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String enchantmentName = GsonHelper.getAsString(json, "enchantment");
        EnumInfusionEnchantment enchantment = EnumInfusionEnchantment.valueOf(enchantmentName.toUpperCase());
        NonNullList<Ingredient> components = NonNullList.create();
        if (json.has("components")) {
            JsonArray componentsArray = GsonHelper.getAsJsonArray(json, "components");
            for (int i = 0; i < componentsArray.size(); i++) {
                Ingredient ingredient = Ingredient.fromJson(componentsArray.get(i), false);
                if (!ingredient.isEmpty()) {
                    components.add(ingredient);
                }
            }
        }
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
        return new InfusionEnchantmentRecipe(recipeId, enchantment, aspects, components);
    }
    @Override
    public @Nullable InfusionEnchantmentRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int enchantmentOrdinal = buffer.readVarInt();
        EnumInfusionEnchantment enchantment = EnumInfusionEnchantment.values()[enchantmentOrdinal];
        int componentCount = buffer.readVarInt();
        NonNullList<Ingredient> components = NonNullList.withSize(componentCount, Ingredient.EMPTY);
        for (int i = 0; i < componentCount; i++) {
            components.set(i, Ingredient.fromNetwork(buffer));
        }
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
        return new InfusionEnchantmentRecipe(recipeId, enchantment, aspects, components);
    }
    @Override
    public void toNetwork(FriendlyByteBuf buffer, InfusionEnchantmentRecipe recipe) {
        buffer.writeVarInt(recipe.enchantment.ordinal());
        buffer.writeVarInt(recipe.getComponents().size());
        for (Ingredient component : recipe.getComponents()) {
            component.toNetwork(buffer);
        }
        Aspect[] aspectArray = recipe.getAspects().getAspects();
        buffer.writeVarInt(aspectArray.length);
        for (Aspect aspect : aspectArray) {
            buffer.writeUtf(aspect.getTag());
            buffer.writeVarInt(recipe.getAspects().getAmount(aspect));
        }
    }
}
