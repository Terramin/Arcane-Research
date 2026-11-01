package arcaneresearch.common.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collections;

public abstract class TileArcaneresearchInventory extends TileArcaneresearch implements Container {

    protected final NonNullList<ItemStack> items;
    protected int[] syncedSlots = new int[0];
    protected String customName;

    @Nullable
    private BitSet syncedSlotBits;

    private LazyOptional<?>[] itemHandlers;

    public TileArcaneresearchInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int inventorySize) {
        super(type, pos, state);
        this.items = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
    }

    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    public ItemStack getSyncedStackInSlot(int index) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        return isSyncedSlot(index) ? items.get(index) : ItemStack.EMPTY;
    }

    protected void invalidateSyncedSlotCache() {
        this.syncedSlotBits = null;
    }

    protected boolean isSyncedSlot(int slot) {
        if (slot < 0) {
            return false;
        }
        int[] slots = syncedSlots;
        if (slots == null || slots.length == 0) {
            return false;
        }
        BitSet bits = syncedSlotBits;
        if (bits == null) {
            bits = new BitSet();
            for (int s : slots) {
                if (s >= 0) {
                    bits.set(s);
                }
            }
            syncedSlotBits = bits;
        }
        return bits.get(slot);
    }

    public String getName() {
        return hasCustomName() ? customName : "container.arcaneresearch";
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public void setCustomName(String name) {
        this.customName = name;
        markDirtyAndSync();
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            if (isSyncedSlot(slot)) {
                syncTile(false);
            } else {
                setChanged();
            }
        }
        return result;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) {
            if (isSyncedSlot(slot)) {
                syncTile(false);
            } else {
                setChanged();
            }
        }
        return result;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        if (isSyncedSlot(slot)) {
            syncTile(false);
        } else {
            setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        Collections.fill(items, ItemStack.EMPTY);
        setChanged();
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (itemHandlers == null) {
                itemHandlers = new LazyOptional[7];
            }
            if (side == null) {
                if (itemHandlers[6] == null) {
                    itemHandlers[6] = LazyOptional.of(() -> new InvWrapper(this));
                }
                return (LazyOptional<T>) itemHandlers[6];
            }
            int index = side.ordinal();
            if (itemHandlers[index] == null) {
                if (this instanceof WorldlyContainer worldlyContainer) {
                    itemHandlers[index] = LazyOptional.of(() -> new SidedInvWrapper(worldlyContainer, side));
                } else {
                    itemHandlers[index] = LazyOptional.of(() -> new InvWrapper(this));
                }
            }
            return (LazyOptional<T>) itemHandlers[index];
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        if (itemHandlers != null) {
            for (LazyOptional<?> handler : itemHandlers) {
                if (handler != null) {
                    handler.invalidate();
                }
            }
            itemHandlers = null;
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        if (itemHandlers != null) {
            for (LazyOptional<?> handler : itemHandlers) {
                if (handler != null) {
                    handler.invalidate();
                }
            }
        }
        itemHandlers = null;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        if (hasCustomName()) {
            tag.putString("CustomName", customName);
        }
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        customName = tag.contains("CustomName") ? tag.getString("CustomName") : null;
        Collections.fill(items, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    public void dropContents() {
        if (level != null && !level.isClientSide) {
            Containers.dropContents(level, worldPosition, this);
            clearContent();
        }
    }
}