package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
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
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.common.tiles.crafting.TileThaumatoriumTop;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;

public class BlockThaumatoriumTop extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 10.0, 12.0);

    public BlockThaumatoriumTop(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.METAL)
                .strength(3.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .noLootTable()
                .requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.INVISIBLE;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useWithoutItem (hand unused).

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {

        if (player.isCrouching()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof TileThaumatorium thaumatorium && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(thaumatorium, buf -> buf.writeBlockPos(pos.below()));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader level,
                                net.minecraft.world.level.ScheduledTickAccess tickAccess, BlockPos currentPos,
                                Direction facing, BlockPos facingPos, BlockState facingState,
                                net.minecraft.util.RandomSource random) {

        if (facing == Direction.DOWN) {
            if (!facingState.is(ModBlocks.THAUMATORIUM.get())) {
                return ModBlocks.ALCHEMICAL_BRASS_BLOCK.get().defaultBlockState();
            }
        }
        return super.updateShape(state, level, tickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval
        // (server-side, old state + isMoving; new state already swapped: level.getBlockState(pos)).
        BlockState newState = level.getBlockState(pos);
        if (!isMoving && !state.is(newState.getBlock())
                && !newState.is(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get())) {

            BlockState below = level.getBlockState(pos.below());
            if (below.is(ModBlocks.THAUMATORIUM.get())) {
                // NEOFORGE_PORT: parity with Forge onRemove — top break downgrades the
                // multiblock base to brass + drops a brass item. The tile inventory is
                // NOT dropped manually: 26.x BlockEntity.preRemoveSideEffects drops
                // Container contents automatically on setBlock (manual loop would double-drop).
                level.setBlock(pos.below(), ModBlocks.ALCHEMICAL_BRASS_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        new net.minecraft.world.item.ItemStack(ModBlocks.ALCHEMICAL_BRASS_BLOCK.get()));
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileThaumatoriumTop(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.THAUMATORIUM_TOP.get()) {
            if (!level.isClientSide()) {
                return (lvl, pos, st, be) -> TileThaumatoriumTop.serverTick(lvl, pos, st, (TileThaumatoriumTop) be);
            }
        }
        return null;
    }
}
