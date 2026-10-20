package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.BlockStabilizer;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class StabilizerTile extends TileArcaneresearch {
    public int count = 0;
    private int lock = 1;

    public StabilizerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        try {
            if (state.getBlock() instanceof BlockStabilizer stab) {
                this.lock = stab.isAdvanced() ? 2 : 1;
            }
        } catch (Exception ignored) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, StabilizerTile tile) {
        try {
            if (pos.getY() < level.getMaxY() - 1
                    && level.getBlockEntity(pos.above()) instanceof AuraNodeTile
                    && !level.hasNeighborSignal(pos)) {
                if (tile.count < 37) {
                    tile.count++;
                }
                if (tile.count > 30 && tile.count % 3 == 0) {
                    double x = pos.getX() + 0.5D + (level.getRandom().nextFloat() - 0.5D) * 0.6D;
                    double z = pos.getZ() + 0.5D + (level.getRandom().nextFloat() - 0.5D) * 0.6D;
                    FXDispatcher.INSTANCE.drawStabilizerParticles(
                            x, pos.getY() + 1.0D, z, 0.0D, 0.06D, 0.0D, 24);
                }
            } else if (tile.count > 0) {
                tile.count--;
            }
        } catch (Exception ignored) {
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StabilizerTile tile) {
    }

    public int getLock() {
        return this.lock;
    }

    /**
     * Паритет TC4 TileNodeStabilizer.getRenderBoundingBox: пузырь рендерится
     * на 1.5 выше — без расширенного бокса его вырезает frustum-culling.
     */
    // NEOFORGE_PORT: без @Override (BlockEntity.getRenderBoundingBox убран в 26.x).
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition.getX(), worldPosition.getY(),
                worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2,
                worldPosition.getZ() + 1);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("stabLock", this.lock);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putInt("stabLock", this.lock);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        try {
            this.lock = tag.getIntOr("stabLock", 0);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        try {
            this.lock = tag.getIntOr("stabLock", 0);
        } catch (Exception ignored) {
        }
    }
}
