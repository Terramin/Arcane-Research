package thaumictinker.tablet;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import arcaneresearch.api.wands.IWand;

 


 
public class DynamismTabletBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static final Direction[] ROTATION = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public DynamismTabletBlock(Properties properties) {
        super(properties.noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.FALSE));
    }

    public static Direction nextFacing(Direction facing) {
        for (int index = 0; index < ROTATION.length; index++) {
            if (ROTATION[index] == facing) {
                return ROTATION[(index + 1) % ROTATION.length];
            }
        }
        return Direction.NORTH;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && stack.getItem() instanceof IWand) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DynamismTabletBlockEntity tablet) {
                if (!state.getValue(POWERED) && !tablet.isBreaking() && tablet.swingProgress() == 0) {
                    level.setBlock(pos, state.setValue(FACING, nextFacing(state.getValue(FACING))), Block.UPDATE_ALL);
                    level.playSound(null, pos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.6F, 1.0F);
                } else {
                    player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.animationTablet.notRotatable"));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof DynamismTabletBlockEntity tablet) {
            NetworkHooks.openScreen(serverPlayer, tablet, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                @Nullable BlockPos fromPos, boolean movedByPiston) {
        if (level.isClientSide()) {
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
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DynamismTabletBlockEntity tablet
                && tablet.isRedstone() && tablet.swingProgress() == 0) {
            tablet.trigger(level);
        }
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, param);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof DynamismTabletBlockEntity tablet) {
            tablet.dropContents();
        }
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DynamismTabletBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != TabletFeature.TABLET_ENTITY) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((DynamismTabletBlockEntity) entity).tick(tickLevel);
    }
}
