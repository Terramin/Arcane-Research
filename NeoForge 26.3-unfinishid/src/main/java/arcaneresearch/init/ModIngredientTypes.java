package arcaneresearch.init;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.IngredientNBTTC;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// NEOFORGE_PORT: new file, no Forge counterpart (Forge used CraftingHelper.register()
//   for its custom ingredient serializer in Arcaneresearch.registerIngredientTypes).
// NeoForge custom ingredients need a registered IngredientType (codec + stream codec)
// for recipe JSON parsing and client sync. Registry name preserved 1:1:
// Forge CrystalAspectIngredient.TYPE_ID was arcaneresearch:crystal; NBTTC registers
// as arcaneresearch:nbt_test.
public class ModIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Arcaneresearch.MODID);

    public static void register(IEventBus modEventBus) {
        INGREDIENT_TYPES.register(modEventBus);
    }

    public static final net.neoforged.neoforge.registries.DeferredHolder<IngredientType<?>, IngredientType<IngredientNBTTC>> NBT_TEST =
            INGREDIENT_TYPES.register("nbt_test", () -> IngredientNBTTC.TYPE);

    public static final net.neoforged.neoforge.registries.DeferredHolder<IngredientType<?>, IngredientType<arcaneresearch.common.lib.crafting.CrystalAspectIngredient>> CRYSTAL =
            INGREDIENT_TYPES.register("crystal", () -> arcaneresearch.common.lib.crafting.CrystalAspectIngredient.TYPE);
}
