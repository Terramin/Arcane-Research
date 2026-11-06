package arcaneresearch.common.items.curios;

import arcaneresearch.common.items.ItemTCBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPrimordialPearl extends ItemTCBase {

    public static final int MAX_DAMAGE = 8;
    private static final int MAX_USE_DAMAGE = MAX_DAMAGE - 1;

    public ItemPrimordialPearl() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                .durability(MAX_DAMAGE), "primordial_pearl", "pearl", "nodule", "mote");
    }

    public static int getVariant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        int damage = stack.getDamageValue();
        if (damage < 3) {
            return 0;
        }
        if (damage < 6) {
            return 1;
        }
        return 2;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        String suffix = switch (getVariant(stack)) {
            case 1 -> "nodule";
            case 2 -> "mote";
            default -> "pearl";
        };
        return Component.translatable(this.getDescriptionId(stack) + "." + suffix);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !hasCraftingRemainingItem(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setDamageValue(stack.getDamageValue() + 1);
        return result;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getDamageValue() < MAX_USE_DAMAGE;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        return 0;
    }

    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        return false;
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }
}