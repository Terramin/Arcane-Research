package arcaneresearch.common.lib.crafting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import arcaneresearch.common.items.armor.ItemVoidRobeArmor;
import java.util.ArrayList;
import java.util.List;
public class RecipesVoidRobeArmorDyes extends CustomRecipe {
    public RecipesVoidRobeArmorDyes(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }
    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack armor = ItemStack.EMPTY;
        List<ItemStack> dyes = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem) stack.getItem();
                if (!(armorItem instanceof ItemVoidRobeArmor) || !armor.isEmpty()) {
                    return false;
                }
                armor = stack;
            } else {
                if (!(stack.getItem() instanceof DyeItem)) {
                    return false;
                }
                dyes.add(stack);
            }
        }
        return !armor.isEmpty() && !dyes.isEmpty();
    }
    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        ItemStack armorStack = ItemStack.EMPTY;
        int[] rgb = new int[3];
        int brightnessSum = 0;
        int count = 0;
        ArmorItem armorItem = null;
        for (int k = 0; k < inv.getContainerSize(); ++k) {
            ItemStack stack = inv.getItem(k);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ArmorItem) {
                armorItem = (ArmorItem) stack.getItem();
                if (!(armorItem instanceof ItemVoidRobeArmor) || !armorStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                armorStack = stack.copy();
                armorStack.setCount(1);
                if (armorItem instanceof ItemVoidRobeArmor robe && robe.hasCustomColor(stack)) {
                    int color = robe.getColor(stack);
                    float r = (color >> 16 & 0xFF) / 255.0f;
                    float g = (color >> 8 & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;
                    brightnessSum += (int) (Math.max(r, Math.max(g, b)) * 255.0f);
                    rgb[0] += (int) (r * 255.0f);
                    rgb[1] += (int) (g * 255.0f);
                    rgb[2] += (int) (b * 255.0f);
                    ++count;
                }
            } else {
                if (!(stack.getItem() instanceof DyeItem dye)) {
                    return ItemStack.EMPTY;
                }
                float[] comp = dye.getDyeColor().getTextureDiffuseColors();
                int r = (int) (comp[0] * 255.0f);
                int g = (int) (comp[1] * 255.0f);
                int b = (int) (comp[2] * 255.0f);
                brightnessSum += Math.max(r, Math.max(g, b));
                rgb[0] += r;
                rgb[1] += g;
                rgb[2] += b;
                ++count;
            }
        }
        if (armorItem == null) {
            return ItemStack.EMPTY;
        }
        int r = rgb[0] / count;
        int g = rgb[1] / count;
        int b = rgb[2] / count;
        float brightness = brightnessSum / (float) count;
        float max = Math.max(r, Math.max(g, b));
        r = (int) (r * brightness / max);
        g = (int) (g * brightness / max);
        b = (int) (b * brightness / max);
        int color = (r << 8) + g;
        color = (color << 8) + b;
        if (armorItem instanceof ItemVoidRobeArmor robe) {
            robe.setColor(armorStack, color);
        }
        return armorStack;
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.ARMOR_DYE;
    }
}
