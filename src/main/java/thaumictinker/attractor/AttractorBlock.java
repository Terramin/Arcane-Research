package thaumictinker.attractor;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import arcaneresearch.api.wands.IWand;

 



 
public class AttractorBlock extends Block implements EntityBlock {

    public static final BooleanProperty PULL = BooleanProperty.create("pull");

    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    private final boolean corporeal;

    public AttractorBlock(Properties properties, boolean corporeal) {
        super(properties.noOcclusion());
        this.corporeal = corporeal;
        registerDefaultState(stateDefinition.any().setValue(PULL, Boolean.TRUE));
    }

    public boolean corporeal() {
        return corporeal;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PULL);
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
            if (corporeal && !level.isClientSide()
                    && level.getBlockEntity(pos) instanceof CorporealAttractorBlockEntity attractor
                    && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, attractor);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.BLOCKS, 1.0F, 0.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.cycle(PULL), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS, 1.0F, 0.5F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CorporealAttractorBlockEntity attractor) {
            attractor.dropContents();
        }
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return corporeal ? new CorporealAttractorBlockEntity(pos, state) : new AttractorBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<?> expected = corporeal ? AttractorFeature.CORPOREAL_ENTITY : AttractorFeature.KINETIC_ENTITY;
        if (type != expected) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> ((AttractorBlockEntity) entity).tick(tickLevel, pos, tickState);
    }
}
