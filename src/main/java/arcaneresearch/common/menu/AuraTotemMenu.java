package arcaneresearch.common.menu;
import arcaneresearch.common.tiles.aura.AuraTotemTile;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.items.resources.ItemCrystalEssence;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.menu.slot.LimitedByClassSlot;
import arcaneresearch.init.ModMenuTypes;

 


 
public class AuraTotemMenu extends AbstractContainerMenu {
    private final Container totem;
    private final ContainerLevelAccess access;
    private int lastTime;
    private final ContainerData data = new SimpleContainerData(1);

    public AuraTotemMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getTotem(playerInventory, extraData));
    }

    public AuraTotemMenu(int containerId, Inventory playerInventory, AuraTotemTile totem) {
        super(ModMenuTypes.AURA_TOTEM.get(), containerId);
        this.totem = totem;
        this.access = ContainerLevelAccess.create(totem.getLevel(), totem.getBlockPos());
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
        if (be instanceof AuraTotemTile totem) {
            return totem;
        }
        throw new IllegalStateException("Block entity is not an AuraTotemTile");
    }

    public Container getTotem() {
        return this.totem;
    }

    public int getLastTime() {
        return this.lastTime;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, AuraBlocks.AURA_TOTEM.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        ItemStack result = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            result = stackInSlot.copy();
            if (slot == 0) {
                if (!this.totem.canPlaceItem(slot, stackInSlot)
                        || !moveItemStackTo(stackInSlot, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.totem.canPlaceItem(slot, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return result;
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        listener.dataChanged(this, 0, this.data.get(0));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (this.totem instanceof AuraTotemTile auraTotem && this.lastTime != auraTotem.time) {
            this.data.set(0, auraTotem.time);
        }
        this.lastTime = this.data.get(0);
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
        if (id == 0) {
            this.data.set(0, value);
            if (this.totem instanceof AuraTotemTile auraTotem) {
                auraTotem.time = value;
            }
        }
    }
}
