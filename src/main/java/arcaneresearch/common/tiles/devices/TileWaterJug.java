package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.lib.fluid.ArcaneresearchFluidTank;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;













public class TileWaterJug extends TileArcaneresearch {

    public static final int TANK_CAPACITY = 1000;
    private static final int SCAN_RANGE = 2;

    public final ArcaneresearchFluidTank tank = new ArcaneresearchFluidTank(TANK_CAPACITY,
            stack -> stack.getFluid() == Fluids.WATER);

    
    
    
    private final net.neoforged.neoforge.transfer.transaction.SnapshotJournal<Boolean> drainJournal =
            new net.neoforged.neoforge.transfer.transaction.SnapshotJournal<>() {
                @Override
                protected Boolean createSnapshot() {
                    return tank.getFluidAmount() >= TANK_CAPACITY;
                }

                @Override
                protected void revertToSnapshot(Boolean snapshot) {
                }

                @Override
                protected void onRootCommit(Boolean wasFull) {
                    setChanged();
                    if (wasFull && tank.getFluidAmount() < TANK_CAPACITY) {
                        syncTile(false);
                    }
                }
            };

    private final ResourceHandler<FluidResource> drainOnlyView =
            new DelegatingResourceHandler<>(tank) {
                @Override
                public int insert(int index, FluidResource resource, int amount,
                                  TransactionContext tx) {
                    return 0;
                }

                
                
                @Override
                public int insert(FluidResource resource, int amount, TransactionContext tx) {
                    return 0;
                }

                @Override
                public int extract(int index, FluidResource resource, int amount,
                                   TransactionContext tx) {
                    drainJournal.updateSnapshots(tx);
                    return super.extract(index, resource, amount, tx);
                }

                @Override
                public int extract(FluidResource resource, int amount, TransactionContext tx) {
                    drainJournal.updateSnapshots(tx);
                    return super.extract(resource, amount, tx);
                }
            };

    private int zone = 0;
    private int counter = 0;
    private List<Integer> handlers = new ArrayList<>();

    private int visualZone = 0;
    private int visualCounter = 0;

    public TileWaterJug(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_JUG.get(), pos, state);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        if (side == Direction.UP) {
            return drainOnlyView;
        }
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileWaterJug tile) {
        tile.counter++;

        if (tile.counter % 5 != 0) {
            return;
        }

        tile.zone++;
        int scanX = (tile.zone / 5) % 5;
        int scanY = (tile.zone / 5 / 5) % 3;
        int scanZ = tile.zone % 5;

        BlockPos scanPos = pos.offset(scanX - SCAN_RANGE, scanY - 1, scanZ - SCAN_RANGE);

        if (tile.isValidFluidTarget(level, scanPos)) {
            if (!tile.handlers.contains(tile.zone)) {
                tile.handlers.add(tile.zone);
                tile.setChanged();
            }
        }

        int handlerIndex = 0;
        while (handlerIndex < tile.handlers.size() && tile.tank.getFluidAmount() >= 25) {
            int handlerZone = tile.handlers.get(handlerIndex);
            int hx = (handlerZone / 5) % 5;
            int hy = (handlerZone / 5 / 5) % 3;
            int hz = handlerZone % 5;

            BlockPos handlerPos = pos.offset(hx - SCAN_RANGE, hy - 1, hz - SCAN_RANGE);

            if (!tile.tryFillTarget(level, handlerPos, handlerZone)) {

                tile.handlers.remove(handlerIndex);
                tile.setChanged();
                continue;
            }
            handlerIndex++;
        }

        if (tile.tank.getFluidAmount() < TANK_CAPACITY) {
            float visNeeded = (TANK_CAPACITY - tile.tank.getFluidAmount()) / 1000.0f;
            if (visNeeded > 0.1f) {
                visNeeded = 0.1f;
            }

            float visDrained = AuraHelper.drainVis(level, pos, visNeeded, false);
            int waterGenerated = (int) (1000.0f * visDrained);

            if (waterGenerated > 0) {
                tile.tank.fill(new FluidStack(Fluids.WATER, waterGenerated), true);
                tile.setChanged();

                if (tile.tank.getFluidAmount() >= TANK_CAPACITY) {
                    tile.syncTile(false);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileWaterJug tile) {
        tile.counter++;

        if (tile.visualCounter > 0) {
            if (tile.visualCounter % 5 == 0) {
                int vx = (tile.visualZone / 5) % 5;
                int vy = (tile.visualZone / 5 / 5) % 3;
                int vz = tile.visualZone % 5;

                FXDispatcher.INSTANCE.waterTrailFx(pos,
                        pos.offset(vx - 2, vy - 1, vz - 2), tile.counter, 0x2870DA, 0.1f);
            }
            tile.visualCounter--;
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tank.writeToNBT(tag);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        tank.readFromNBT(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        handlers.clear();
        
        int[] arr = tag.getIntArray("handlers").orElse(new int[0]);
        for (int h : arr) {
            handlers.add(h);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putIntArray("handlers", handlers.stream().mapToInt(i -> i).toArray());
    }

    private boolean isValidFluidTarget(Level level, BlockPos targetPos) {
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.is(Blocks.CAULDRON) || targetState.is(Blocks.WATER_CAULDRON)) {
            return true;
        }

        BlockEntity te = level.getBlockEntity(targetPos);
        if (te != null) {
            var handler = level.getCapability(Capabilities.Fluid.BLOCK, targetPos, Direction.UP);
            return handler != null;
        }

        return false;
    }

    private boolean tryFillTarget(Level level, BlockPos targetPos, int zoneId) {
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.is(Blocks.CAULDRON)) {
            if (tank.getFluidAmount() >= 333) {
                level.setBlock(targetPos, Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 1), 2);
                tank.drain(333, true);
                level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zoneId);
                setChanged();
                syncTile(false);
            }
            return true;
        }

        if (targetState.is(Blocks.WATER_CAULDRON)) {
            int currentLevel = targetState.getValue(LayeredCauldronBlock.LEVEL);
            if (currentLevel < 3 && tank.getFluidAmount() >= 333) {
                level.setBlock(targetPos, targetState.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1), 2);
                tank.drain(333, true);
                level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zoneId);
                setChanged();
                syncTile(false);
            }
            return true;
        }

        BlockEntity te = level.getBlockEntity(targetPos);
        if (te != null) {
            var handler = level.getCapability(Capabilities.Fluid.BLOCK, targetPos, Direction.UP);
            if (handler != null) {
                
                
                try (var tx = Transaction.openRoot()) {
                    int filled = handler.insert(FluidResource.of(Fluids.WATER), 25, tx);
                    if (filled > 0) {
                        tx.commit();
                        tank.drain(filled, true);
                        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zoneId);
                        setChanged();
                        syncTile(false);
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            if (level != null && level.isClientSide()) {
                visualZone = param;
                visualCounter = 5;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public int getWaterLevel() {
        return tank.getFluidAmount();
    }

    public boolean isFull() {
        return tank.getFluidAmount() >= TANK_CAPACITY;
    }
}
