package thaumictinker.kami.tool;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

 


 
public class AwakenedSwordItem extends SwordItem implements AwakenedTool {

    public AwakenedSwordItem(Tier tier, int attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public ToolKind kind() {
        return ToolKind.SWORD;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return useAwakened(level, player, hand, super.use(level, player, hand));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        boolean result = super.mineBlock(stack, level, state, pos, owner);
        mineBlockAwakened(stack, level, state, pos, owner);
        return result;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return destroySpeedAwakened(super.getDestroySpeed(stack, state), state);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendAwakenedTooltip(tooltip, stack);
    }
}
