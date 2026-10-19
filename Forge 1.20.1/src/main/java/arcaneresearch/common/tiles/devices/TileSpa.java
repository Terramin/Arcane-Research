package arcaneresearch.common.tiles.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import arcaneresearch.common.items.consumables.ItemBathSalts;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public class TileSpa extends TileArcaneresearch implements Container {
    public static final int TANK_CAPACITY = 5000;
    public final FluidTank tank = new FluidTank(TANK_CAPACITY);
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);
    public boolean mixMode = true;
    private ItemStack bathSalts = ItemStack.EMPTY;
    private int counter = 0;
    public TileSpa(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileSpa(BlockPos pos, BlockState state) {
        this(ModBlockEntities.SPA.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileSpa tile) {
        tile.counter++;
        if (tile.counter % 40 != 0 || level.hasNeighborSignal(pos)) {
            return;
        }
        if (tile.mixMode) {
            if (tile.tank.getFluidAmount() >= 1000
                    && tile.tank.getFluid().getFluid() == Fluids.WATER
                    && !tile.bathSalts.isEmpty()
                    && tile.bathSalts.getItem() instanceof ItemBathSalts) {
                BlockState target = ModBlocks.PURIFYING_FLUID.get().defaultBlockState();
                BlockPos placePos = tile.findPlacePos(target, true);
                if (placePos == null) {
                    placePos = tile.findPlacePos(target, false);
                }
                if (placePos != null) {
                    tile.bathSalts.shrink(1);
                    tile.tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    level.setBlock(placePos, target, 3);
                    tile.setChanged();
                    tile.syncTile(false);
                }
            }
        } else {
            FluidStack stored = tile.tank.getFluid();
            if (!stored.isEmpty() && stored.getAmount() >= 1000) {
                BlockState target = fluidSourceState(stored.getFluid());
                if (target != null) {
                    BlockPos placePos = tile.findPlacePos(target, true);
                    if (placePos == null) {
                        placePos = tile.findPlacePos(target, false);
                    }
                    if (placePos != null) {
                        tile.tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                        level.setBlock(placePos, target, 3);
                        tile.setChanged();
                        tile.syncTile(false);
                    }
                }
            }
        }
    }
    @Nullable
    private static BlockState fluidSourceState(Fluid fluid) {
        if (fluid == Fluids.WATER) {
            return Blocks.WATER.defaultBlockState();
        }
        if (fluid == Fluids.LAVA) {
            return Blocks.LAVA.defaultBlockState();
        }
        return null;
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("mix", mixMode);
        tank.writeToNBT(tag);
        if (!bathSalts.isEmpty()) {
            tag.put("BathSalts", bathSalts.save(new CompoundTag()));
        }
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        mixMode = tag.contains("mix") ? tag.getBoolean("mix")
                : (!tag.contains("MixMode") || tag.getBoolean("MixMode"));
        tank.readFromNBT(tag);
        if (tag.contains("BathSalts")) {
            bathSalts = ItemStack.of(tag.getCompound("BathSalts"));
        } else {
            bathSalts = ItemStack.EMPTY;
        }
    }
    @Nullable
    private BlockPos findPlacePos(BlockState target, boolean mustBeAdjacent) {
        if (level == null) {
            return null;
        }
        if (!mustBeAdjacent) {
            BlockPos above = worldPosition.above();
            if (isValidLocation(above, false, target)) {
                return above;
            }
            return null;
        }
        for (int xx = -2; xx <= 2; xx++) {
            for (int zz = -2; zz <= 2; zz++) {
                BlockPos candidate = worldPosition.offset(xx, 1, zz);
                if (isValidLocation(candidate, true, target)) {
                    return candidate;
                }
            }
        }
        return null;
    }
    private boolean isValidLocation(BlockPos pos, boolean mustBeAdjacent, BlockState target) {
        if (level == null) {
            return false;
        }
        Fluid targetFluid = target.getFluidState().getType();
        if ((targetFluid == Fluids.WATER || target.getBlock() == Blocks.LAVA)
                && level.dimensionType().ultraWarm()) {
            return false;
        }
        BlockPos below = pos.below();
        if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return false;
        }
        BlockState current = level.getBlockState(pos);
        if (!current.canBeReplaced()) {
            return false;
        }
        if (current.getBlock() == target.getBlock()
                && current.getFluidState().isSource()
                && current.getFluidState().getType() == targetFluid) {
            return false;
        }
        if (mustBeAdjacent) {
            boolean touches = false;
            for (Direction dir : Direction.values()) {
                BlockState neighbor = level.getBlockState(pos.relative(dir));
                if (neighbor.getBlock() == target.getBlock()
                        && neighbor.getFluidState().isSource()) {
                    touches = true;
                    break;
                }
            }
            if (!touches) {
                return false;
            }
        }
        return true;
    }
    public void toggleMix() {
        mixMode = !mixMode;
        setChanged();
        syncTile(false);
    }
    @Override
    public int getContainerSize() {
        return 1;
    }
    @Override
    public boolean isEmpty() {
        return bathSalts.isEmpty();
    }
    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? bathSalts : ItemStack.EMPTY;
    }
    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || bathSalts.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = bathSalts.split(amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack result = bathSalts;
        bathSalts = ItemStack.EMPTY;
        return result;
    }
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            bathSalts = stack;
            setChanged();
        }
    }
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
    @Override
    public void clearContent() {
        bathSalts = ItemStack.EMPTY;
    }
    public int[] getSlotsForFace(Direction face) {
        return face == Direction.UP ? new int[0] : new int[]{0};
    }
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        return face != Direction.UP && slot == 0 && stack.getItem() instanceof ItemBathSalts;
    }
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        return face != Direction.UP;
    }
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }
}
