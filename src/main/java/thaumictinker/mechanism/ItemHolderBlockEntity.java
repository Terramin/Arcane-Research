package thaumictinker.mechanism;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import thaumictinker.mobilizer.MovableTile;

 



 
public abstract class ItemHolderBlockEntity extends BlockEntity implements WorldlyContainer, Clearable, MovableTile {

    private static final int[] SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    private ItemStack item = ItemStack.EMPTY;

    protected ItemHolderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

      
    public abstract boolean accepts(ItemStack stack);

      
    protected boolean exposesSlot(Direction face) {
        return true;
    }

    public ItemStack getTheItem() {
        return item;
    }

    public ItemStack removeTheItem() {
        ItemStack result = item;
        item = ItemStack.EMPTY;
        sync();
        return result;
    }

    public ItemStack splitTheItem(int count) {
        ItemStack result = item.split(count);
        if (item.isEmpty()) {
            item = ItemStack.EMPTY;
        }
        sync();
        return result;
    }

    public void setTheItem(ItemStack stack) {
        item = stack;
        sync();
    }

    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return item.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? item : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return slot == 0 ? splitTheItem(count) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slot == 0 ? removeTheItem() : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            setTheItem(stack);
        }
    }

    @Override
    public void clearContent() {
        item = ItemStack.EMPTY;
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    

    @Override
    public int[] getSlotsForFace(Direction face) {
        return exposesSlot(face) ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction face) {
        return accepts(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {
        return exposesSlot(face);
    }

    

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!item.isEmpty()) {
            tag.put("Item", item.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        item = ItemStack.of(tag.getCompound("Item"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!item.isEmpty()) {
            tag.put("Item", item.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

      
    public void dropContents(net.minecraft.world.level.Level level, BlockPos pos) {
        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, removeTheItem());
    }
}
