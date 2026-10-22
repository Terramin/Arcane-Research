package thaumictinker.enchanting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.level.Level;

 




 
public class SpellClothRecipe extends CustomRecipe {

    public SpellClothRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        boolean cloth = false;
        boolean enchanted = false;
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.isEnchanted() && !EnchantmentManager.isNoRemoveEnchant(stack) && !enchanted) {
                enchanted = true;
            } else if (stack.is(EnchantingFeature.SPELL_CLOTH) && !cloth) {
                cloth = true;
            } else {
                return false;
            }
        }
        return cloth && enchanted;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registryAccess) {
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEnchanted() && !EnchantmentManager.isNoRemoveEnchant(stack)) {
                ItemStack result = stack.copy();
                result.setCount(1);
                if (result.getTag() != null) {
                    result.getTag().remove("Enchantments");
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.is(EnchantingFeature.SPELL_CLOTH)) {
                ItemStack used = stack.copy();
                used.setCount(1);
                used.setDamageValue(used.getDamageValue() + 1);
                if (used.getDamageValue() < used.getMaxDamage()) {
                    result.set(slot, used);
                }
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return EnchantingFeature.SPELL_CLOTH_SERIALIZER;
    }
}
