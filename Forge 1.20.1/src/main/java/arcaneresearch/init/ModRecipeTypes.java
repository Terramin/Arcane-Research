package arcaneresearch.init;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<RecipeType<IArcaneRecipe>> ARCANE_WORKBENCH =
            RECIPE_TYPES.register("arcane_workbench", () -> new RecipeType<IArcaneRecipe>() {
                @Override
                public String toString() {
                    return new ResourceLocation(Arcaneresearch.MODID, "arcane_workbench").toString();
                }
            });
    public static final RegistryObject<RecipeType<CrucibleRecipeType>> CRUCIBLE =
            RECIPE_TYPES.register("crucible", () -> new RecipeType<CrucibleRecipeType>() {
                @Override
                public String toString() {
                    return new ResourceLocation(Arcaneresearch.MODID, "crucible").toString();
                }
            });
    public static final RegistryObject<RecipeType<InfusionRecipeType>> INFUSION =
            RECIPE_TYPES.register("infusion", () -> new RecipeType<InfusionRecipeType>() {
                @Override
                public String toString() {
                    return new ResourceLocation(Arcaneresearch.MODID, "infusion").toString();
                }
            });
}
