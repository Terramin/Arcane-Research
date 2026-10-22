package arcaneresearch.init;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionEnchantmentRecipe;
import arcaneresearch.common.lib.crafting.InfusionEnchantmentRecipeSerializer;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.common.lib.crafting.RecipeLabelFill;
import arcaneresearch.common.lib.crafting.RecipeMagicDust;
import arcaneresearch.common.lib.crafting.RecipeTripleMeatTreat;
import arcaneresearch.common.lib.crafting.ShapedArcaneRecipe;
import arcaneresearch.common.lib.crafting.ShapedArcaneVoidJarRecipe;
import arcaneresearch.common.lib.crafting.ShapelessArcaneEssencePhialRecipe;
import arcaneresearch.common.lib.crafting.ShapelessArcaneRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;










public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Arcaneresearch.MODID);

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedArcaneRecipe>> ARCANE_WORKBENCH_SHAPED =
            RECIPE_SERIALIZERS.register("arcane_workbench_shaped", () -> ShapedArcaneRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessArcaneRecipe>> ARCANE_WORKBENCH_SHAPELESS =
            RECIPE_SERIALIZERS.register("arcane_workbench_shapeless", () -> ShapelessArcaneRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CrucibleRecipeType>> CRUCIBLE =
            RECIPE_SERIALIZERS.register("crucible", () -> CrucibleRecipeType.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionRecipeType>> INFUSION =
            RECIPE_SERIALIZERS.register("infusion", () -> InfusionRecipeType.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<InfusionEnchantmentRecipe>> INFUSION_ENCHANTMENT =
            RECIPE_SERIALIZERS.register("infusion_enchantment", () -> InfusionEnchantmentRecipeSerializer.INSTANCE);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeMagicDust>> SALIS_MUNDUS =
            RECIPE_SERIALIZERS.register("salis_mundus", () -> RecipeMagicDust.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeTripleMeatTreat>> TRIPLE_MEAT_TREAT =
            RECIPE_SERIALIZERS.register("triple_meat_treat", () -> RecipeTripleMeatTreat.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeLabelFill>> LABEL_FILL =
            RECIPE_SERIALIZERS.register("label_fill", () -> RecipeLabelFill.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedArcaneVoidJarRecipe>> ARCANE_WORKBENCH_VOID_JAR =
            RECIPE_SERIALIZERS.register("arcane_workbench_void_jar", () -> ShapedArcaneVoidJarRecipe.SERIALIZER);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessArcaneEssencePhialRecipe>> ARCANE_WORKBENCH_ESSENCE_PHIAL =
            RECIPE_SERIALIZERS.register("arcane_workbench_essence_phial", () -> ShapelessArcaneEssencePhialRecipe.SERIALIZER);
}
