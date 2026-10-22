package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;


import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileHungryChest extends TileArcaneresearch implements Container {

    public static final int SIZE = 27;
    public static final double PICKUP_RANGE = 2.5;
    public float lidAngle = 0.0f;
    public float prevLidAngle = 0.0f;
    public int numPlayersUsing = 0;
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private int tickCount = 0;

    public TileHungryChest(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileHungryChest(BlockPos pos, BlockState state) {
        this(ModBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileHungryChest tile) {
        tile.tickCount++;

        if (tile.tickCount % 10 == 0) {
            tile.pickupNearbyItems();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileHungryChest tile) {
        tile.prevLidAngle = tile.lidAngle;

        if (tile.numPlayersUsing > 0 && tile.lidAngle < 1.0f) {
            tile.lidAngle += 0.1f;
            if (tile.lidAngle > 1.0f) tile.lidAngle = 1.0f;
        }
        if (tile.numPlayersUsing == 0 && tile.lidAngle > 0.0f) {
            tile.lidAngle -= 0.1f;
            if (tile.lidAngle < 0.0f) tile.lidAngle = 0.0f;
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    private void pickupNearbyItems() {
        if (level == null) return;

        AABB pickupBox = new AABB(
                worldPosition.getX() - PICKUP_RANGE,
                worldPosition.getY() - PICKUP_RANGE,
                worldPosition.getZ() - PICKUP_RANGE,
                worldPosition.getX() + 1 + PICKUP_RANGE,
                worldPosition.getY() + 1 + PICKUP_RANGE,
                worldPosition.getZ() + 1 + PICKUP_RANGE
        );

        List<ItemEntity> itemEntities = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class), pickupBox, e -> true);

        boolean absorbed = false;
        for (ItemEntity itemEntity : itemEntities) {
            if (itemEntity.isRemoved() || itemEntity.hasPickUpDelay()) continue;

            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;

            ItemStack remaining = insertItem(stack);

            if (remaining.isEmpty()) {

                itemEntity.discard();
                playPickupSound();
                absorbed = true;
            } else if (remaining.getCount() < stack.getCount()) {

                itemEntity.setItem(remaining);
                playPickupSound();
                absorbed = true;
            }
        }
        if (absorbed) {
            
            
            
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 2);
        }
    }

    public ItemStack insertItem(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack toInsert = stack.copy();

        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, toInsert)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, toInsert.getCount());
                    slot.grow(toAdd);
                    toInsert.shrink(toAdd);
                    if (toInsert.isEmpty()) {
                        setChanged();
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        for (int i = 0; i < SIZE; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, toInsert);
                setChanged();
                return ItemStack.EMPTY;
            }
        }

        return toInsert;
    }

    private void playPickupSound() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2f,
                    0.8f + level.getRandom().nextFloat() * 0.4f);
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
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
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
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
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) {
            if (numPlayersUsing < 0) numPlayersUsing = 0;
            numPlayersUsing++;
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, numPlayersUsing);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
        }
    }

    
    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!user.getLivingEntity().isSpectator()) {
            numPlayersUsing--;
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, numPlayersUsing);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {
            numPlayersUsing = param;
            return true;
        }
        
        
        if (id == 2) {
            if (lidAngle < param / 10.0F) lidAngle = param / 10.0F;
            return true;
        }
        return super.triggerEvent(id, param);
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
