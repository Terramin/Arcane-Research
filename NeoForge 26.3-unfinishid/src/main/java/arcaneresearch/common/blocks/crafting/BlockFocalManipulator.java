package arcaneresearch.common.blocks.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockFocalManipulator extends BlockTCDevice {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public BlockFocalManipulator(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.STONE)
                .strength(2.0f, 10.0f)
                .sound(SoundType.STONE)
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileFocalManipulator(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.FOCAL_MANIPULATOR.get()) {
            return (lvl, pos, st, be) -> {
                if (lvl.isClientSide()) {
                    TileFocalManipulator.clientTick(lvl, pos, st, (TileFocalManipulator) be);
                } else {
                    TileFocalManipulator.serverTick(lvl, pos, st, (TileFocalManipulator) be);
                }
            };
        }
        return null;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useWithoutItem (hand unused).

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileFocalManipulator manipulator && player instanceof ServerPlayer serverPlayer) {

            serverPlayer.openMenu( new net.minecraft.world.MenuProvider() {
                @Override
                public net.minecraft.network.chat.Component getDisplayName() {
                    return net.minecraft.network.chat.Component.translatable("block.arcaneresearch.focal_manipulator");
                }

                @Override
                public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
                                                                                      net.minecraft.world.entity.player.Inventory playerInventory,
                                                                                      net.minecraft.world.entity.player.Player player) {
                    return new arcaneresearch.common.menu.FocalManipulatorMenu(containerId, playerInventory, manipulator);
                }
            }, pos);
            return InteractionResult.CONSUME;
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
        if (!state.is(level.getBlockState(pos).getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileFocalManipulator tile) {
                tile.dropContents();
            }
        }

    }
}
