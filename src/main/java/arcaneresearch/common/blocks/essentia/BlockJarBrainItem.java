package arcaneresearch.common.blocks.essentia;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class BlockJarBrainItem extends BlockJarItem {

    public BlockJarBrainItem(Block block, net.minecraft.world.item.Item.Properties properties) {
        super(block, properties);
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        net.minecraft.nbt.CompoundTag tag = arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
        if (tag != null && tag.contains("xp")) {
            int xp = tag.getIntOr("xp", 0);
            if (xp > 0) {
                tooltip.accept(Component.literal(xp + " xp").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
