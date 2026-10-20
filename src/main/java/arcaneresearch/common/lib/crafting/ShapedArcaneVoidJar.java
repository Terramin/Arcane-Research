package arcaneresearch.common.lib.crafting;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import arcaneresearch.api.aspects.AspectList;
@Deprecated
public class ShapedArcaneVoidJar extends ShapedArcaneVoidJarRecipe {
    public ShapedArcaneVoidJar(ResourceLocation id, String group, int width, int height,
                               NonNullList<Ingredient> ingredients, ItemStack result,
                               int visCost, AspectList crystals, String research) {
        super(id, group, width, height, ingredients, result, visCost, crystals, research);
    }
}
