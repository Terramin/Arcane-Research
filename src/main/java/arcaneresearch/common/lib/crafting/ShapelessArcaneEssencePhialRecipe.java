package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.items.consumables.ItemPhial;
import arcaneresearch.common.items.resources.ItemWispEssence;
import javax.annotation.Nullable;
public class ShapelessArcaneEssencePhialRecipe extends ShapelessArcaneRecipe {
    public ShapelessArcaneEssencePhialRecipe(ResourceLocation id, String group,
                                             net.minecraft.core.NonNullList<net.minecraft.world.item.crafting.Ingredient> ingredients,
                                             ItemStack result, int visCost, AspectList crystals, String research) {
        super(id, group, ingredients, result, visCost, crystals, research);
    }
    @Override
    public boolean matches(IArcaneWorkbench container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        Aspect aspect = null;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemWispEssence)) {
                continue;
            }
            Aspect contained = ItemWispEssence.getAspect(stack);
            if (contained == null) {
                return false;
            }
            if (aspect == null) {
                aspect = contained;
            } else if (!aspect.getTag().equals(contained.getTag())) {
                return false;
            }
        }
        return aspect != null;
    }
    @Override
    public ItemStack assemble(IArcaneWorkbench container, RegistryAccess registryAccess) {
        Aspect aspect = null;
        int total = 0;
        boolean mixed = false;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemWispEssence wisp)) {
                continue;
            }
            AspectList contained = wisp.getAspects(stack);
            if (contained == null || contained.size() <= 0) {
                continue;
            }
            Aspect a = contained.getAspects()[0];
            if (aspect == null) {
                aspect = a;
            } else if (!aspect.getTag().equals(a.getTag())) {
                mixed = true;
            }
            total += contained.getAmount(a);
        }
        if (aspect == null || mixed || total <= 0) {
            return super.assemble(container, registryAccess);
        }
        return ItemPhial.makePhial(aspect, Math.min(ItemPhial.PHIAL_CAPACITY, total));
    }
    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Serializer extends ShapelessArcaneRecipe.Serializer {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public ShapelessArcaneEssencePhialRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapelessArcaneRecipe base = super.fromJson(recipeId, json);
            return new ShapelessArcaneEssencePhialRecipe(recipeId, base.getGroup(),
                    base.getIngredients(), base.getResultItem(null), base.getVis(),
                    base.getCrystals(), base.getResearch());
        }
        @Override
        public @Nullable ShapelessArcaneEssencePhialRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapelessArcaneRecipe base = super.fromNetwork(recipeId, buffer);
            return new ShapelessArcaneEssencePhialRecipe(recipeId, base.getGroup(),
                    base.getIngredients(), base.getResultItem(null), base.getVis(),
                    base.getCrystals(), base.getResearch());
        }
    }
}
