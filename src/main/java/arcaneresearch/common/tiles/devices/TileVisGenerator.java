package arcaneresearch.common.tiles.devices;

import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileVisGenerator extends TileArcaneresearch implements IEnergyStorage {

    private static final int CAPACITY = 1000;
    private static final int MAX_EXTRACT = 20;
    private static final float VIS_PER_RECHARGE = 1.0f;
    private static final int ENERGY_PER_VIS = 1000;

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);

    protected int energy = 0;

    @Nullable
    private BlockState cachedFacingState;
    @Nullable
    private Direction cachedFacing;

    public TileVisGenerator(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileVisGenerator(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileVisGenerator tile) {
        if (!tile.isEnabled(state)) return;
        tile.recharge();
        tile.outputEnergy();
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

    private void recharge() {
        if (energy > 0) return;
        if (level == null) return;
        float vis = AuraHandler.drainVis(level, worldPosition, VIS_PER_RECHARGE, false);
        if (vis <= 0) return;
        energy = (int) (vis * ENERGY_PER_VIS);
        markDirtyAndSync();
    }

    private void outputEnergy() {
        if (energy <= 0) return;
        if (level == null) return;
        Direction facing = getFacing();
        BlockPos targetPos = worldPosition.relative(facing);
        BlockEntity targetTile = level.getBlockEntity(targetPos);
        if (targetTile == null) return;
        LazyOptional<IEnergyStorage> cap =
                targetTile.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite());
        if (!cap.isPresent()) return;
        IEnergyStorage handler = cap.orElse(null);
        if (!handler.canReceive()) return;
        int toExtract = Math.min(energy, MAX_EXTRACT);
        int accepted = handler.receiveEnergy(toExtract, false);
        if (accepted > 0) {
            energy -= accepted;
            if (energy == 0) markDirtyAndSync();
            else setChanged();
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        Direction cached = cachedFacing;
        if (state != cachedFacingState || cached == null) {
            cached = state.hasProperty(BlockStateProperties.FACING)
                    ? state.getValue(BlockStateProperties.FACING)
                    : Direction.NORTH;
            cachedFacingState = state;
            cachedFacing = cached;
        }
        return cached;
    }

    private boolean isEnabled(BlockState state) {
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }
        return true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && side == getFacing()) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}