package arcaneresearch.common.menu;

import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;
import arcaneresearch.common.menu.slot.ArrowSlot;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TurretMenu extends AbstractContainerMenu {

    private final EntityTurretCrossbow turret;
    private final boolean isAdvanced;

    public TurretMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getEntity(playerInventory, extraData));
    }

    public TurretMenu(int containerId, Inventory playerInventory, EntityTurretCrossbow turret) {
        super(getMenuType(turret), containerId);
        this.turret = turret;
        this.isAdvanced = turret instanceof EntityTurretCrossbowAdvanced;

        int arrowX = this.isAdvanced ? 42 : 80;
        addSlot(new ArrowSlot(turret, 0, arrowX, 29));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    private static MenuType<?> getMenuType(EntityTurretCrossbow turret) {
        return turret instanceof EntityTurretCrossbowAdvanced
                ? ModMenuTypes.TURRET_ADVANCED.get()
                : ModMenuTypes.TURRET_BASIC.get();
    }

    private static EntityTurretCrossbow getEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        int entityId = extraData.readInt();
        Entity entity = playerInventory.player.level().getEntity(entityId);
        if (entity instanceof EntityTurretCrossbow turret) return turret;
        throw new IllegalStateException("Entity " + entityId + " is not a turret crossbow: " + entity);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int button) {
        if (!(turret instanceof EntityTurretCrossbowAdvanced advanced)) return false;

        switch (button) {
            case 1 -> {
                advanced.setTargetAnimal(!advanced.getTargetAnimal());
                return true;
            }
            case 2 -> {
                advanced.setTargetMob(!advanced.getTargetMob());
                return true;
            }
            case 3 -> {
                advanced.setTargetPlayer(!advanced.getTargetPlayer());
                return true;
            }
            case 4 -> {
                advanced.setTargetFriendly(!advanced.getTargetFriendly());
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return turret != null && turret.isAlive() && turret.distanceTo(player) < 8.0f;
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
        } else if (ArrowSlot.isValidArrow(stackInSlot)) {
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

    public EntityTurretCrossbow getTurret() {
        return turret;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }
}