package arcaneresearch.common.blocks.crafting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
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
    public BlockCrucible() {
        super(BlockBehaviour.Properties.of()
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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide) {
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
                                    0.4f, 2.0f + level.random.nextFloat() * 0.4f);
                        }
                    }
                }
            }
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
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
        FluidStack heldFluid = FluidUtil.getFluidContained(heldItem).orElse(FluidStack.EMPTY);
        if (!heldFluid.isEmpty() && heldFluid.getFluid() == Fluids.WATER) {
            if (crucible.getTank().getFluidAmount() < crucible.getTank().getCapacity()) {
                if (FluidUtil.interactWithFluidHandler(player, hand, crucible.getTank())) {
                    crucible.markDirtyAndSync();
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.33f,
                            1.0f + (level.random.nextFloat() - level.random.nextFloat()) * 0.3f);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (!heldItem.isEmpty() && !player.isShiftKeyDown()
                && !(heldItem.getItem() instanceof arcaneresearch.api.casters.ICaster)
                && hit.getDirection() == net.minecraft.core.Direction.UP
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileCrucible crucible) {
                crucible.spillAll();
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
        if (level.isClientSide) {
            return (lvl, p, st, te) -> TileCrucible.clientTick(lvl, p, st, (TileCrucible) te);
        }
        return (lvl, p, st, te) -> TileCrucible.serverTick(lvl, p, st, (TileCrucible) te);
    }
}
