package thaumictinker.kami.tool;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

  
public class AwakenedShovelItem extends ShovelItem implements AwakenedTool {

    public AwakenedShovelItem(Tier tier, float attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public ToolKind kind() {
        return ToolKind.SHOVEL;
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
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            ToolHandler.changeMode(context.getItemInHand());
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.useOn(context);
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
