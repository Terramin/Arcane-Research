package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/RecipeTripleMeatTreat.java.
// 26.x changes only: vanilla ShapelessRecipe ctor is now (CommonInfo, CraftingBookInfo,
// ItemStackTemplate, List<Ingredient>); matches/assemble take CraftingInput;
// canCraftInDimensions is gone from the vanilla interface; old fromJson/fromNetwork/
// toNetwork serializer replaced by unit MAP_CODEC/STREAM_CODEC + new
// RecipeSerializer<>(MAP_CODEC, STREAM_CODEC) (mirrors RepairItemRecipe — the recipe
// carries no data, its inputs are fixed).
// Sugar + 3 distinct meats logic VERBATIM.
public class RecipeTripleMeatTreat extends ShapelessRecipe {

    // NEOFORGE_PORT: unit codecs (no data); the instance touches ModItems holders, so
    // it is built lazily via LazyCodecs - serializers register before items are bound
    // (same timing as data-pack recipe load).
    // The single memoized instance is SHARED by MAP_CODEC and STREAM_CODEC:
    // recipe sync (neoforge:recipe_content) encodes with the stream codec and
    // rejects any instance but the expected one.
    private static final java.util.function.Supplier<RecipeTripleMeatTreat> INSTANCE =
            com.google.common.base.Suppliers.memoize(RecipeTripleMeatTreat::new);
    public static final MapCodec<RecipeTripleMeatTreat> MAP_CODEC = LazyCodecs.lazyUnit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTripleMeatTreat> STREAM_CODEC =
            LazyCodecs.lazyUnitStream(INSTANCE);
    public static final RecipeSerializer<RecipeTripleMeatTreat> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public RecipeTripleMeatTreat() {
        // NEOFORGE_PORT (26.3): Forge ShapelessRecipe kept Recipe#showNotification() = true.
        super(new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                // NEOFORGE_PORT: fromStack(new ItemStack(...)) touches holder.components(),
                // unbound during datapack decode - wrapAsHolder stores the intrusive
                // reference instead (bound at freeze; vanilla ItemStackTemplate semantics).
                new ItemStackTemplate(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .wrapAsHolder(ModItems.TRIPLE_MEAT_TREAT.get()),
                        1, net.minecraft.core.component.DataComponentPatch.EMPTY),
                treatIngredients());
    }

    private static List<Ingredient> treatIngredients() {
        List<Ingredient> ingredients = new ArrayList<>(4);
        ingredients.add(Ingredient.of(ModItems.CHUNKS_BEEF.get()));
        ingredients.add(Ingredient.of(ModItems.CHUNKS_CHICKEN.get()));
        ingredients.add(Ingredient.of(ModItems.CHUNKS_PORK.get()));
        ingredients.add(Ingredient.of(Items.SUGAR));
        return ingredients;
    }

    private static boolean isChunk(Item item) {
        return item == ModItems.CHUNKS_BEEF.get()
                || item == ModItems.CHUNKS_CHICKEN.get()
                || item == ModItems.CHUNKS_PORK.get()
                || item == ModItems.CHUNKS_FISH.get()
                || item == ModItems.CHUNKS_RABBIT.get()
                || item == ModItems.CHUNKS_MUTTON.get();
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        boolean sugar = false;
        ArrayList<Item> meats = new ArrayList<>();
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.SUGAR)) {
                if (sugar) return false;
                sugar = true;
            } else if (isChunk(stack.getItem())) {
                if (meats.contains(stack.getItem()) || meats.size() >= 3) return false;
                meats.add(stack.getItem());
            } else {
                return false;
            }
        }
        return sugar && meats.size() == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        return new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        // NEOFORGE_PORT: parameterized types are invariant, so the custom
        // SERIALIZER (Forge returned Serializer.INSTANCE verbatim) goes out
        // through a wildcard cast — same instance, same behavior.
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) SERIALIZER;
    }
}
