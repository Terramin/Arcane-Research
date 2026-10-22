package arcaneresearch.common.menu;

import arcaneresearch.common.menu.slot.DeconstructSlot;
import arcaneresearch.common.tiles.crafting.DeconstructTile;
import arcaneresearch.init.ModMenuTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

 




 
public class DeconstructMenu extends AbstractContainerMenu {

      
    public static final int BUTTON_COLLECT = 1;

    private final BlockPos pos;
    private final DeconstructTile tile;
    private final ContainerData data;

    public DeconstructMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readBlockPos());
    }

    public DeconstructMenu(int containerId, Inventory inventory, DeconstructTile tile) {
        this(containerId, inventory, tile.getBlockPos());
    }

    public DeconstructMenu(int containerId, Inventory inventory, BlockPos pos) {
        super(ModMenuTypes.DECONSTRUCTION_TABLE.get(), containerId);
        this.pos = pos;
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        this.tile = be instanceof DeconstructTile decon ? decon : null;
        Container container = tile != null ? tile : new SimpleContainer(1);
        this.data = tile != null && !inventory.player.level().isClientSide()
                ? tile.dataAccess() : new SimpleContainerData(1);

        addSlot(new DeconstructSlot(container, 0, 64, 16));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public BlockPos pos() {
        return pos;
    }

    public DeconstructTile tile() {
        return tile;
    }

      
    public int breaktime() {
        return data.get(0);
    }

    public int breaktimeScaled(int scale) {
        return breaktime() * scale / DeconstructTile.BREAK_TICKS;
    }

      
    public arcaneresearch.api.aspects.Aspect currentAspect() {
        return tile != null ? tile.currentAspect() : null;
    }

      
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == BUTTON_COLLECT && tile != null && player instanceof ServerPlayer) {
            return tile.claimAspect(player) != null;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return tile != null && tile.stillValid(player);
    }

      
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack inSlot = slot.getItem();
        ItemStack original = inSlot.copy();

        if (slotIndex == 0) {
            if (!moveItemStackTo(inSlot, 1, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (DeconstructTile.hasAspects(inSlot)) {
            if (!moveItemStackTo(inSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < 28) {
            if (!moveItemStackTo(inSlot, 28, 37, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < 37) {
            if (!moveItemStackTo(inSlot, 1, 28, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (inSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (inSlot.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, inSlot);
        return original;
    }
}
