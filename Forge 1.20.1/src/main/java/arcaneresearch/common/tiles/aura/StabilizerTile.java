package arcaneresearch.common.tiles.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.blocks.aura.BlockStabilizer;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
            if (pos.getY() < level.getMaxBuildHeight() - 1
                    && level.getBlockEntity(pos.above()) instanceof AuraNodeTile
                    && !level.hasNeighborSignal(pos)) {
                if (tile.count < 37) {
                    tile.count++;
                }
                if (tile.count > 30 && tile.count % 3 == 0) {
                    double x = pos.getX() + 0.5D + (level.random.nextFloat() - 0.5D) * 0.6D;
                    double z = pos.getZ() + 0.5D + (level.random.nextFloat() - 0.5D) * 0.6D;
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
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        return new net.minecraft.world.phys.AABB(worldPosition.getX(), worldPosition.getY(),
                worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2,
                worldPosition.getZ() + 1);
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("stabLock", this.lock);
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("stabLock", this.lock);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        try {
            this.lock = tag.getInt("stabLock");
        } catch (Exception ignored) {
        }
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        try {
            this.lock = tag.getInt("stabLock");
        } catch (Exception ignored) {
        }
    }
}
