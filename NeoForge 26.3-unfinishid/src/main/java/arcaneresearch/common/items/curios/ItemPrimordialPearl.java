package arcaneresearch.common.items.curios;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import arcaneresearch.common.items.ItemTCBase;

import java.util.function.Consumer;

public class ItemPrimordialPearl extends ItemTCBase {

    public static final int MAX_DAMAGE = 8;

    public ItemPrimordialPearl(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.EPIC)
                // NEOFORGE_PORT: Forge isRepairable()/isValidRepairItem() (both false) are REMOVED
                // in 26.x; anvil-combine repair now defaults ON via canCombineRepair, so opt out
                // here to keep the old behavior (see ItemElementalSword precedent).
                .setNoCombineRepair()
                .durability(MAX_DAMAGE), "primordial_pearl", "pearl", "nodule", "mote");
    }

    public static int getVariant(ItemStack stack) {
        int damage = stack.getDamageValue();
        if (damage < 3) {
            return 0;
        } else if (damage < 6) {
            return 1;
        } else {
            return 2;
        }
    }

    // NEOFORGE_PORT: per-stack getDescriptionId(ItemStack) is GONE in 26.x
    // (getDescriptionId() is now final, no-arg); same key via the no-arg call.
    @Override
    public Component getName(ItemStack stack) {
        String suffix = switch (getVariant(stack)) {
            case 0 -> "pearl";
            case 1 -> "nodule";
            case 2 -> "mote";
            default -> "pearl";
        };
        return Component.translatable(this.getDescriptionId() + "." + suffix);
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (ItemStack, Level, List) -> (ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    // TODO_PORT (items): Forge damage-based crafting remainder (getCraftingRemainingItem /
    // hasCraftingRemainingItem returned a +1-damage copy while damage < MAX_DAMAGE - 1) has no
    // method equivalent in 26.x - remainder is now a static ItemStackTemplate via
    // Properties.craftRemainder(...). Restore with a custom remainder hook if one fits
    // (Forge ref: damage+1 copy).

    // NEOFORGE_PORT: Forge getBurnTime override (returned 0 = non-fuel) dropped - fuel is now
    // data-driven via Properties.cookingFuel(...), and 0 matches the default anyway.

    // TODO_PORT (items): Forge isValidRepairItem() (false), canApplyAtEnchantingTable() (false),
    // isBookEnchantable() (false), isEnchantable() (false), getEnchantmentValue() (0) and
    // getRarity(stack) (UNCOMMON) are all REMOVED in 26.x with no method equivalents - repair is
    // data-driven and rarity is a component (EPIC kept from Properties above). Restore via
    // components if the old per-stack behavior is needed (Forge ref: see dropped overrides).
}
