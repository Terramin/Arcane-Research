package arcaneresearch.common.menu;

import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Container menu of the alchemy furnace: slot 0 holds the catalyst, slots 1..27 the
 * main inventory and 28..36 the hotbar. The tile is updated when the menu opens and
 * whenever the player selects a recipe from the synchronized list.
 */
public class ThaumatoriumMenu extends AbstractContainerMenu {

    private final TileThaumatorium thaumatorium;
    private final Player player;

    /** Client-side constructor reading the furnace position from the packet payload. */
    public ThaumatoriumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    /** Server-side constructor bound to an existing furnace block entity. */
    public ThaumatoriumMenu(int containerId, Inventory playerInventory, TileThaumatorium blockEntity) {
        super(ModMenuTypes.THAUMATORIUM.get(), containerId);
        this.thaumatorium = blockEntity;
        this.player = playerInventory.player;
        this.thaumatorium.eventHandler = this;

        addSlot(new Slot(blockEntity, 0, 55, 24));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 135 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 193));
        }

        thaumatorium.updateRecipes(player);
    }

    private static TileThaumatorium getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileThaumatorium tile) return tile;
        throw new IllegalStateException("Block entity is not a TileThaumatorium: " + be);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        thaumatorium.updateRecipes(player);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        Level level = thaumatorium.getLevel();
        if (level == null || !level.isClientSide) {
            thaumatorium.eventHandler = null;
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return thaumatorium != null && thaumatorium.stillValid(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        boolean moved = false;
        for (int[] range : arcaneresearch.common.tiles.crafting.ThaumatoriumRules.moveAttempts(index, total)) {
            if (moveItemStackTo(stackInSlot, range[0], range[1], false)) {
                moved = true;
                break;
            }
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

    public TileThaumatorium getBlockEntity() {
        return thaumatorium;
    }
}