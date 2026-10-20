package arcaneresearch.common.tiles.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.world.aura.AuraChunk;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;
import java.util.Arrays;
public class TileDioptra extends TileArcaneresearch {
    public int counter = 0;
    public byte[] grid_amt = new byte[169];
    public TileDioptra(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(grid_amt, (byte) 0);
    }
    public TileDioptra(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DIOPTRA.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileDioptra tile) {
        tile.counter++;
        if (tile.counter % 20 == 0) {
            tile.updateGrid();
            tile.setChanged();
            tile.syncTile(false);
        }
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileDioptra tile) {
        tile.counter = 0;
    }
    private void updateGrid() {
        Arrays.fill(grid_amt, (byte) 0);
        boolean showVis = isShowingVis();
        int chunkX = worldPosition.getX() >> 4;
        int chunkZ = worldPosition.getZ() >> 4;
        for (int xx = 0; xx < 13; xx++) {
            for (int zz = 0; zz < 13; zz++) {
                AuraChunk ac = AuraHandler.getAuraChunk(level.dimension(), chunkX + xx - 6, chunkZ + zz - 6);
                if (ac != null) {
                    float value;
                    if (showVis) {
                        value = ac.getVis() / 500.0f * 64.0f;
                    } else {
                        value = ac.getFlux() / 500.0f * 64.0f;
                    }
                    grid_amt[xx + zz * 13] = (byte) Math.min(64, (int) value);
                }
            }
        }
    }
    private boolean isShowingVis() {
        BlockState state = getBlockState();
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)) {
            return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED);
        }
        return true;
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByteArray("grid_a", grid_amt);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        if (tag.contains("grid_a")) {
            byte[] data = tag.getByteArray("grid_a");
            if (data.length == 169) {
                grid_amt = data;
            }
        }
    }
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 0.3, worldPosition.getY() - 0.3, worldPosition.getZ() - 0.3,
                worldPosition.getX() + 1.3, worldPosition.getY() + 2.3, worldPosition.getZ() + 1.3);
    }
    public int getGridValue(int x, int z) {
        if (x < 0 || x >= 13 || z < 0 || z >= 13) return 0;
        return grid_amt[x + z * 13] & 0xFF;
    }
    public boolean isDisplayingVis() {
        return isShowingVis();
    }
    public int getVisAtCenter() {
        return grid_amt[84] & 0xFF;
    }
}
