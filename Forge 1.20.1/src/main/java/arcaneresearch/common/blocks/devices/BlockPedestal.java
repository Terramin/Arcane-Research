package arcaneresearch.common.blocks.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.crafting.IInfusionStabiliserExt;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.BlockTC;
import arcaneresearch.common.tiles.crafting.TilePedestal;
import javax.annotation.Nullable;
public class BlockPedestal extends BlockTC implements EntityBlock, IInfusionStabiliserExt {
    private static final VoxelShape SHAPE_ARCANE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 16.0, 14.0)
    );
    private static final VoxelShape SHAPE_ELDRITCH = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(2.0, 4.0, 2.0, 14.0, 8.0, 14.0),
            Block.box(4.0, 8.0, 4.0, 12.0, 12.0, 12.0)
    );
    private final PedestalType pedestalType;
    public BlockPedestal(PedestalType type) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0f, 10.0f)
                .sound(SoundType.STONE)
                .noOcclusion());
        this.pedestalType = type;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockInlay.CHARGE, 0));
    }
    public static BlockPedestal createArcane() {
        return new BlockPedestal(PedestalType.ARCANE);
    }
    public static BlockPedestal createAncient() {
        return new BlockPedestal(PedestalType.ANCIENT);
    }
    public static BlockPedestal createEldritch() {
        return new BlockPedestal(PedestalType.ELDRITCH);
    }
    public PedestalType getPedestalType() {
        return pedestalType;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockInlay.CHARGE);
    }
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            BlockInlay.updateSurroundingInlay(level, pos, state);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockInlay.notifyInlayNeighborsOfStateChange(level, pos.relative(dir));
            }
        }
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            BlockInlay.updateSurroundingInlay(level, pos, state);
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BlockInlay.CHARGE) > 0) {
            FXDispatcher.INSTANCE.blockRunes2(pos.getX(), pos.getY() - 0.375, pos.getZ(),
                    1.0f, 0.0f, 0.0f, 10, 0.0f);
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return pedestalType == PedestalType.ARCANE ? SHAPE_ARCANE : SHAPE_ELDRITCH;
    }
    public double getItemRenderHeight() {
        return pedestalType == PedestalType.ARCANE ? 1.28 : 1.03;
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TilePedestal pedestal) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (pedestal.getDisplayedItem().isEmpty() && !heldItem.isEmpty()) {
                if (pedestal.tryInsertItem(player, heldItem)) {
                    return InteractionResult.CONSUME;
                }
            } else if (!pedestal.getDisplayedItem().isEmpty()) {
                ItemStack taken = pedestal.tryTakeItem(player);
                if (!taken.isEmpty()) {
                    if (!player.getInventory().add(taken)) {
                        player.drop(taken, false);
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TilePedestal pedestal) {
                pedestal.dropContents();
            }
            if (!level.isClientSide) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    level.updateNeighborsAt(pos.relative(dir), this);
                }
                BlockInlay.updateSurroundingInlay(level, pos, state);
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockInlay.notifyInlayNeighborsOfStateChange(level, pos.relative(dir));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePedestal(pos, state);
    }
    @Override
    public boolean canStabaliseInfusion(Level level, BlockPos pos) {
        return true;
    }
    @Override
    public float getStabilizationAmount(Level level, BlockPos pos) {
        return pedestalType == PedestalType.ELDRITCH ? 0.1f : 0.0f;
    }
    @Override
    public boolean hasSymmetryPenalty(Level level, BlockPos pos1, BlockPos pos2) {
        BlockEntity te1 = level.getBlockEntity(pos1);
        BlockEntity te2 = level.getBlockEntity(pos2);
        if (te1 instanceof TilePedestal p1 && te2 instanceof TilePedestal p2) {
            return p1.getDisplayedItem().isEmpty() != p2.getDisplayedItem().isEmpty();
        }
        return false;
    }
    @Override
    public float getSymmetryPenalty(Level level, BlockPos pos) {
        return 0.1f;
    }
    public enum PedestalType {
        ARCANE(0.0f),
        ANCIENT(0.0f),
        ELDRITCH(0.1f);
        private final float stabilityBonus;
        PedestalType(float stabilityBonus) {
            this.stabilityBonus = stabilityBonus;
        }
        public float getStabilityBonus() {
            return stabilityBonus;
        }
    }
}
