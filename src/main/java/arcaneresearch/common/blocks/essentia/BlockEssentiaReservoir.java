package arcaneresearch.common.blocks.essentia;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.items.ItemEssentiaContainer;
import arcaneresearch.common.items.consumables.ItemPhial;
import arcaneresearch.common.items.consumables.ItemPhialEmpty;
import arcaneresearch.common.items.consumables.ItemPhialFilled;
import arcaneresearch.common.tiles.essentia.TileEssentiaReservoir;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nullable;
public class BlockEssentiaReservoir extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    public BlockEssentiaReservoir() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEssentiaReservoir reservoir) {
            ItemStack heldStack = player.getItemInHand(hand);
            if (heldStack.getItem() instanceof ItemPhialEmpty || heldStack.getItem() instanceof ItemPhial || heldStack.getItem() instanceof ItemPhialFilled || heldStack.getItem() instanceof ItemEssentiaContainer) {
                AspectList aspects = reservoir.getAspects();
                if (aspects != null && aspects.size() > 0) {
                    Aspect aspect = aspects.getAspects()[0];
                    int amount = aspects.getAmount(aspect);
                    if (amount > 0) {
                        if (heldStack.getItem() instanceof ItemEssentiaContainer containerItem && containerItem.getAspects(heldStack) != null && containerItem.getAspects(heldStack).size() > 0) {
                            int filled = reservoir.addToContainer(aspect, containerItem.getCapacity());
                            if (filled > 0) {
                                containerItem.setAspects(heldStack, new AspectList().add(aspect, Math.min(containerItem.getCapacity(), filled + containerItem.getAmount(heldStack))));
                                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.25f, 1.0f);
                            }
                        } else {
                            int drained = Math.min(amount, 10);
                            if (reservoir.takeFromContainer(aspect, drained)) {
                                ItemStack phialStack = ItemPhial.makeFilledPhial(aspect);
                                phialStack.setCount(drained);
                                if (!player.addItem(phialStack)) {
                                    level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, phialStack));
                                }
                                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.25f, 1.0f);
                            }
                        }
                        reservoir.markDirtyAndSync();
                    }
                } else {
                    if (player.getItemInHand(hand).getItem() instanceof ItemEssentiaContainer) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("Reservoir is empty"), true);
                    }
                }
            } else if (heldStack.getItem() == Items.BOOK || heldStack.getItem() == Items.WRITABLE_BOOK || heldStack.getItem() == Items.WRITTEN_BOOK) {
                Aspect aspect = reservoir.getAspect();
                if (aspect != null) {
                    reservoir.setAspectFilter(aspect);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Label set to: " + aspect.getName()), true);
                } else {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("No aspect to label"), true);
                }
            }
        }
        return InteractionResult.CONSUME;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileEssentiaReservoir reservoir && reservoir.getAmount() > 0) {
                Aspect aspect = reservoir.getAspect();
                if (aspect != null) {
                    for (int i = 0; i < 10; i++) {
                        level.addParticle(ParticleTypes.SOUL,
                                pos.getX() + level.random.nextDouble(),
                                pos.getY() + level.random.nextDouble(),
                                pos.getZ() + level.random.nextDouble(),
                                (aspect == net.minecraft.core.Holder.direct(Aspect.FLUX)) ? 1.0 : 0.0,
                                level.random.nextDouble() * 0.1,
                                0.0);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEssentiaReservoir reservoir) {
            return (int) (reservoir.getFillPercent() * 15);
        }
        return 0;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEssentiaReservoir(pos, state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.ESSENTIA_RESERVOIR.get() ?
                (lvl, pos, st, te) -> TileEssentiaReservoir.serverTick(lvl, pos, st, (TileEssentiaReservoir) te) : null;
    }
}
