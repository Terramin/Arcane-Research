package thaumictinker.kami.tool;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

 





 
public interface AwakenedTool {

    float SHEARS_LEAVES = 15.0F;
    float SHEARS_WOOL = 5.0F;

    ToolKind kind();

      
    default InteractionResultHolder<ItemStack> useAwakened(Level level, Player player, InteractionHand hand,
                                                           InteractionResultHolder<ItemStack> fallback) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            ToolHandler.changeMode(stack);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return fallback;
    }

      
    default void mineBlockAwakened(ItemStack stack, Level level, BlockState state, BlockPos pos,
                                   LivingEntity owner) {
        if (kind() != ToolKind.SWORD && level instanceof ServerLevel serverLevel
                && owner instanceof ServerPlayer player && !ToolHandler.BREAKING_EXTRA) {
            ToolHandler.breakExtra(kind(), stack, serverLevel, player, pos, state);
        }
    }

      
    default float destroySpeedAwakened(float base, BlockState state) {
        if (kind() != ToolKind.AXE) {
            return base;
        }
        if (state.is(BlockTags.LEAVES) || state.is(Blocks.COBWEB)
                || state.getBlock() instanceof VineBlock) {
            return Math.max(base, SHEARS_LEAVES);
        }
        if (state.is(BlockTags.WOOL)) {
            return Math.max(base, SHEARS_WOOL);
        }
        return base;
    }

    default void appendAwakenedTooltip(List<Component> tooltip, ItemStack stack) {
        tooltip.add(ToolHandler.modeName(kind(), ToolHandler.getMode(stack)));
    }
}
