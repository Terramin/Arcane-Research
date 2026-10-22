package arcaneresearch.common.container;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;



import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.common.container.slot.SlotGhostFull;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.seals.SealProvide;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketItemToClientContainer;
import arcaneresearch.init.ModMenuTypes;

import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Deprecated
public class ContainerLogistics extends AbstractContainerMenu {
    private final ContainerData data;
    public InventoryLogistics input;
    public int start;
    public int end;
    public String searchText;
    public boolean updated;
    private Level worldObj;
    private Player player;
    private TreeMap<String, ItemStack> items;
    private int lastTotal;
    private int lastStart;
    private int lastEnd;

    public ContainerLogistics(Inventory playerInventory, Level world) {
        super(ModMenuTypes.LOGISTICS.get(), containerIdOf(playerInventory));
        player = null;
        input = new InventoryLogistics();
        items = new TreeMap<String, ItemStack>();
        lastTotal = 0;
        start = 0;
        end = 0;
        searchText = "";
        lastStart = 0;
        lastEnd = 0;
        updated = false;
        worldObj = world;
        player = playerInventory.player;
        this.data = new SimpleContainerData(2);
        addDataSlots(data);
        for (int a = 0; a < input.getContainerSize(); ++a) {
            addSlot(new SlotGhostFull(input, a, 19 + a % 9 * 19, 19 + a / 9 * 19));
        }
        refreshItemList(true);
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    public void refreshItemList(boolean full) {
        int newTotal = lastTotal;
        TreeMap<String, ItemStack> ti = new TreeMap<String, ItemStack>();
        if (full) {
            newTotal = 0;
            CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(worldObj, player.blockPosition(), 32);
            for (SealEntity seal : seals) {
                if (seal.getSeal() instanceof SealProvide sealProvide && seal.getOwner().equals(player.getUUID().toString())) {
                    ResourceHandler<ItemResource> handler = ArcaneresearchInvHelper.getItemHandlerAt(worldObj, seal.getSealPos().pos, seal.getSealPos().face);
                    if (handler == null) {
                        continue;
                    }
                    for (int slot = 0; slot < handler.size(); ++slot) {
                        ItemStack stack = ItemUtil.getStack(handler, slot).copy();
                        if (stack.isEmpty()) {
                            continue;
                        }
                        if (sealProvide.matchesFilters(stack)) {
                            if (!searchText.isEmpty()
                                    && !stack.getHoverName().getString().toLowerCase().contains(searchText.toLowerCase())) {
                                continue;
                            }
                            String key = stack.getHoverName().getString() + "_" + stack.getDamageValue() + "_"
                                    
                                    + (ArcaneresearchInvHelper.hasCustomTag(stack) ? ArcaneresearchInvHelper.getCustomTag(stack).hashCode() : 0);
                            if (ti.containsKey(key)) {
                                ti.get(key).grow(stack.getCount());
                            } else {
                                ti.put(key, stack);
                            }
                            newTotal += stack.getCount();
                        }
                    }
                }
            }
        }
        if (lastTotal != newTotal || start != lastStart) {
            lastTotal = newTotal;
            if (full) {
                items = ti;
            }
            input.clearContent();
            int j = 0;
            int q = 0;
            for (String key2 : items.keySet()) {
                if (++j <= start * 9) {
                    continue;
                }
                input.setItem(q, items.get(key2).copy());
                if (++q >= input.getContainerSize()) {
                    break;
                }
            }
            end = Math.max(0, items.size() / 9 - 8);
        }
    }

    @Override
    public void broadcastChanges() {
        sendLargeSlotsToClient();
        super.broadcastChanges();
        data.set(0, start);
        data.set(1, end);
        lastStart = start;
        lastEnd = end;
    }

    private void sendLargeSlotsToClient() {

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (Slot slot : slots) {
            if (slot instanceof SlotGhostFull) {
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && itemstack.getCount() > itemstack.getMaxStackSize()) {
                    PacketHandler.sendToPlayer(new PacketItemToClientContainer(containerId, slot.index, itemstack), serverPlayer);
                }
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player par1EntityPlayer, int par2) {
        if (par2 == 22) {
            refreshItemList(true);
            return true;
        }
        if (par2 == 0) {
            if (start < Math.max(0, items.size() / 9 - 8)) {
                ++start;
                refreshItemList(false);
            }
            return true;
        }
        if (par2 == 1) {
            if (start > 0) {
                --start;
                refreshItemList(false);
            }
            return true;
        }
        if (par2 >= 100) {
            int s = par2 - 100;
            if (s >= 0 && s <= Math.max(0, items.size() / 9 - 8)) {
                start = s;
                refreshItemList(false);
            }
            return true;
        }
        return super.clickMenuButton(par1EntityPlayer, par2);
    }

    @Override
    public ItemStack quickMoveStack(Player par1EntityPlayer, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = slots.get(slot);
        if (slotObject != null && slotObject.hasItem()) {
            ItemStack stackInSlot = slotObject.getItem();
            stack = stackInSlot.copy();
            if (slot < input.getContainerSize()) {
                if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, input.getContainerSize(), slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!input.canPlaceItem(slot, stackInSlot) || !moveItemStackTo(stackInSlot, 0, input.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slotObject.set(ItemStack.EMPTY);
            } else {
                slotObject.setChanged();
            }
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }

    
    public void containerChanged(Container invBasic) {
        broadcastChanges();
    }

    public void setSearchText(String text) {
        this.searchText = text;
        this.start = 0;
        refreshItemList(true);
    }

    public void requestItem(ItemStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) {
            return;
        }
        CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(worldObj, player.blockPosition(), 32);
        for (SealEntity seal : seals) {
            if (seal.getSeal() instanceof SealProvide && seal.getOwner().equals(player.getUUID().toString())) {
                ResourceHandler<ItemResource> handler = ArcaneresearchInvHelper.getItemHandlerAt(worldObj, seal.getSealPos().pos, seal.getSealPos().face);
                if (handler == null) {
                    continue;
                }
                for (int slot = 0; slot < handler.size(); slot++) {
                    ItemStack slotStack = ItemUtil.getStack(handler, slot);
                    if (ItemStack.isSameItemSameComponents(slotStack, stack)) {
                        GolemHelper.requestProvisioning(worldObj, player, stack.copyWithCount(Math.min(amount, slotStack.getCount())));
                        return;
                    }
                }
            }
        }
    }

    public int getStart() {
        return data.get(0);
    }

    public int getEnd() {
        return data.get(1);
    }

    public Level getLevel() {
        return worldObj;
    }

    public Player getPlayer() {
        return player;
    }
}
