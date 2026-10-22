package arcaneresearch.common.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.seals.SealProvide;
import arcaneresearch.common.menu.slot.GhostSlot;
import arcaneresearch.init.ModMenuTypes;

import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogisticsMenu extends AbstractContainerMenu {

    private static final int DATA_START = 0;
    private static final int DATA_END = 1;
    private final Level level;
    private final Player player;
    private final BlockPos targetPos;
    private final Direction targetSide;
    private final boolean deliverToPlayer;
    private final Container displayContainer;
    private final ContainerData data;
    private TreeMap<String, ItemStack> items = new TreeMap<>();
    private int start = 0;
    private int end = 0;
    private int lastStart = 0;
    private int lastEnd = 0;
    private int lastTotal = 0;
    private String searchText = "";

    public LogisticsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                extraData.isReadable() ? extraData.readBlockPos() : playerInventory.player.blockPosition(),
                extraData.isReadable() ? Direction.values()[extraData.readByte()] : Direction.UP,
                extraData.isReadable() && extraData.readBoolean());
    }

    public LogisticsMenu(int containerId, Inventory playerInventory, BlockPos pos, Direction side) {
        this(containerId, playerInventory, pos, side, false);
    }

    public LogisticsMenu(int containerId, Inventory playerInventory, BlockPos pos, Direction side, boolean deliverToPlayer) {
        super(ModMenuTypes.LOGISTICS.get(), containerId);
        this.level = playerInventory.player.level();
        this.player = playerInventory.player;
        this.targetPos = pos;
        this.targetSide = side;
        this.deliverToPlayer = deliverToPlayer;

        this.displayContainer = new SimpleContainer(81) {
            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE;
            }

            
            
            
            @Override
            public int getMaxStackSize(ItemStack stack) {
                return getMaxStackSize();
            }
        };

        this.data = new SimpleContainerData(2);
        addDataSlots(data);

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col;
                addSlot(new GhostSlot(displayContainer, slotIndex, 19 + col * 19, 19 + row * 19,
                        Integer.MAX_VALUE, () -> true, false));
            }
        }

        refreshItemList(true);
    }

    public void refreshItemList(boolean full) {
        int newTotal = lastTotal;
        TreeMap<String, ItemStack> tempItems = new TreeMap<>();

        if (full) {
            newTotal = 0;
            CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(level, player.blockPosition(), 32);

            for (SealEntity seal : seals) {
                if (seal.getSeal() instanceof SealProvide sealProvide &&
                        seal.getOwner().equals(player.getUUID().toString())) {

                    ResourceHandler<ItemResource> handler = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
                    if (handler == null) continue;

                    for (int slot = 0; slot < handler.size(); slot++) {
                        
                        ItemStack stack = ItemUtil.getStack(handler, slot).copy();
                        if (stack.isEmpty()) continue;

                        if (sealProvide.matchesFilters(stack)) {

                            if (!searchText.isEmpty() &&
                                    !stack.getHoverName().getString().toLowerCase().contains(searchText.toLowerCase())) {
                                continue;
                            }

                            
                            
                            
                            
                            String key = stack.getHoverName().getString() +
                                    "_" + stack.getDamageValue() +
                                    "_" + (!stack.isComponentsPatchEmpty() ? stack.getComponentsPatch().hashCode() : 0);

                            if (tempItems.containsKey(key)) {
                                ItemStack existing = tempItems.get(key);
                                existing.grow(stack.getCount());
                            } else {
                                tempItems.put(key, stack);
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
                items = tempItems;
            }

            for (int i = 0; i < displayContainer.getContainerSize(); i++) {
                displayContainer.setItem(i, ItemStack.EMPTY);
            }

            int itemIndex = 0;
            int displayIndex = 0;
            for (String key : items.keySet()) {
                itemIndex++;
                if (itemIndex <= start * 9) continue;

                displayContainer.setItem(displayIndex, items.get(key).copy());
                displayIndex++;
                if (displayIndex >= displayContainer.getContainerSize()) break;
            }

            end = Math.max(0, items.size() / 9 - 8);
        }
    }

    private ResourceHandler<ItemResource> getItemHandler(Level level, BlockPos pos, Direction face) {
        
        
        return arcaneresearch.api.ArcaneresearchInvHelper.getItemHandlerAt(level, pos, face);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case 0 -> {

                if (start < items.size() / 9 - 8) {
                    start++;
                    refreshItemList(false);
                }
                return true;
            }
            case 1 -> {

                if (start > 0) {
                    start--;
                    refreshItemList(false);
                }
                return true;
            }
            case 22 -> {

                refreshItemList(true);
                return true;
            }
            default -> {

                if (buttonId >= 100) {
                    int targetScroll = buttonId - 100;
                    if (targetScroll >= 0 && targetScroll <= Math.max(0, items.size() / 9 - 8)) {
                        start = targetScroll;
                        refreshItemList(false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void broadcastChanges() {
        sendLargeSlotsToClient();
        super.broadcastChanges();

        data.set(DATA_START, start);
        data.set(DATA_END, end);

        lastStart = start;
        lastEnd = end;
    }

    
    
    private void sendLargeSlotsToClient() {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        for (net.minecraft.world.inventory.Slot slot : slots) {
            if (slot instanceof GhostSlot) {
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && itemstack.getCount() > itemstack.getMaxStackSize()) {
                    arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                            new arcaneresearch.common.lib.network.misc.PacketItemToClientContainer(
                                    containerId, slot.index, itemstack),
                            serverPlayer);
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public void setSearchText(String text) {
        this.searchText = text;
        this.start = 0;
        refreshItemList(true);
    }

    public void requestItem(ItemStack stack, int amount) {
        if (stack.isEmpty() || amount <= 0) return;

        CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(level, player.blockPosition(), 32);

        for (SealEntity seal : seals) {
            if (seal.getSeal() instanceof SealProvide sealProvide &&
                    seal.getOwner().equals(player.getUUID().toString())) {

                ResourceHandler<ItemResource> handler = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
                if (handler == null) continue;

                for (int slot = 0; slot < handler.size(); slot++) {
                    ItemStack slotStack = ItemUtil.getStack(handler, slot);
                    if (ItemStack.isSameItemSameComponents(slotStack, stack)) {

                        GolemHelper.requestProvisioning(
                                level,
                                player,
                                stack.copyWithCount(Math.min(amount, slotStack.getCount()))
                        );
                        return;
                    }
                }
            }
        }
    }

    public int getStart() {
        return data.get(DATA_START);
    }

    public int getEnd() {
        return data.get(DATA_END);
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public Direction getTargetSide() {
        return targetSide;
    }

    public boolean isDeliverToPlayer() {
        return deliverToPlayer;
    }
}
