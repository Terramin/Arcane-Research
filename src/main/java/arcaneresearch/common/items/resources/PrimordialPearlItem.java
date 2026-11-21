package arcaneresearch.common.items.resources;

import arcaneresearch.common.items.ItemTCBase;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class PrimordialPearlItem extends ItemTCBase {

    public static final int MAX_DAMAGE = 8;
    private static final int MAX_USE_DAMAGE = MAX_DAMAGE - 1;

    public PrimordialPearlItem(Item.Properties props) {
        super(props
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
    public Component getName(ItemStack stack) {
        String suffix = switch (getVariant(stack)) {
            case 1 -> "nodule";
            case 2 -> "mote";
            default -> "pearl";
        };
        return Component.translatable(this.getDescriptionId() + "." + suffix);
    }

    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !hasCraftingRemainingItem(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        result.setDamageValue(stack.getDamageValue() + 1);
        return result;
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getDamageValue() < MAX_USE_DAMAGE;
    }

    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
