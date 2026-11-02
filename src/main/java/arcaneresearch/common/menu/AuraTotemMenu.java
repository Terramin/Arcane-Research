package arcaneresearch.common.menu;

import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.menu.slot.LimitedByClassSlot;
import arcaneresearch.common.tiles.aura.AuraTotemTile;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class AuraTotemMenu extends AbstractContainerMenu {

    private final AuraTotemTile totem;
    private final ContainerLevelAccess access;
    private int lastTime;
    private final ContainerData data = new SimpleContainerData(1);

    public AuraTotemMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getTotem(playerInventory, extraData));
    }

    public AuraTotemMenu(int containerId, Inventory playerInventory, AuraTotemTile totem) {
        super(ModMenuTypes.AURA_TOTEM.get(), containerId);
        this.totem = totem;

        Level level = totem.getLevel();
        this.access = level != null
                ? ContainerLevelAccess.create(level, totem.getBlockPos())
                : ContainerLevelAccess.NULL;

        addSlot(new LimitedByClassSlot(ItemCrystalEssence.class, totem, 0, 80, 24));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(this.data);
        this.data.set(0, totem.time);
        this.lastTime = totem.time;
    }

    private static AuraTotemTile getTotem(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof AuraTotemTile totem) return totem;
        throw new IllegalStateException("Block entity is not an AuraTotemTile: " + be);
    }

    public Container getTotem() {
        return this.totem;
    }

    public int getLastTime() {
        return this.lastTime;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.totem == null) return false;
        Level level = this.totem.getLevel();
        if (level == null) return false;
        return stillValid(this.access, player, AuraBlocks.AURA_TOTEM.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        final int total = this.slots.size();
        if (slotIndex < 0 || slotIndex >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        boolean moved;
        if (slotIndex == 0) {
            moved = this.totem.canPlaceItem(slotIndex, stackInSlot)
                    && moveItemStackTo(stackInSlot, 1, total, true);
        } else {
            moved = this.totem.canPlaceItem(slotIndex, stackInSlot)
                    && moveItemStackTo(stackInSlot, 0, 1, false);
        }
        if (!moved) return ItemStack.EMPTY;

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public void addSlotListener(@NotNull ContainerListener listener) {
        super.addSlotListener(listener);
        listener.dataChanged(this, 0, this.data.get(0));
    }

    @Override
    public void broadcastChanges() {
        if (this.totem != null && this.lastTime != this.totem.time) {
            this.data.set(0, this.totem.time);
            this.lastTime = this.totem.time;
        }
        super.broadcastChanges();
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
        if (id == 0) {
            this.data.set(0, value);
            this.lastTime = value;
            if (this.totem != null) {
                this.totem.time = value;
            }
        }
    }
}