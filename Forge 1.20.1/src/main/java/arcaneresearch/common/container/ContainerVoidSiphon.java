package arcaneresearch.common.container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.container.slot.SlotOutput;
import arcaneresearch.common.tiles.crafting.TileVoidSiphon;
import arcaneresearch.init.ModMenuTypes;
@Deprecated
public class ContainerVoidSiphon extends AbstractContainerMenu {
    private final TileVoidSiphon siphon;
    private final ContainerData data;
    public ContainerVoidSiphon(Inventory playerInventory, TileVoidSiphon tileEntity) {
        super(ModMenuTypes.VOID_SIPHON.get(), containerIdOf(playerInventory));
        siphon = tileEntity;
        data = createDataAccess(tileEntity);
        addSlot(new SlotOutput(tileEntity, 0, 80, 32));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
        addDataSlots(data);
    }
    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
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
        return siphon.stillValid(player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index == 0) {
                if (!siphon.canPlaceItem(0, stackInSlot)
                        || !moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!siphon.canPlaceItem(index, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
    public TileVoidSiphon getBlockEntity() {
        return siphon;
    }
    public int getProgress() {
        return data.get(0);
    }
}
