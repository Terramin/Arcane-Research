package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/RecipeMagicDust.java.
// 26.x changes only: vanilla ShapelessRecipe ctor is now (CommonInfo, CraftingBookInfo,
// ItemStackTemplate, List<Ingredient>); matches/assemble/getRemainingItems take
// CraftingInput; canCraftInDimensions is gone from the vanilla interface; old
// fromJson/fromNetwork/toNetwork serializer replaced by unit MAP_CODEC/STREAM_CODEC +
// `new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC)` (mirrors RepairItemRecipe — the
// recipe carries no data, its inputs are fixed).
// CrystalAspectIngredient is an ICustomIngredient in 26.x (Ingredient is final), so the
// three crystal entries wrap via `new Ingredient(custom)`; test() semantics unchanged.
// Bowl/flint/redstone + 3 distinct crystals logic VERBATIM (remainder override kept).
public class RecipeMagicDust extends ShapelessRecipe {

    // NEOFORGE_PORT: unit codecs (no data); the instance touches ModItems holders, so
    // it is built lazily via LazyCodecs - serializers register before items are bound
    // (same timing as data-pack recipe load).
    // The single memoized instance is SHARED by MAP_CODEC and STREAM_CODEC:
    // recipe sync (neoforge:recipe_content) encodes with the stream codec and
    // rejects any instance but the expected one.
    private static final java.util.function.Supplier<RecipeMagicDust> INSTANCE =
            com.google.common.base.Suppliers.memoize(RecipeMagicDust::new);
    public static final MapCodec<RecipeMagicDust> MAP_CODEC = LazyCodecs.lazyUnit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeMagicDust> STREAM_CODEC =
            LazyCodecs.lazyUnitStream(INSTANCE);
    public static final RecipeSerializer<RecipeMagicDust> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public RecipeMagicDust() {
        super(new Recipe.CommonInfo(false),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                // NEOFORGE_PORT: fromStack(new ItemStack(...)) touches holder.components(),
                // which is unbound during datapack decode ("Components not bound yet").
                // wrapAsHolder only stores the intrusive reference (bound at freeze) -
                // identical semantics to vanilla ItemStackTemplate handling.
                new ItemStackTemplate(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .wrapAsHolder(ModItems.SALIS_MUNDUS.get()),
                        1, net.minecraft.core.component.DataComponentPatch.EMPTY),
                salisIngredients());
    }

    private static List<Ingredient> salisIngredients() {
        List<Ingredient> ingredients = new ArrayList<>(6);
        ingredients.add(Ingredient.of(Items.BOWL));
        ingredients.add(Ingredient.of(Items.FLINT));
        ingredients.add(Ingredient.of(Items.REDSTONE));
        ingredients.add(new Ingredient(new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.AIR)));
        ingredients.add(new Ingredient(new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.FIRE)));
        ingredients.add(new Ingredient(new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.WATER)));
        return ingredients;
    }

    // NEOFORGE_PORT: Forge-era legacy helper (fixed inputs, same content).
    public List<Ingredient> getIngredients() {
        return salisIngredients();
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        ArrayList<String> crystals = new ArrayList<>();
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.BOWL) && !bowl) {
                bowl = true;
            } else if (stack.is(Items.FLINT) && !flint) {
                flint = true;
            } else if (stack.is(Items.REDSTONE) && !redstone) {
                redstone = true;
            } else if (stack.getItem() instanceof ItemCrystalEssence ice) {
                AspectList aspects = ice.getAspects(stack);
                if (aspects == null || aspects.size() == 0 || crystals.size() >= 3) {
                    return false;
                }
                String tag = aspects.getAspects()[0].getTag();
                if (crystals.contains(tag)) {
                    return false;
                }
                crystals.add(tag);
            } else {
                return false;
            }
        }
        return bowl && redstone && flint && crystals.size() == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        return new ItemStack(ModItems.SALIS_MUNDUS.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            ItemStack rest = arcaneresearch.common.lib.events.CraftingEvents.getCraftingRemainingItem(stack);

            if (!stack.isEmpty() && (stack.is(Items.FLINT) || stack.is(Items.BOWL))) {
                ItemStack kept = stack.copy();
                kept.setCount(1);
                rest = kept;
            }
            remaining.set(i, rest);
        }
        return remaining;
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
