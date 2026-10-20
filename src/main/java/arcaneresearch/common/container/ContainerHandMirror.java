package arcaneresearch.common.container;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.common.items.tools.ItemHandMirror;
import arcaneresearch.init.ModMenuTypes;
@Deprecated
public class ContainerHandMirror extends AbstractContainerMenu implements ContainerListener {
    public Container input;
    ItemStack mirror;
    Player player;
    private Level worldObj;
    private int posX;
    private int posY;
    private int posZ;
    public ContainerHandMirror(Inventory playerInventory, Level world, int x, int y, int z) {
        super(ModMenuTypes.HAND_MIRROR.get(), containerIdOf(playerInventory));
        input = new InventoryHandMirror(this);
        mirror = ItemStack.EMPTY;
        player = null;
        worldObj = world;
        posX = x;
        posY = y;
        posZ = z;
        player = playerInventory.player;
        mirror = playerInventory.getSelected();
        addSlot(new Slot(input, 0, 80, 24));
        bindPlayerInventory(playerInventory);
        slotsChanged(input);
    }
    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }
    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
        }
    }
    @Override
    public void containerChanged(Container container) {
        slotsChanged(container);
    }
    @Override
    public void slotsChanged(Container container) {
        ItemStack stackInSlot = input.getItem(0);
        if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameTags(stackInSlot, mirror)) {
            player.closeContainer();
            return;
        }
        if (!worldObj.isClientSide && !stackInSlot.isEmpty() && player != null) {
            ItemStack toTransport = stackInSlot.copy();
            input.setItem(0, ItemStack.EMPTY);
            input.setChanged();
            if (ItemHandMirror.transport(mirror, toTransport, player, worldObj)) {
                broadcastChanges();
            } else {
                input.setItem(0, toTransport);
                input.setChanged();
            }
        }
    }
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot != null && slot.hasItem() && slot.getItem().getItem() instanceof ItemHandMirror) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            if (stackInSlot.getItem() instanceof ItemHandMirror) {
                return ItemStack.EMPTY;
            }
            result = stackInSlot.copy();
            if (index == 0) {
                if (!moveItemStackTo(stackInSlot, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
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
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    @Override
    public void removed(Player par1EntityPlayer) {
        super.removed(par1EntityPlayer);
        if (!worldObj.isClientSide) {
            ItemStack remaining = input.removeItemNoUpdate(0);
            if (!remaining.isEmpty()) {
                par1EntityPlayer.drop(remaining, false);
            }
        }
    }
    private boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse, int limit) {
        boolean merged = false;
        int slotIndex = start;
        if (reverse) {
            slotIndex = end - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && ((!reverse && slotIndex < end) || (reverse && slotIndex >= start))) {
                Slot slot = slots.get(slotIndex);
                ItemStack existing = slot.getItem();
                if (!existing.isEmpty() && existing.getItem() == stack.getItem()
                        && (!stack.isDamageableItem() || stack.getDamageValue() == existing.getDamageValue())
                        && ItemStack.isSameItemSameTags(stack, existing)) {
                    int combined = existing.getCount() + stack.getCount();
                    int max = Math.min(stack.getMaxStackSize(), limit);
                    if (combined <= max) {
                        stack.setCount(0);
                        existing.setCount(combined);
                        slot.setChanged();
                        merged = true;
                    } else if (existing.getCount() < max) {
                        stack.shrink(max - existing.getCount());
                        existing.setCount(max);
                        slot.setChanged();
                        merged = true;
                    }
                }
                if (reverse) {
                    --slotIndex;
                } else {
                    ++slotIndex;
                }
            }
        }
        if (!stack.isEmpty()) {
            slotIndex = reverse ? end - 1 : start;
            while ((!reverse && slotIndex < end) || (reverse && slotIndex >= start)) {
                Slot slot = slots.get(slotIndex);
                ItemStack existing = slot.getItem();
                if (existing.isEmpty()) {
                    ItemStack placed = stack.copy();
                    placed.setCount(Math.min(placed.getCount(), limit));
                    slot.set(placed);
                    slot.setChanged();
                    stack.shrink(placed.getCount());
                    merged = true;
                    break;
                }
                if (reverse) {
                    --slotIndex;
                } else {
                    ++slotIndex;
                }
            }
        }
        return merged;
    }
}
