package thaumictinker.kami.gateway;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

 



 
public class WarpGateBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SIZE = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean locked;
    private long teleportedAt = Long.MIN_VALUE;

    public WarpGateBlockEntity(BlockPos pos, BlockState state) {
        super(GatewayFeature.WARP_GATE_ENTITY, pos, state);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            sync();
        }
    }

    public AABB standingZone() {
        return new AABB(worldPosition.getX(), worldPosition.getY() + 1.0, worldPosition.getZ(),
                worldPosition.getX() + 1.0, worldPosition.getY() + 1.5, worldPosition.getZ() + 1.0);
    }

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        
        return new AABB(worldPosition).expandTowards(0.0, 3.0, 0.0).inflate(1.0);
    }

    public boolean isStandingOn(Player player) {
        return player.getBoundingBox().intersects(standingZone().inflate(0.5));
    }

    public void teleportPlayer(ServerPlayer player, int index) {
        if (level == null || teleportedAt == level.getGameTime() || index < 0 || index >= SIZE
                || !isStandingOn(player)) {
            return;
        }
        PearlLocation location = SkyPearlItem.location(items.get(index));
        if (location != null && GatewayTeleport.teleport(player, location)) {
            teleportedAt = level.getGameTime();
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            sync();
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
        stack.setCount(Math.min(stack.getCount(), getMaxStackSize()));
        sync();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof SkyPearlItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents(ServerLevel level) {
        Containers.dropContents(level, worldPosition, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumictinker.warp_gate");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WarpGateMenu(containerId, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        locked = tag.getBoolean("locked");
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("locked", locked);
        ContainerHelper.saveAllItems(tag, items, true);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
