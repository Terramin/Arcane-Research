package arcaneresearch.common.items.curios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import java.util.List;
public class ItemEnchantmentPlaceholder extends Item {
    public ItemEnchantmentPlaceholder() {
        super(new Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }
    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.enchanted_placeholder.text")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_AQUA));
    }
}
