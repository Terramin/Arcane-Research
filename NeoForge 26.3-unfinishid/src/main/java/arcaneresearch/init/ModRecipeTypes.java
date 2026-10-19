package arcaneresearch.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModRecipeTypes.java (PORTED 90%).
// REQUIRED changes:
//   DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ...) -> create(Registries.RECIPE_TYPE, ...)
//   RegistryObject -> DeferredHolder (same .get() API).
// All 3 entries ported in ORIGINAL ORDER with IDENTICAL registry names.
public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Arcaneresearch.MODID);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }

    public static final DeferredHolder<RecipeType<?>, RecipeType<IArcaneRecipe>> ARCANE_WORKBENCH =
            RECIPE_TYPES.register("arcane_workbench", () -> new RecipeType<IArcaneRecipe>() {
                @Override
                public String toString() {
                    return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcane_workbench").toString();
                }
            });

    // Forge order slots 2-3 — PORTED (need common/lib/crafting — ported this phase).
    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipeType>> CRUCIBLE =
            RECIPE_TYPES.register("crucible", () -> new RecipeType<CrucibleRecipeType>() {
                @Override
                public String toString() {
                    return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "crucible").toString();
                }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRecipeType>> INFUSION =
            RECIPE_TYPES.register("infusion", () -> new RecipeType<InfusionRecipeType>() {
                @Override
                public String toString() {
                    return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "infusion").toString();
                }
            });
}
