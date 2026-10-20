package arcaneresearch.common.blocks.crafting;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.DeconstructTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Порт {@code thaumcraft.common.blocks.BlockTable} (TC 4.2.3.5, мета 14 —
 * Deconstruction Table): деревянный стол, ПКМ открывает GUI разборки
 * предметов на аспекты. Сама разборка — {@link DeconstructTile}
 * (в соседнем пакете {@code common.tiles.crafting}).
 * Регистрация — {@code ModBlocks.DECONSTRUCTION_TABLE} +
 * {@code ModBlockEntities.DECONSTRUCTION_TABLE}.
 */
public class DeconstructBlock extends Block implements EntityBlock {

    /** Хитбокс 1-в-1 по модели {@code deconstruction_table.json}. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(11.0, 4.0, 11.0, 15.0, 8.0, 15.0),
            Block.box(1.0, 4.0, 1.0, 5.0, 8.0, 5.0),
            Block.box(11.0, 4.0, 1.0, 15.0, 8.0, 5.0),
            Block.box(1.0, 4.0, 11.0, 5.0, 8.0, 15.0));

    public DeconstructBlock(BlockBehaviour.Properties props) {
        super(props.mapColor(MapColor.WOOD)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DeconstructTile tile && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(tile, buf -> buf.writeBlockPos(pos));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        // NEOFORGE_PORT: Block.onRemove removed in 26.x -> affectNeighborsAfterRemoval.
        // Slot contents are dropped automatically: 26.x BlockEntity.preRemoveSideEffects
        // drops Container contents (LevelChunk.setBlockState calls it with the tile still
        // present, before this hook and before the block entity is removed), so the
        // 1.20.1 tile.dropContents() call is not needed.
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeconstructTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof DeconstructTile tile) {
                tile.serverTick();
            }
        };
    }
}
