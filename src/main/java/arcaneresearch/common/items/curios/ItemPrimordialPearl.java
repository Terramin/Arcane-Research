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
                
                
                
                .rarity(Rarity.UNCOMMON)
                
                
                
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

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {

        super.appendHoverText(stack, context, display, tooltip, flag);
    }

    
    
    
    @Override
    public net.minecraft.world.item.@org.jspecify.annotations.Nullable ItemStackTemplate getCraftingRemainder(
            net.minecraft.world.item.ItemInstance instance) {
        int damage = instance.getOrDefault(net.minecraft.core.component.DataComponents.DAMAGE, 0);
        if (damage >= MAX_DAMAGE - 1) {
            return null;
        }
        ItemStack result = instance instanceof ItemStack stack
                ? stack.copyWithCount(1)
                : ((net.minecraft.world.item.ItemStackTemplate) instance).create();
        result.setCount(1);
        result.setDamageValue(damage + 1);
        return net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(result);
    }

    
    

    
    
    
    
    
}
