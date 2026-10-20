package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.ItemEssentiaContainer;
import arcaneresearch.common.items.consumables.ItemLabel;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/RecipeLabelFill.java.
// 26.x changes only: vanilla ShapelessRecipe ctor is now (CommonInfo, CraftingBookInfo,
// ItemStackTemplate, List<Ingredient>); matches/assemble take CraftingInput;
// canCraftInDimensions is gone from the vanilla interface; old fromJson/fromNetwork/
// toNetwork serializer replaced by unit MAP_CODEC/STREAM_CODEC + new
// RecipeSerializer<>(MAP_CODEC, STREAM_CODEC) (mirrors RepairItemRecipe — the recipe
// carries no data, its inputs are fixed).
// Label+phial matching/filling logic VERBATIM.
public class RecipeLabelFill extends ShapelessRecipe {

    // NEOFORGE_PORT: unit codecs (no data); the instance touches ModItems holders, so
    // it is built lazily via LazyCodecs - serializers register before items are bound
    // (same timing as data-pack recipe load).
    // The single memoized instance is SHARED by MAP_CODEC and STREAM_CODEC:
    // recipe sync (neoforge:recipe_content) encodes with the stream codec and
    // rejects any instance but the expected one.
    private static final java.util.function.Supplier<RecipeLabelFill> INSTANCE =
            com.google.common.base.Suppliers.memoize(RecipeLabelFill::new);
    public static final MapCodec<RecipeLabelFill> MAP_CODEC = LazyCodecs.lazyUnit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeLabelFill> STREAM_CODEC =
            LazyCodecs.lazyUnitStream(INSTANCE);
    public static final RecipeSerializer<RecipeLabelFill> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public RecipeLabelFill() {
        // NEOFORGE_PORT (26.3): Forge ShapelessRecipe kept Recipe#showNotification() = true.
        super(new Recipe.CommonInfo(true),
                new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
                // NEOFORGE_PORT: fromStack(new ItemStack(...)) touches holder.components(),
                // unbound during datapack decode - wrapAsHolder stores the intrusive
                // reference instead (bound at freeze; vanilla ItemStackTemplate semantics).
                new ItemStackTemplate(
                        net.minecraft.core.registries.BuiltInRegistries.ITEM
                                .wrapAsHolder(ModItems.LABEL_FILLED.get()),
                        1, net.minecraft.core.component.DataComponentPatch.EMPTY),
                labelIngredients());
    }

    private static List<Ingredient> labelIngredients() {
        List<Ingredient> ingredients = new ArrayList<>(2);
        ingredients.add(Ingredient.of(ModItems.LABEL_BLANK.get()));
        ingredients.add(Ingredient.of(ModItems.PHIAL_FILLED.get()));
        return ingredients;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        boolean label = false;
        boolean phial = false;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(ModItems.LABEL_BLANK.get()) && !label) {
                label = true;
            } else if (stack.is(ModItems.PHIAL_FILLED.get()) && !phial
                    && stack.getItem() instanceof ItemEssentiaContainer container) {
                AspectList aspects = container.getAspects(stack);
                if (aspects == null || aspects.size() == 0) return false;
                phial = true;
            } else {
                return false;
            }
        }
        return label && phial;
    }

    @Override
    public ItemStack assemble(CraftingInput inv) {
        Aspect aspect = null;
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.PHIAL_FILLED.get())
                    && stack.getItem() instanceof ItemEssentiaContainer container) {
                AspectList aspects = container.getAspects(stack);
                if (aspects != null && aspects.size() > 0) {
                    aspect = aspects.getAspects()[0];
                    break;
                }
            }
        }
        ItemStack out = new ItemStack(ModItems.LABEL_FILLED.get());
        if (aspect != null && out.getItem() instanceof ItemLabel label) {
            label.setAspect(out, aspect);
        }
        return out;
    }

    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {

        return net.minecraft.core.NonNullList.withSize(inv.size(), ItemStack.EMPTY);
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
