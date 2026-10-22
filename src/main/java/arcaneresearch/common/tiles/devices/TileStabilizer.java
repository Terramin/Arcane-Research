package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileStabilizer extends TileArcaneresearch {

    private static final int RANGE = 8;
    private static final int CAPACITY = 15;
    private static final float POLLUTION_AMOUNT = 0.25f;
    protected int energy = 0;
    private int ticks = 0;
    private int delay = 0;
    private int lastEnergy = 0;

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
            tile.setChanged();
            tile.syncTile(false);
            level.updateNeighborsAt(pos, state.getBlock());
        }

        if (tile.energy > 0 && tile.delay <= 0 && tile.ticks % 5 == 0) {
            int previousEnergy = tile.energy;
            tile.tryAddStability();
            if (previousEnergy != tile.energy) {
                tile.setChanged();
                tile.syncTile(false);
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
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);

        tag.putInt("energy", energy);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        energy = Math.min(tag.getIntOr("energy", 0), CAPACITY);
    }

    private void tryAddStability() {
        if (level == null) return;

        Direction facing = getFacing();
        AABB area = new AABB(worldPosition).inflate(RANGE);

        java.util.List<EntityFluxRift> rifts = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityFluxRift.class), area, e -> true);
        for (EntityFluxRift rift : rifts) {
            if (rift.isRemoved()) continue;
            if (rift.getStability() == EntityFluxRift.EnumStability.VERY_STABLE) continue;

            if (mitigate(1)) {

                rift.addStability(0.125f);
                delay += 5;
                if (energy <= 0) return;
            }
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
        if (energy >= amount) {
            energy -= amount;
            if (level != null) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
            return true;
        }
        return false;
    }

    
    
    public AABB getRenderBoundingBox() {
        return getCustomRenderBoundingBox();
    }

    public AABB getCustomRenderBoundingBox() {
        return new AABB(
                worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 1.5, worldPosition.getZ() + 1
        );
    }
}
