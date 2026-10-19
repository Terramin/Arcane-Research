package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.init.ModBlocks;
import javax.annotation.Nullable;
public class ShapedArcaneVoidJarRecipe extends ShapedArcaneRecipe {
    public ShapedArcaneVoidJarRecipe(ResourceLocation id, String group, int width, int height,
                                     NonNullList<Ingredient> ingredients, ItemStack result,
                                     int visCost, AspectList crystals, String research) {
        super(id, group, width, height, ingredients, result, visCost, crystals, research);
    }
    @Override
    public ItemStack assemble(IArcaneWorkbench container, RegistryAccess registryAccess) {
        ItemStack res = super.assemble(container, registryAccess);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack in = container.getItem(i);
            if (!in.isEmpty() && Block.byItem(in.getItem()) == ModBlocks.JAR_NORMAL.get()) {
                if (in.hasTag()) {
                    res.setTag(in.getTag().copy());
                }
                break;
            }
        }
        return res;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Serializer extends ShapedArcaneRecipe.Serializer {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public ShapedArcaneVoidJarRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ShapedArcaneRecipe base = super.fromJson(recipeId, json);
            return new ShapedArcaneVoidJarRecipe(recipeId, base.getGroup(), base.getWidth(), base.getHeight(),
                    base.getIngredients(), base.getResultItem(null), base.getVis(), base.getCrystals(),
                    base.getResearch());
        }
        @Override
        public @Nullable ShapedArcaneVoidJarRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ShapedArcaneRecipe base = super.fromNetwork(recipeId, buffer);
            return new ShapedArcaneVoidJarRecipe(recipeId, base.getGroup(), base.getWidth(), base.getHeight(),
                    base.getIngredients(), base.getResultItem(null), base.getVis(), base.getCrystals(),
                    base.getResearch());
        }
    }
}
