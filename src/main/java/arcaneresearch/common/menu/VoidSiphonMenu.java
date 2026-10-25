package arcaneresearch.common.menu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.menu.slot.OutputSlot;
import arcaneresearch.common.tiles.crafting.TileVoidSiphon;
import arcaneresearch.init.ModMenuTypes;
public class VoidSiphonMenu extends AbstractContainerMenu {
    private final TileVoidSiphon blockEntity;
    private final ContainerData data;
    public VoidSiphonMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData), new SimpleContainerData(1));
    }
    public VoidSiphonMenu(int containerId, Inventory playerInventory, TileVoidSiphon blockEntity) {
        this(containerId, playerInventory, blockEntity, createDataAccess(blockEntity));
    }
    private VoidSiphonMenu(int containerId, Inventory playerInventory, TileVoidSiphon blockEntity, ContainerData data) {
        super(ModMenuTypes.VOID_SIPHON.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;
        addSlot(new OutputSlot(blockEntity, 0, 80, 32));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }
    private static TileVoidSiphon getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileVoidSiphon tile) {
            return tile;
        }
        throw new IllegalStateException("Block entity is not a TileVoidSiphon");
    }
    private static ContainerData createDataAccess(TileVoidSiphon tile) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> tile.getProgress();
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    tile.setProgress(value);
                }
            }
            @Override
            public int getCount() {
                return 1;
            }
        };
    }
    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index == 0) {
                if (!blockEntity.canPlaceItem(0, stackInSlot)
                        || !moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!blockEntity.canPlaceItem(0, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return result;
    }
    public TileVoidSiphon getBlockEntity() {
        return blockEntity;
    }
    public int getProgress() {
        return data.get(0);
    }
}
