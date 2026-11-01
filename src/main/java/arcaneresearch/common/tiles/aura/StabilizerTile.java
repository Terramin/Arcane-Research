package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.blocks.aura.BlockStabilizer;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class StabilizerTile extends TileArcaneresearch {
    public int count = 0;
    private int lock = 1;

    private AABB renderBox;

    public StabilizerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        if (state.getBlock() instanceof BlockStabilizer stab) {
            this.lock = stab.isAdvanced() ? 2 : 1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, StabilizerTile tile) {
        if (level == null) return;

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
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StabilizerTile tile) {
    }

    public int getLock() {
        return this.lock;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1
            );
        }

        return renderBox;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("stabLock", this.lock);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("stabLock", this.lock);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("stabLock")) {
            this.lock = tag.getInt("stabLock");
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        if (tag.contains("stabLock")) {
            this.lock = tag.getInt("stabLock");
        }
    }
}