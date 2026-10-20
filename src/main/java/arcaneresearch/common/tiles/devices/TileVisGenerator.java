package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// NEOFORGE_PORT: 1:1 logic from Forge .../tiles/devices/TileVisGenerator.java.
// REQUIRED changes (21.9 transfer rework, verified in 26.3.0.23-beta jar):
//   - implements IEnergyStorage (removed) -> implements EnergyHandler
//     (getAmountAsLong/getCapacityAsLong/insert/extract). Old no-receive/no-pull
//     semantics preserved: insert() -> 0, extract() -> 0 (tile PUSHES via outputEnergy).
//   - LazyOptional + getCapability/invalidateCaps -> removed; exposure via
//     getEnergyHandler(side) (facing side only, as before), wired in
//     ModBlockEntities.registerCapabilities.
//   - Neighbor query: targetTile.getCapability(ENERGY, side):LazyOptional ->
//     level.getCapability(Energy.BLOCK, pos, side):@Nullable; canReceive()+receiveEnergy()
//     collapse into one transactional insert (commit only when accepted > 0).
//   - tag.getInt -> getIntOr (Optional).
// Recharge/output amounts, facing/enabled rules unchanged.
public class TileVisGenerator extends TileArcaneresearch implements EnergyHandler {

    private static final int CAPACITY = 1000;
    private static final int MAX_EXTRACT = 20;
    private static final float VIS_PER_RECHARGE = 1.0f;
    private static final int ENERGY_PER_VIS = 1000;
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
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);

        tag.putInt("energy", energy);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        energy = tag.getIntOr("energy", 0);
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

        var handler = level.getCapability(Capabilities.Energy.BLOCK, targetPos, facing.getOpposite());
        if (handler != null) {
            int toExtract = Math.min(energy, MAX_EXTRACT);
            try (var tx = Transaction.openRoot()) {
                int accepted = handler.insert(toExtract, tx);
                if (accepted > 0) {
                    tx.commit();
                    energy -= accepted;
                    setChanged();
                    if (energy == 0) {
                        syncTile(false);
                    }
                }
            }
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

    @Nullable
    public EnergyHandler getEnergyHandler(Direction side) {
        return side == getFacing() ? this : null;
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return CAPACITY;
    }

    @Override
    public int insert(int amount, TransactionContext tx) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext tx) {
        return 0;
    }
}
