package thaumictinker.mechanism;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

 



 
public abstract class ItemHolderBlock extends Block implements EntityBlock {

    protected ItemHolderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.getBlockEntity(pos) instanceof ItemHolderBlockEntity holder) {
            if (!holder.isEmpty()) {
                take(level, player, holder);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!stack.isEmpty() && stack.getCount() > 0 && holder.accepts(stack)) {
                if (!level.isClientSide) {
                    holder.setTheItem(stack.copyWithCount(1));
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ItemHolderBlockEntity holder) {
                holder.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void take(Level level, Player player, ItemHolderBlockEntity holder) {
        if (level.isClientSide) {
            return;
        }
        ItemStack taken = holder.removeTheItem();
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
    }
}
