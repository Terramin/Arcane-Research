package arcaneresearch.common.tiles.devices;

import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public class TileRedstoneRelay extends TileArcaneresearch {

    private int inputThreshold = 1;
    private int outputStrength = 15;

    @Nullable
    private AABB inputDialCache;
    @Nullable
    private Direction inputDialCacheFacing;

    @Nullable
    private AABB outputDialCache;
    @Nullable
    private Direction outputDialCacheFacing;

    public TileRedstoneRelay(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileRedstoneRelay(BlockPos pos, BlockState state) {
        this(ModBlockEntities.REDSTONE_RELAY.get(), pos, state);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByte("in", (byte) inputThreshold);
        tag.putByte("out", (byte) outputStrength);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        if (tag.contains("In") || tag.contains("Out")) {
            setInputThreshold(tag.getByte("In"));
            setOutputStrength(tag.getByte("Out"));
        } else {
            if (tag.getByte("in") != 0) setInputThreshold(tag.getByte("in"));
            if (tag.getByte("out") != 0) setOutputStrength(tag.getByte("out"));
        }
    }

    public void increaseInput() {
        if (level == null || level.isClientSide()) return;
        inputThreshold++;
        if (inputThreshold > 15) {
            inputThreshold = 1;
        }
        markDirtyAndSync();
    }

    public void increaseOutput() {
        if (level == null || level.isClientSide()) return;
        outputStrength++;
        if (outputStrength > 15) {
            outputStrength = 1;
        }
        markDirtyAndSync();
    }

    public int getInputThreshold() {
        return inputThreshold;
    }

    public void setInputThreshold(int value) {
        int clamped = Math.max(1, Math.min(15, value));
        if (inputThreshold != clamped) {
            inputThreshold = clamped;
            setChanged();
        }
    }

    public int getOutputStrength() {
        return outputStrength;
    }

    public void setOutputStrength(int value) {
        int clamped = Math.max(1, Math.min(15, value));
        if (outputStrength != clamped) {
            outputStrength = clamped;
            setChanged();
        }
    }

    public int calculateOutput(int inputSignal) {
        return inputSignal >= inputThreshold ? outputStrength : 0;
    }

    public Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.NORTH;
    }

    public Direction getInputDirection() {
        return getFacing().getOpposite();
    }

    public Direction getOutputDirection() {
        return getFacing();
    }

    public AABB getInputDialBounds() {
        Direction facing = getFacing();
        AABB cached = inputDialCache;
        if (cached != null && inputDialCacheFacing == facing) {
            return cached;
        }
        double minX, minZ, maxX, maxZ;
        switch (facing) {
            case WEST -> {
                minX = worldPosition.getX() + 0.125;
                maxX = worldPosition.getX() + 0.375;
                minZ = worldPosition.getZ() + 0.625;
                maxZ = worldPosition.getZ() + 0.875;
            }
            case NORTH -> {
                minX = worldPosition.getX() + 0.625;
                maxX = worldPosition.getX() + 0.875;
                minZ = worldPosition.getZ() + 0.625;
                maxZ = worldPosition.getZ() + 0.875;
            }
            case EAST -> {
                minX = worldPosition.getX() + 0.625;
                maxX = worldPosition.getX() + 0.875;
                minZ = worldPosition.getZ() + 0.125;
                maxZ = worldPosition.getZ() + 0.375;
            }
            default -> {
                minX = worldPosition.getX() + 0.125;
                maxX = worldPosition.getX() + 0.375;
                minZ = worldPosition.getZ() + 0.125;
                maxZ = worldPosition.getZ() + 0.375;
            }
        }
        double minY = worldPosition.getY() + 0.0625;
        double maxY = worldPosition.getY() + 0.25;
        AABB result = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        inputDialCache = result;
        inputDialCacheFacing = facing;
        return result;
    }

    public AABB getOutputDialBounds() {
        Direction facing = getFacing();
        AABB cached = outputDialCache;
        if (cached != null && outputDialCacheFacing == facing) {
            return cached;
        }
        double minX, minZ, maxX, maxZ;
        switch (facing) {
            case WEST -> {
                minX = worldPosition.getX() + 0.125;
                maxX = worldPosition.getX() + 0.375;
                minZ = worldPosition.getZ() + 0.375;
                maxZ = worldPosition.getZ() + 0.625;
            }
            case NORTH -> {
                minX = worldPosition.getX() + 0.375;
                maxX = worldPosition.getX() + 0.625;
                minZ = worldPosition.getZ() + 0.125;
                maxZ = worldPosition.getZ() + 0.375;
            }
            case EAST -> {
                minX = worldPosition.getX() + 0.625;
                maxX = worldPosition.getX() + 0.875;
                minZ = worldPosition.getZ() + 0.375;
                maxZ = worldPosition.getZ() + 0.625;
            }
            default -> {
                minX = worldPosition.getX() + 0.375;
                maxX = worldPosition.getX() + 0.625;
                minZ = worldPosition.getZ() + 0.625;
                maxZ = worldPosition.getZ() + 0.875;
            }
        }
        double minY = worldPosition.getY() + 0.0625;
        double maxY = worldPosition.getY() + 0.25;
        AABB result = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        outputDialCache = result;
        outputDialCacheFacing = facing;
        return result;
    }
}