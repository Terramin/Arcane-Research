package arcaneresearch.common.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// NEOFORGE_PORT: Forge capability plumbing (Capability/LazyOptional/InvWrapper) removed;
//   item access is exposed centrally via RegisterCapabilitiesEvent (see ModBlockEntities).
public abstract class TileArcaneresearchInventory extends TileArcaneresearch implements Container {

    protected NonNullList<ItemStack> items;
    protected int[] syncedSlots = new int[0];
    protected String customName;

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

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        if (hasCustomName()) {
            tag.putString("CustomName", customName);
        }
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        // NEOFORGE_PORT: ValueInput.getStringOr returns String (no Optional).
        String name = tag.getStringOr("CustomName", "");
        customName = name.isEmpty() ? null : name;

        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(tag, items);
    }

    public void dropContents() {
        if (level != null && !level.isClientSide()) {
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
