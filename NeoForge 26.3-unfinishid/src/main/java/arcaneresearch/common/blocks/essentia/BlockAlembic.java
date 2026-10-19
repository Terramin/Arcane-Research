package arcaneresearch.common.blocks.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.api.blocks.ILabelable;
import arcaneresearch.common.items.consumables.ItemLabel;
import arcaneresearch.common.tiles.essentia.TileAlembic;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;

public class BlockAlembic extends Block implements EntityBlock, ILabelable {

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public BlockAlembic(BlockBehaviour.Properties props) {

        super(props
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 20.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TileAlembic alembic)) {
            return InteractionResult.PASS;
        }
        Direction side = hit.getDirection();

        if (player.isShiftKeyDown() && alembic.aspectFilter != null
                && side.ordinal() == alembic.facing) {
            alembic.aspectFilter = null;
            alembic.facing = Direction.DOWN.ordinal();
            alembic.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            if (level.isClientSide()) {
                level.playSound(player, pos, ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            } else {
                level.addFreshEntity(new ItemEntity(level,
                        pos.getX() + 0.5 + side.getStepX() / 3.0,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5 + side.getStepZ() / 3.0,
                        new ItemStack(ModItems.LABEL_BLANK.get())));
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }

        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()
                && (alembic.aspectFilter == null || side.ordinal() != alembic.facing)) {
            if (level.isClientSide()) {
                level.playSound(player, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 0.4f, 1.0f);
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5f,
                        1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f, false);
            } else {
                AuraHelper.polluteAura(level, pos, alembic.amount, true);
                alembic.amount = 0;
                alembic.aspect = null;
                alembic.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)). Inventory drops are automatic
        // now (BlockEntity.preRemoveSideEffects default drops Container contents).
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);


    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileAlembic alembic) {
            float r = alembic.amount / (float) TileAlembic.MAX_AMOUNT;
            return net.minecraft.util.Mth.floor(r * 14.0f) + (alembic.amount > 0 ? 1 : 0);
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileAlembic(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        return null;
    }

    @Override
    public boolean applyLabel(Player player, BlockPos pos, Direction side, ItemStack labelStack) {

        BlockEntity te = player.level().getBlockEntity(pos);
        if (!(te instanceof TileAlembic alembic) || side.ordinal() <= 1 || alembic.aspectFilter != null) {
            return false;
        }
        Aspect labelAspect = null;
        if (labelStack.getItem() instanceof ItemLabel label) {
            labelAspect = label.getAspect(labelStack);
        }
        if (labelAspect == null && labelStack.getItem() instanceof IEssentiaContainerItem container) {
            var aspects = container.getAspects(labelStack);
            if (aspects != null && aspects.size() > 0) {
                labelAspect = aspects.getAspects()[0];
            }
        }
        if (alembic.amount == 0 && labelAspect == null) {
            return false;
        }
        Aspect aspect = null;
        if (alembic.amount == 0 && labelAspect != null) {
            aspect = labelAspect;
        }
        if (alembic.amount > 0) {
            aspect = alembic.aspect;
        }
        if (aspect == null) {
            return false;
        }
        alembic.aspectFilter = aspect;
        alembic.facing = side.ordinal();
        alembic.setChanged();
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.PAGE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        return true;
    }
}
