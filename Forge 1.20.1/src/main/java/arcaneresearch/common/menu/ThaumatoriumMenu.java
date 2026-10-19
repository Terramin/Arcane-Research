package arcaneresearch.common.menu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;
import arcaneresearch.init.ModMenuTypes;
public class ThaumatoriumMenu extends AbstractContainerMenu {
    private final TileThaumatorium thaumatorium;
    private final Player player;
    public ThaumatoriumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }
    public ThaumatoriumMenu(int containerId, Inventory playerInventory, TileThaumatorium blockEntity) {
        super(ModMenuTypes.THAUMATORIUM.get(), containerId);
        this.thaumatorium = blockEntity;
        this.player = playerInventory.player;
        this.thaumatorium.eventHandler = this;
        addSlot(new Slot(blockEntity, 0, 55, 24));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 135 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 193));
        }
        thaumatorium.updateRecipes(player);
    }
    private static TileThaumatorium getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileThaumatorium thaumatorium) return thaumatorium;
        throw new IllegalStateException("Block entity is not a TileThaumatorium");
    }
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        thaumatorium.updateRecipes(player);
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!thaumatorium.getLevel().isClientSide) thaumatorium.eventHandler = null;
    }
    @Override
    public boolean stillValid(Player player) {
        return thaumatorium.stillValid(player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack inSlot = slot.getItem();
            result = inSlot.copy();
            if (index != 0) {
                if (!moveItemStackTo(inSlot, 0, 1, false)) return ItemStack.EMPTY;
            } else if (index >= 1 && index < 28) {
                if (!moveItemStackTo(inSlot, 28, 37, false)) return ItemStack.EMPTY;
            } else {
                if (index >= 28 && index < 37 && !moveItemStackTo(inSlot, 1, 28, false)) return ItemStack.EMPTY;
                if (!moveItemStackTo(inSlot, 1, 37, false)) return ItemStack.EMPTY;
            }
            if (inSlot.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (inSlot.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, inSlot);
        }
        return result;
    }
    public TileThaumatorium getBlockEntity() {
        return thaumatorium;
    }
}
