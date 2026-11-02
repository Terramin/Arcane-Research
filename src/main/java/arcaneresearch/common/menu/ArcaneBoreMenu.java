package arcaneresearch.common.menu;

import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.common.menu.slot.PickaxeSlot;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArcaneBoreMenu extends AbstractContainerMenu {

    private final EntityArcaneBore bore;

    public ArcaneBoreMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getEntity(playerInventory, extraData));
    }

    public ArcaneBoreMenu(int containerId, Inventory playerInventory, EntityArcaneBore bore) {
        super(ModMenuTypes.ARCANE_BORE.get(), containerId);
        this.bore = bore;

        addSlot(new PickaxeSlot(bore, 0, 80, 29));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static EntityArcaneBore getEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        int entityId = extraData.readInt();
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof EntityArcaneBore bore) return bore;
        throw new IllegalStateException("Entity " + entityId + " is not an Arcane Bore: " + entity);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return bore != null && bore.isAlive() && bore.distanceTo(player) < 8.0f;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index == 0) {
            if (!moveItemStackTo(stackInSlot, 1, total, true)) return ItemStack.EMPTY;
        } else if (PickaxeSlot.isValidPickaxe(stackInSlot)) {
            if (!moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < 28) {
            if (!moveItemStackTo(stackInSlot, 28, total, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stackInSlot, 1, 28, false)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    public EntityArcaneBore getBore() {
        return bore;
    }
}