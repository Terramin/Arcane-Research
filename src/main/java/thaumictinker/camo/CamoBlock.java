package thaumictinker.camo;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

 



 
public abstract class CamoBlock extends Block implements EntityBlock {

    protected CamoBlock(Properties properties) {
        super(properties.noOcclusion());
    }

    public static boolean isRenderableCamo(BlockState state) {
        return !state.isAir() && !(state.getBlock() instanceof CamoBlock);
    }

    public static boolean isValidCamo(BlockState state) {
        return isRenderableCamo(state)
                && !(state.getBlock() instanceof EntityBlock)
                && state.getRenderShape() == RenderShape.MODEL
                && (state.getBlock() instanceof SlabBlock || Block.isShapeFullBlock(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)));
    }

    @Nullable
    public static BlockState camoFor(ItemStack stack, Direction face) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        BlockState state = blockItem.getBlock().defaultBlockState();
        if (!isValidCamo(state)) {
            return null;
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && face.getAxis().isHorizontal()) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, face);
        }
        return state;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            BlockState camo = camoFor(stack, hit.getDirection());
            if (camo == null) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof CamoBlockEntity entity) {
                entity.setCamo(camo);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CamoBlockEntity entity) {
            entity.setCamo(null);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
