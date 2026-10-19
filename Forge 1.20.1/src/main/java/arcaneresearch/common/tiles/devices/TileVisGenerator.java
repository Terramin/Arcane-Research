package arcaneresearch.common.tiles.devices;
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
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public class TileVisGenerator extends TileArcaneresearch implements IEnergyStorage {
    private static final int CAPACITY = 1000;
    private static final int MAX_EXTRACT = 20;
    private static final float VIS_PER_RECHARGE = 1.0f;
    private static final int ENERGY_PER_VIS = 1000;
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> this);
    protected int energy = 0;
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
        energy = tag.getInt("energy");
    }
    private void recharge() {
        if (energy > 0) return;
        if (level == null) return;
        float vis = AuraHandler.drainVis(level, worldPosition, VIS_PER_RECHARGE, false);
        energy = (int) (vis * ENERGY_PER_VIS);
        setChanged();
        syncTile(false);
    }
    private void outputEnergy() {
        if (energy <= 0) return;
        if (level == null) return;
        Direction facing = getFacing();
        BlockPos targetPos = worldPosition.relative(facing);
        BlockEntity targetTile = level.getBlockEntity(targetPos);
        if (targetTile != null) {
            targetTile.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite()).ifPresent(handler -> {
                if (handler.canReceive()) {
                    int toExtract = Math.min(energy, MAX_EXTRACT);
                    int accepted = handler.receiveEnergy(toExtract, false);
                    if (accepted > 0) {
                        energy -= accepted;
                        setChanged();
                        if (energy == 0) {
                            syncTile(false);
                        }
                    }
                }
            });
        }
    }
    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.NORTH;
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
        return true;
    }
    @Override
    public boolean canReceive() {
        return false;
    }
}
