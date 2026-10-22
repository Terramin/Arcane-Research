package arcaneresearch.common.lib.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import arcaneresearch.Arcaneresearch;
import net.minecraft.resources.Identifier;





public class InfusionEnchantmentRecipeSerializer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "infusion_enchantment");

    public static final RecipeSerializer<InfusionEnchantmentRecipe> INSTANCE =
            InfusionEnchantmentRecipe.SERIALIZER;

    private InfusionEnchantmentRecipeSerializer() {
    }
}
