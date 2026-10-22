package thaumictinker.attractor;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

 



 
public class CorporealAttractorBlockEntity extends AttractorBlockEntity implements Container, MenuProvider {

    private boolean adult = true;
    private ItemStack filter = ItemStack.EMPTY;

    public CorporealAttractorBlockEntity(BlockPos pos, BlockState state) {
        super(AttractorFeature.CORPOREAL_ENTITY, pos, state);
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        if (this.adult != adult) {
            this.adult = adult;
            sync();
        }
    }

    public ItemStack filter() {
        return filter;
    }

      
    public void dropContents() {
        if (level != null && !level.isClientSide() && !filter.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), filter);
            filter = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean applies(Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
            return false;
        }
        boolean can = true;
        if (entity instanceof AgeableMob ageable) {
            can = adult != ageable.isBaby();
        }
        if (can && !filter.isEmpty()) {
            String pattern = SoulMouldItem.getPatternName(filter);
            return pattern != null && !pattern.isEmpty() && pattern.equals(SoulMouldItem.patternOf(living));
        }
        return can;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return filter.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? filter : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || filter.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = filter.split(amount);
        sync();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = filter;
        filter = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            filter = stack;
            stack.setCount(Math.min(stack.getCount(), getMaxStackSize()));
            sync();
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof SoulMouldItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        filter = ItemStack.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumictinker.corporeal_attractor");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CorporealAttractorMenu(containerId, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        adult = tag.getBoolean("adultCheck");
        filter = ItemStack.of(tag.getCompound("Filter"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("adultCheck", adult);
        if (!filter.isEmpty()) {
            tag.put("Filter", filter.save(new CompoundTag()));
        }
    }
}
