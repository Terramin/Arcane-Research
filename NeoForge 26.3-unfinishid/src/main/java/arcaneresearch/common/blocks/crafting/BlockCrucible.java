package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack; // NEOFORGE_PORT: package move (fluids -> NeoForge transfer era)
import net.neoforged.neoforge.transfer.fluid.FluidUtil; // NEOFORGE_PORT: package move
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileCrucible;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockCrucible extends BlockTCDevice {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 3.0, 0.0, 2.0, 16.0, 16.0),
            Block.box(14.0, 3.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(2.0, 3.0, 0.0, 14.0, 16.0, 2.0),
            Block.box(2.0, 3.0, 14.0, 14.0, 16.0, 16.0),
            Block.box(2.0, 3.0, 2.0, 14.0, 4.0, 14.0),
            Block.box(0.0, 0.0, 0.0, 3.0, 3.0, 2.0),
            Block.box(0.0, 0.0, 2.0, 2.0, 3.0, 3.0),
            Block.box(13.0, 0.0, 0.0, 16.0, 3.0, 2.0),
            Block.box(14.0, 0.0, 2.0, 16.0, 3.0, 3.0),
            Block.box(0.0, 0.0, 14.0, 3.0, 3.0, 16.0),
            Block.box(0.0, 0.0, 13.0, 2.0, 3.0, 14.0),
            Block.box(13.0, 0.0, 14.0, 16.0, 3.0, 16.0),
            Block.box(14.0, 0.0, 13.0, 16.0, 3.0, 14.0)
    );

    private int collisionDelay = 0;

    public BlockCrucible(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.METAL)
                .strength(2.0f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion());
    }

    @Override
    protected boolean hasFacing() {
        return false;
    }

    @Override
    protected boolean hasEnabled() {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileCrucible crucible) {
                if (entity instanceof ItemEntity itemEntity
                        && !(entity instanceof arcaneresearch.common.entities.EntitySpecialItem)) {

                    if (crucible.isHeated()) {
                        crucible.attemptSmelt(itemEntity);
                    }
                } else if (entity instanceof LivingEntity living) {
                    collisionDelay++;
                    if (collisionDelay >= 10) {
                        collisionDelay = 0;

                        if (crucible.isHeated() && crucible.getTank().getFluidAmount() > 0) {
                            living.hurt(level.damageSources().inFire(), 1.0f);
                            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                                    0.4f, 2.0f + level.getRandom().nextFloat() * 0.4f);
                        }
                    }
                }
            }
        }
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof TileCrucible crucible)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && heldItem.isEmpty()) {
            if (crucible.aspects.visSize() > 0 || crucible.getTank().getFluidAmount() > 0) {
                crucible.spillAll();
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        FluidStack heldFluid = FluidUtil.getFirstStackContained(heldItem);
        if (!heldFluid.isEmpty() && heldFluid.getFluid() == Fluids.WATER) {
            if (crucible.getTank().getFluidAmount() < crucible.getTank().getCapacity()) {
                // NEOFORGE_PORT: transfer-era overload needs a caller-owned transaction
                // (same BlockSpa pattern); a null TransactionContext would NPE.
                // Forge interact -> markDirtyAndSync + sound logic kept 1:1.
                try (var tx = net.neoforged.neoforge.transfer.transaction.Transaction.openRoot()) {
                    if (FluidUtil.interactWithFluidHandler(player, hand, pos, crucible.getTank(), tx)) {
                        tx.commit();
                        crucible.markDirtyAndSync();
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f,
                                1.0f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.3f);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (!heldItem.isEmpty() && !player.isShiftKeyDown()
                && !(heldItem.getItem() instanceof arcaneresearch.api.casters.ICaster)
                && hit.getDirection() == Direction.UP
                && crucible.isHeated() && crucible.getTank().getFluidAmount() > 0) {
            ItemStack result = crucible.attemptSmelt(heldItem.copy(), player.getName().getString());
            if (result == null || result.getCount() < heldItem.getCount()) {

                if (!player.getAbilities().instabuild) {
                    if (result == null || result.isEmpty()) {
                        heldItem.shrink(1);
                    } else {
                        player.setItemInHand(hand, result);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side removal hook with the old state + isMoving; the new state is
        // already in the chunk: level.getBlockState(pos)).
        // NEOFORGE_PORT (26.3): LevelChunk.setBlockState removes the tile BEFORE this hook, so the
        // lookup below finds nothing; the spill runs in TileCrucible.preRemoveSideEffects.
        // Spill kept BEFORE super to match Forge onRemove order (spill, then
        // super.onRemove which updates neighbours/comparators).
        if (!state.is(level.getBlockState(pos).getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileCrucible crucible) {
                crucible.spillAll();
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    // NEOFORGE_PORT: 26.x getAnalogOutputSignal gained a Direction param (same
    // pattern as BlockJar); visSize -> signal logic kept 1:1 with Forge.
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileCrucible crucible) {

            float r = (float) crucible.aspects.visSize() / (float) TileCrucible.MAX_ASPECTS;
            return Math.min(15, net.minecraft.util.Mth.floor(r * 14.0f)
                    + (crucible.aspects.visSize() > 0 ? 1 : 0));
        }
        return 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if (random.nextInt(10) == 0) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileCrucible crucible) {
                if (crucible.isHeated() && crucible.getTank().getFluidAmount() > 0) {
                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                            0.1f + random.nextFloat() * 0.1f, 1.2f + random.nextFloat() * 0.2f, false);
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileCrucible(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CRUCIBLE.get()) {
            return null;
        }
        if (level.isClientSide()) {
            return (lvl, p, st, te) -> TileCrucible.clientTick(lvl, p, st, (TileCrucible) te);
        }
        return (lvl, p, st, te) -> TileCrucible.serverTick(lvl, p, st, (TileCrucible) te);
    }
}
