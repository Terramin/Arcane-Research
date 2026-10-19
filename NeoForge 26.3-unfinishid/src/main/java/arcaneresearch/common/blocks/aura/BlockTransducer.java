package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.tiles.aura.TransducerTile;

import javax.annotation.Nullable;

public class BlockTransducer extends BaseEntityBlock {
    public BlockTransducer(BlockBehaviour.Properties props) {
        super(props
                .strength(2.0F, 2000.0F)
                .sound(SoundType.STONE)
                // Девайс — не куб (рисует TESR): иначе грани соседних блоков,
                // касающиеся его, отсекаются и соседи выглядят прозрачными.
                .noOcclusion()
                .requiresCorrectToolForDrops());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Геометрию рисует TransducerRenderer (OBJ lock + поршни), куб не нужен.
        return RenderShape.INVISIBLE;
    }

    // NEOFORGE_PORT: 26.x signature is propagatesSkylightDown(BlockState).
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, net.minecraft.world.level.BlockGetter level,
                                    BlockPos pos) {
        return 1.0F;
    }

    /**
     * Порт TC4 BlockStoneDevice.onNeighborChange: перечитать статус при
     * смене сигнала — иначе поздно поданный сигнал никогда не переводил
     * status 0→1 (переход живёт только в checkStatus).
     */
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block neighborBlock,
                                net.minecraft.world.level.redstone.Orientation orientation, boolean moved) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, moved);
        try {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TransducerTile tile) {
                tile.checkStatus(level);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        try {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TransducerTile tile) {
                tile.checkStatus(level);
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new TransducerTile(AuraBlockEntities.TRANSDUCER_TILE.get(), pos, state);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        try {
            if (level.isClientSide()) {
                return createTickerHelper(type, AuraBlockEntities.TRANSDUCER_TILE.get(),
                        TransducerTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.TRANSDUCER_TILE.get(),
                    TransducerTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
