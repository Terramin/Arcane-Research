package arcaneresearch.common.tiles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public abstract class TileArcaneresearchInventory extends TileArcaneresearch implements Container {
    protected NonNullList<ItemStack> items;
    protected int[] syncedSlots = new int[0];
    protected String customName;
    @SuppressWarnings("unchecked")
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
    protected boolean isSyncedSlot(int slot) {
        for (int s : syncedSlots) {
            if (s == slot) {
                return true;
            }
        }
        return false;
    }
    public String getName() {
        return hasCustomName() ? customName : "container.arcaneresearch";
    }
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }
    public void setCustomName(String name) {
        this.customName = name;
        setChanged();
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
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            if (isSyncedSlot(slot)) {
                syncTile(false);
            }
        }
        return result;
    }
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (!result.isEmpty()) {
            setChanged();
            if (isSyncedSlot(slot)) {
                syncTile(false);
            }
        }
        return result;
    }
    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        if (isSyncedSlot(slot)) {
            syncTile(false);
        }
    }
    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }
    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }
    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (side != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            int index = side.ordinal();
            if (itemHandlers == null) {
                itemHandlers = new LazyOptional[6];
            }
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
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);
    }
    public void dropContents() {
        if (level != null && !level.isClientSide) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level,
                            worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                }
            }
            clearContent();
        }
    }
}
