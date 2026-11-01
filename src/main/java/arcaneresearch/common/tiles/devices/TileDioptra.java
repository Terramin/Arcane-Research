package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.world.aura.AuraChunk;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;

import java.util.Arrays;

public class TileDioptra extends TileArcaneresearch {
    private static final int GRID_SIZE = 13;
    private static final int GRID_AREA = GRID_SIZE * GRID_SIZE;
    private static final int CENTER_INDEX = (GRID_SIZE / 2) + (GRID_SIZE / 2) * GRID_SIZE;
    private static final int HALF_GRID = GRID_SIZE / 2;
    private static final int MAX_VALUE = 64;

    public int counter = 0;
    public byte[] grid_amt = new byte[GRID_AREA];

    public TileDioptra(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
    }

    private void updateGrid() {
        if (level == null) return;

        Arrays.fill(grid_amt, (byte) 0);

        boolean showVis = isShowingVis();
        int chunkX = worldPosition.getX() >> 4;
        int chunkZ = worldPosition.getZ() >> 4;

        for (int xx = 0; xx < GRID_SIZE; xx++) {
            for (int zz = 0; zz < GRID_SIZE; zz++) {
                AuraChunk ac = AuraHandler.getAuraChunk(
                        level.dimension(),
                        chunkX + xx - HALF_GRID,
                        chunkZ + zz - HALF_GRID
                );

                if (ac == null) continue;

                float raw = showVis ? ac.getVis() : ac.getFlux();
                float value = raw / 500.0f * 64.0f;

                grid_amt[xx + zz * GRID_SIZE] = (byte) Math.min(MAX_VALUE, (int) value);
            }
        }
    }

    private boolean isShowingVis() {
        if (level == null) return true;

        BlockState state = getBlockState();

        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
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

            if (data.length == GRID_AREA) {
                grid_amt = data;
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 0.3, worldPosition.getY() - 0.3, worldPosition.getZ() - 0.3,
                worldPosition.getX() + 1.3, worldPosition.getY() + 2.3, worldPosition.getZ() + 1.3
        );
    }

    public int getGridValue(int x, int z) {
        if (x < 0 || x >= GRID_SIZE || z < 0 || z >= GRID_SIZE) return 0;
        return grid_amt[x + z * GRID_SIZE] & 0xFF;
    }

    public boolean isDisplayingVis() {
        return isShowingVis();
    }

    public int getVisAtCenter() {
        return grid_amt[CENTER_INDEX] & 0xFF;
    }
}