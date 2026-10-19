package arcaneresearch.common.blocks.essentia;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nullable;
import java.util.List;
public class BlockJarBrainItem extends BlockJarItem {
    public BlockJarBrainItem(Block block) {
        super(block);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains("xp")) {
            int xp = stack.getTag().getInt("xp");
            if (xp > 0) {
                tooltip.add(Component.literal(xp + " xp").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
