package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.init.ModItems;
import javax.annotation.Nullable;
import java.util.ArrayList;
public class RecipeMagicDust extends ShapelessRecipe {
    public RecipeMagicDust(ResourceLocation id) {
        super(id, "", CraftingBookCategory.MISC, new ItemStack(ModItems.SALIS_MUNDUS.get()),
                NonNullList.of(Ingredient.EMPTY,
                        Ingredient.of(Items.BOWL),
                        Ingredient.of(Items.FLINT),
                        Ingredient.of(Items.REDSTONE),
                        new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.AIR),
                        new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.FIRE),
                        new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.WATER)));
    }
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(Items.BOWL),
                Ingredient.of(Items.FLINT),
                Ingredient.of(Items.REDSTONE),
                new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.AIR),
                new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.FIRE),
                new CrystalAspectIngredient(arcaneresearch.api.aspects.Aspect.WATER));
    }
    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        ArrayList<String> crystals = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.SALIS_MUNDUS.get());
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 6;
    }
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            ItemStack rest = ForgeHooks.getCraftingRemainingItem(stack);
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
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Serializer implements RecipeSerializer<RecipeMagicDust> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public RecipeMagicDust fromJson(ResourceLocation id, JsonObject json) {
            return new RecipeMagicDust(id);
        }
        @Override
        public @Nullable RecipeMagicDust fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new RecipeMagicDust(id);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buf, RecipeMagicDust recipe) {
        }
    }
}
