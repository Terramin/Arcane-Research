package arcaneresearch.common.items.curios;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class ItemEnchantmentPlaceholder extends Item {

    public ItemEnchantmentPlaceholder(Item.Properties props) {
        super(props
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // NEOFORGE_PORT: isEnchantable(ItemStack) REMOVED from Item in 26.x (enchantability is now a
    // data component via Properties.enchantable); no override is needed (or possible) to keep
    // this item non-enchantable.
    // TODO_PORT (items): if this item must ever accept table/anvil enchantments, add the 26.x
    // enchantment component instead (Forge ref: isEnchantable returned false).

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (ItemStack, Level, List) -> (ItemStack, TooltipContext, TooltipDisplay, Consumer, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.enchanted_placeholder.text")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA));
    }
}
