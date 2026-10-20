package arcaneresearch.common.lib.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import arcaneresearch.Arcaneresearch;
import net.minecraft.resources.Identifier;

// NEOFORGE_PORT: old fromJson/fromNetwork/toNetwork serializer replaced by the 26.x
// record serializer (MapCodec + StreamCodec live on InfusionEnchantmentRecipe).
// Kept as the registration holder so ModRecipeSerializers keeps its Forge-order entry;
// ID matches the Forge registry name.
public class InfusionEnchantmentRecipeSerializer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "infusion_enchantment");

    public static final RecipeSerializer<InfusionEnchantmentRecipe> INSTANCE =
            InfusionEnchantmentRecipe.SERIALIZER;

    private InfusionEnchantmentRecipeSerializer() {
    }
}
