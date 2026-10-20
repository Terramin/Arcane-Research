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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
public class TileWaterJug extends TileArcaneresearch {
    public static final int TANK_CAPACITY = 1000;
    private static final int SCAN_RANGE = 2;
    public final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }
    };
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new WaterJugFluidHandler());
    private int zone = 0;
    private int counter = 0;
    private List<Integer> handlers = new ArrayList<>();
    private int visualZone = 0;
    private int visualCounter = 0;
    public TileWaterJug(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WATER_JUG.get(), pos, state);
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
                tile.tank.fill(new FluidStack(Fluids.WATER, waterGenerated), IFluidHandler.FluidAction.EXECUTE);
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
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tank.writeToNBT(tag);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        tank.readFromNBT(tag);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        handlers.clear();
        if (tag.contains("handlers")) {
            int[] arr = tag.getIntArray("handlers");
            for (int h : arr) {
                handlers.add(h);
            }
        }
    }
    @Override
    protected void saveAdditional(CompoundTag tag) {
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
            LazyOptional<IFluidHandler> cap = te.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP);
            return cap.isPresent();
        }
        return false;
    }
    private boolean tryFillTarget(Level level, BlockPos targetPos, int zoneId) {
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.is(Blocks.CAULDRON)) {
            if (tank.getFluidAmount() >= 333) {
                level.setBlock(targetPos, Blocks.WATER_CAULDRON.defaultBlockState()
                        .setValue(LayeredCauldronBlock.LEVEL, 1), 2);
                tank.drain(333, IFluidHandler.FluidAction.EXECUTE);
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
                tank.drain(333, IFluidHandler.FluidAction.EXECUTE);
                level.blockEvent(worldPosition, getBlockState().getBlock(), 1, zoneId);
                setChanged();
                syncTile(false);
            }
            return true;
        }
        BlockEntity te = level.getBlockEntity(targetPos);
        if (te != null) {
            LazyOptional<IFluidHandler> cap = te.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP);
            if (cap.isPresent()) {
                IFluidHandler handler = cap.orElse(null);
                if (handler != null) {
                    int filled = handler.fill(new FluidStack(Fluids.WATER, 25), IFluidHandler.FluidAction.EXECUTE);
                    if (filled > 0) {
                        tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
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
            if (level != null && level.isClientSide) {
                visualZone = param;
                visualCounter = 5;
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side == Direction.UP && cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }
    public int getWaterLevel() {
        return tank.getFluidAmount();
    }
    public boolean isFull() {
        return tank.getFluidAmount() >= TANK_CAPACITY;
    }
    private class WaterJugFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }
        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluid();
        }
        @Override
        public int getTankCapacity(int tankIndex) {
            return TANK_CAPACITY;
        }
        @Override
        public boolean isFluidValid(int tankIndex, @Nonnull FluidStack stack) {
            return false;
        }
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }
        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!resource.getFluid().isSame(Fluids.WATER)) {
                return FluidStack.EMPTY;
            }
            boolean wasFull = tank.getFluidAmount() >= TANK_CAPACITY;
            FluidStack drained = tank.drain(resource, action);
            if (action.execute()) {
                setChanged();
                if (wasFull && tank.getFluidAmount() < TANK_CAPACITY) {
                    syncTile(false);
                }
            }
            return drained;
        }
        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            boolean wasFull = tank.getFluidAmount() >= TANK_CAPACITY;
            FluidStack drained = tank.drain(maxDrain, action);
            if (action.execute()) {
                setChanged();
                if (wasFull && tank.getFluidAmount() < TANK_CAPACITY) {
                    syncTile(false);
                }
            }
            return drained;
        }
    }
}
