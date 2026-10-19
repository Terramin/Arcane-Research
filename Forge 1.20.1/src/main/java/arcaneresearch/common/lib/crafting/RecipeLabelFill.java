package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.ItemEssentiaContainer;
import arcaneresearch.common.items.consumables.ItemLabel;
import arcaneresearch.init.ModItems;
import javax.annotation.Nullable;
public class RecipeLabelFill extends ShapelessRecipe {
    public RecipeLabelFill(ResourceLocation id) {
        super(id, "", CraftingBookCategory.MISC, new ItemStack(ModItems.LABEL_FILLED.get()),
                NonNullList.of(Ingredient.EMPTY,
                        Ingredient.of(ModItems.LABEL_BLANK.get()),
                        Ingredient.of(ModItems.PHIAL_FILLED.get())));
    }
    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        boolean label = false;
        boolean phial = false;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        Aspect aspect = null;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Serializer implements RecipeSerializer<RecipeLabelFill> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public RecipeLabelFill fromJson(ResourceLocation id, JsonObject json) {
            return new RecipeLabelFill(id);
        }
        @Override
        public @Nullable RecipeLabelFill fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new RecipeLabelFill(id);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buf, RecipeLabelFill recipe) {
        }
    }
}
