package arcaneresearch.init;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.crafting.*;
public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Arcaneresearch.MODID);
    public static final RegistryObject<RecipeSerializer<ShapedArcaneRecipe>> ARCANE_WORKBENCH_SHAPED =
            RECIPE_SERIALIZERS.register("arcane_workbench_shaped", () -> ShapedArcaneRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ShapelessArcaneRecipe>> ARCANE_WORKBENCH_SHAPELESS =
            RECIPE_SERIALIZERS.register("arcane_workbench_shapeless", () -> ShapelessArcaneRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<CrucibleRecipeType>> CRUCIBLE =
            RECIPE_SERIALIZERS.register("crucible", () -> CrucibleRecipeType.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<InfusionRecipeType>> INFUSION =
            RECIPE_SERIALIZERS.register("infusion", () -> InfusionRecipeType.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT =
            RECIPE_SERIALIZERS.register("infusion_enchantment", () -> InfusionEnchantmentRecipeSerializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<RecipeMagicDust>> SALIS_MUNDUS =
            RECIPE_SERIALIZERS.register("salis_mundus", () -> RecipeMagicDust.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<RecipeTripleMeatTreat>> TRIPLE_MEAT_TREAT =
            RECIPE_SERIALIZERS.register("triple_meat_treat", () -> RecipeTripleMeatTreat.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<RecipeLabelFill>> LABEL_FILL =
            RECIPE_SERIALIZERS.register("label_fill", () -> RecipeLabelFill.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ShapedArcaneRecipe>> ARCANE_WORKBENCH_VOID_JAR =
            RECIPE_SERIALIZERS.register("arcane_workbench_void_jar", () -> ShapedArcaneVoidJarRecipe.Serializer.INSTANCE);
    public static final RegistryObject<RecipeSerializer<ShapelessArcaneRecipe>> ARCANE_WORKBENCH_ESSENCE_PHIAL =
            RECIPE_SERIALIZERS.register("arcane_workbench_essence_phial", () -> ShapelessArcaneEssencePhialRecipe.Serializer.INSTANCE);
}
