package arcaneresearch.common.lib.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import arcaneresearch.common.items.armor.ItemVoidRobeArmor;

import java.util.ArrayList;
import java.util.List;

// NEOFORGE_PORT: 1:1 from Forge common/lib/crafting/RecipesVoidRobeArmorDyes.java.
// 26.x changes: same as RecipesRobeArmorDyes (no-arg CustomRecipe, CraftingInput
// signatures, own unit serializer registered as void_robe_dye; ARMOR_DYE is gone).
// Dye-blending logic VERBATIM.
public class RecipesVoidRobeArmorDyes extends CustomRecipe {

    // Unit codecs sharing one memoized instance (recipe sync encodes with the
    // stream codec and rejects any instance but the expected one).
    private static final java.util.function.Supplier<RecipesVoidRobeArmorDyes> INSTANCE =
            com.google.common.base.Suppliers.memoize(RecipesVoidRobeArmorDyes::new);
    public static final MapCodec<RecipesVoidRobeArmorDyes> MAP_CODEC = LazyCodecs.lazyUnit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipesVoidRobeArmorDyes> STREAM_CODEC =
            LazyCodecs.lazyUnitStream(INSTANCE);
    public static final RecipeSerializer<RecipesVoidRobeArmorDyes> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public RecipesVoidRobeArmorDyes() {
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack armor = ItemStack.EMPTY;
        List<ItemStack> dyes = new ArrayList<>();
        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            // NEOFORGE_PORT: ArmorItem removed; void robes are plain Items now.
            if (stack.getItem() instanceof ItemVoidRobeArmor) {
                if (!armor.isEmpty()) {
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
    public ItemStack assemble(CraftingInput inv) {
        ItemStack armorStack = ItemStack.EMPTY;
        int[] rgb = new int[3];
        int brightnessSum = 0;
        int count = 0;
        net.minecraft.world.item.Item armorItem = null;
        for (int k = 0; k < inv.size(); ++k) {
            ItemStack stack = inv.getItem(k);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof ItemVoidRobeArmor) {
                armorItem = stack.getItem();
                if (!armorStack.isEmpty()) {
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
                if (!(stack.getItem() instanceof DyeItem)) {
                    return ItemStack.EMPTY;
                }
                // NEOFORGE_PORT: DyeItem.getDyeColor removed; dye color is the DYE
                // component; diffuse colors are a packed int now (was float[3]).
                net.minecraft.world.item.DyeColor dyeColor = stack.get(
                        net.minecraft.core.component.DataComponents.DYE);
                if (dyeColor == null) {
                    return ItemStack.EMPTY;
                }
                int diffuse = dyeColor.getTextureDiffuseColor();
                int r = (diffuse >> 16) & 0xFF;
                int g = (diffuse >> 8) & 0xFF;
                int b = diffuse & 0xFF;
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
    public net.minecraft.world.item.crafting.RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
