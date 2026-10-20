package arcaneresearch.common.blocks.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.tiles.aura.StabilizerTile;

import javax.annotation.Nullable;

public class BlockStabilizer extends BaseEntityBlock {
    private final boolean advanced;

    public BlockStabilizer(BlockBehaviour.Properties props, boolean advanced) {
        super(props
                .strength(2.0F, 2000.0F)
                .sound(SoundType.STONE)
                // Девайс — не куб (рисует TESR): иначе грани соседних блоков,
                // касающиеся его, отсекаются и соседи выглядят прозрачными.
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.advanced = advanced;
    }

    public boolean isAdvanced() {
        return this.advanced;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Геометрию рисует StabilizerRenderer (OBJ lock + поршни), куб не нужен.
        return RenderShape.INVISIBLE;
    }

    // NEOFORGE_PORT: 26.x signature is propagatesSkylightDown(BlockState).
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            return new StabilizerTile(AuraBlockEntities.STABILIZER_TILE.get(), pos, state);
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
                return createTickerHelper(type, AuraBlockEntities.STABILIZER_TILE.get(),
                        StabilizerTile::clientTick);
            }
            return createTickerHelper(type, AuraBlockEntities.STABILIZER_TILE.get(),
                    StabilizerTile::serverTick);
        } catch (Exception e) {
            return null;
        }
    }
}
