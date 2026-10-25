package arcaneresearch.common.lib.crafting;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModItems;
import javax.annotation.Nullable;
import java.util.ArrayList;
public class RecipeTripleMeatTreat extends ShapelessRecipe {
    public RecipeTripleMeatTreat(ResourceLocation id) {
        super(id, "", CraftingBookCategory.MISC, new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get()),
                NonNullList.of(Ingredient.EMPTY,
                        Ingredient.of(ModItems.CHUNKS_BEEF.get()),
                        Ingredient.of(ModItems.CHUNKS_CHICKEN.get()),
                        Ingredient.of(ModItems.CHUNKS_PORK.get()),
                        Ingredient.of(Items.SUGAR)));
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
    public boolean matches(CraftingContainer inv, Level level) {
        boolean sugar = false;
        ArrayList<Item> meats = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
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
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        return new ItemStack(ModItems.TRIPLE_MEAT_TREAT.get());
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }
    public static class Serializer implements RecipeSerializer<RecipeTripleMeatTreat> {
        public static final Serializer INSTANCE = new Serializer();
        @Override
        public RecipeTripleMeatTreat fromJson(ResourceLocation id, JsonObject json) {
            return new RecipeTripleMeatTreat(id);
        }
        @Override
        public @Nullable RecipeTripleMeatTreat fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return new RecipeTripleMeatTreat(id);
        }
        @Override
        public void toNetwork(FriendlyByteBuf buf, RecipeTripleMeatTreat recipe) {
        }
    }
}
