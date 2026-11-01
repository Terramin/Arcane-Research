package arcaneresearch.common.tiles.devices;

import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class TileStabilizer extends TileArcaneresearch {

    private static final int RANGE = 8;
    private static final int CAPACITY = 15;
    private static final float POLLUTION_AMOUNT = 0.25f;

    protected int energy = 0;
    private int ticks = 0;
    private int delay = 0;
    private int lastEnergy = 0;

    @Nullable
    private AABB stabilityBox;
    @Nullable
    private AABB renderBox;

    public TileStabilizer(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileStabilizer(BlockPos pos, BlockState state) {
        this(ModBlockEntities.STABILIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileStabilizer tile) {
        tile.ticks++;
        if (tile.energy < CAPACITY && tile.ticks % 20 == 0) {
            tile.energy++;
            AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
            tile.markDirtyAndSync();
            level.updateNeighborsAt(pos, state.getBlock());
        }
        if (tile.energy > 0 && tile.delay <= 0 && tile.ticks % 5 == 0) {
            int previousEnergy = tile.energy;
            tile.tryAddStability();
            if (previousEnergy != tile.energy) {
                tile.markDirtyAndSync();
            }
        }
        if (tile.delay > 0) {
            tile.delay--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileStabilizer tile) {
        if (tile.energy != tile.lastEnergy) {
            level.sendBlockUpdated(pos, state, state, 3);
            tile.lastEnergy = tile.energy;
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("energy", energy);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        int loaded = tag.getInt("energy");
        if (loaded < 0) loaded = 0;
        if (loaded > CAPACITY) loaded = CAPACITY;
        energy = loaded;
    }

    private void tryAddStability() {
        if (level == null) return;
        AABB area = stabilityBox;
        if (area == null) {
            area = new AABB(worldPosition).inflate(RANGE);
            stabilityBox = area;
        }
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, area);
        for (EntityFluxRift rift : rifts) {
            if (rift.isRemoved()) continue;
            if (rift.getStability() == EntityFluxRift.EnumStability.VERY_STABLE) continue;
            if (!mitigate(1)) break;
            rift.addStability(0.125f);
            delay += 5;
            if (energy <= 0) break;
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.UP;
    }

    public int getEnergy() {
        return energy;
    }

    public int getCapacity() {
        return CAPACITY;
    }

    public boolean mitigate(int amount) {
        if (amount <= 0) return false;
        if (energy >= amount) {
            energy -= amount;
            if (level != null) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
            return true;
        }
        return false;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getCustomRenderBoundingBox();
    }

    public AABB getCustomRenderBoundingBox() {
        AABB box = renderBox;
        if (box == null) {
            box = new AABB(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 1.5, worldPosition.getZ() + 1);
            renderBox = box;
        }
        return box;
    }
}