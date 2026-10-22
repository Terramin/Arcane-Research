package thaumictinker.transvector;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import arcaneresearch.common.items.wands.ItemWand;
import arcaneresearch.init.ModSounds;
import thaumictinker.camo.CamoBlock;

 


 
public class TransvectorDislocatorBlock extends CamoBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public TransvectorDislocatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && !stack.isEmpty() && stack.getItem() instanceof ItemWand) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FACING, hit.getDirection()), Block.UPDATE_ALL);
                level.playSound(null, pos, ModSounds.TOOL.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block signalBlock, BlockPos fromPos, boolean movedByPiston) {
        if (level.isClientSide) {
            return;
        }
        boolean power = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean on = state.getValue(POWERED);
        if (power && !on) {
            level.scheduleTick(pos, this, 1);
            level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_INVISIBLE);
        } else if (!power && on) {
            level.setBlock(pos, state.setValue(POWERED, false), Block.UPDATE_INVISIBLE);
        }
    }

    @Override
    public void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TransvectorDislocatorBlockEntity dislocator) {
            dislocator.receiveRedstonePulse();
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransvectorDislocatorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != TransvectorFeature.DISLOCATOR_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((TransvectorDislocatorBlockEntity) entity).tick();
    }
}
