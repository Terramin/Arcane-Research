package arcaneresearch.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.lib.utils.InventoryUtils;

import javax.annotation.Nullable;

public class BlockTCTile extends BlockTC implements EntityBlock {
    protected static boolean keepInventory = false;
    // NEOFORGE_PORT: widened to public (was protected): read by TileArcaneresearch
    // preRemoveSideEffects (different package) for the essentia-spill guard.
    public static boolean spillEssentia = true;
    protected Class<? extends BlockEntity> tileClass;

    public BlockTCTile(Properties properties, Class<? extends BlockEntity> tileClass) {
        super(properties);
        this.tileClass = tileClass;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (tileClass == null) {
            return null;
        }
        try {
            return tileClass.getDeclaredConstructor(BlockPos.class, BlockState.class).newInstance(pos, state);
        } catch (ReflectiveOperationException e) {
            Arcaneresearch.LOGGER.error("Failed to create tile entity for {}", tileClass, e);
        }
        return null;
    }

    // NEOFORGE_PORT: Block.onRemove removed in 26.x; no override needed here anymore:
    // inventory drops are automatic (BlockEntity.preRemoveSideEffects default drops
    // Container contents) and the essentia spill moved to
    // TileArcaneresearch.preRemoveSideEffects (tile-side, runs with the tile present).

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity tileentity = level.getBlockEntity(pos);
        return tileentity != null && tileentity.triggerEvent(id, param);
    }
}
