package arcaneresearch.common.menu;

import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.seals.SealProvide;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketItemToClientContainer;
import arcaneresearch.common.menu.slot.GhostSlot;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogisticsMenu extends AbstractContainerMenu {

    private static final int DATA_START = 0;
    private static final int DATA_END = 1;
    private static final int PAGE_ROWS = 9;
    private static final int PAGE_COLS = 9;
    private static final int PAGE_SIZE = PAGE_ROWS * PAGE_COLS;

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
                readSafeDirection(extraData),
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

        this.displayContainer = new SimpleContainer(PAGE_SIZE) {
            @Override
            public int getMaxStackSize() {
                return Integer.MAX_VALUE;
            }
        };

        this.data = new SimpleContainerData(2);
        addDataSlots(data);

        for (int row = 0; row < PAGE_ROWS; row++) {
            for (int col = 0; col < PAGE_COLS; col++) {
                int slotIndex = row * PAGE_COLS + col;
                addSlot(new GhostSlot(displayContainer, slotIndex, 19 + col * 19, 19 + row * 19,
                        Integer.MAX_VALUE, () -> true, false));
            }
        }

        refreshItemList(true);
    }

    private static Direction readSafeDirection(FriendlyByteBuf buf) {
        if (!buf.isReadable()) return Direction.UP;
        int idx = buf.readByte() & 0xFF;
        Direction[] dirs = Direction.values();
        return idx < dirs.length ? dirs[idx] : Direction.UP;
    }

    public void refreshItemList(boolean full) {
        int newTotal = lastTotal;
        TreeMap<String, ItemStack> tempItems = null;

        if (full) {
            newTotal = 0;
            tempItems = new TreeMap<>();
            CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(level, player.blockPosition(), 32);
            String needle = searchText.isEmpty() ? null : searchText.toLowerCase(Locale.ROOT);
            String ownerUuid = player.getUUID().toString();

            for (SealEntity seal : seals) {
                if (!(seal.getSeal() instanceof SealProvide sealProvide)) continue;
                if (seal.getOwner() == null || !ownerUuid.equals(seal.getOwner())) continue;
                if (seal.getSealPos() == null) continue;

                IItemHandler handler = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
                if (handler == null) continue;

                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;
                    if (!sealProvide.matchesFilters(stack)) continue;
                    if (needle != null
                            && !stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)) {
                        continue;
                    }

                    stack = stack.copy();
                    String key = stack.getHoverName().getString()
                            + "_" + stack.getDamageValue()
                            + "_" + (stack.getTag() != null ? stack.getTag().hashCode() : 0);

                    ItemStack existing = tempItems.get(key);
                    if (existing != null) {
                        existing.grow(stack.getCount());
                    } else {
                        tempItems.put(key, stack);
                    }
                    newTotal += stack.getCount();
                }
            }
        }

        if (!full && start == lastStart) {
            return;
        }

        if (full) {
            items = tempItems;
            lastTotal = newTotal;
        }

        for (int i = 0; i < displayContainer.getContainerSize(); i++) {
            displayContainer.setItem(i, ItemStack.EMPTY);
        }

        int itemIndex = 0;
        int displayIndex = 0;
        for (String key : items.keySet()) {
            itemIndex++;
            if (itemIndex <= start * PAGE_COLS) continue;
            displayContainer.setItem(displayIndex, items.get(key).copy());
            displayIndex++;
            if (displayIndex >= displayContainer.getContainerSize()) break;
        }

        end = Math.max(0, (Math.max(0, items.size() - PAGE_SIZE) + PAGE_COLS - 1) / PAGE_COLS);
    }

    private IItemHandler getItemHandler(Level level, BlockPos pos, Direction face) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return null;
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, face).orElse(null);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        switch (buttonId) {
            case 0 -> {
                if (start < end) {
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
                    if (targetScroll <= end) {
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
        if (!level.isClientSide) {
            data.set(DATA_START, start);
            data.set(DATA_END, end);
            lastStart = start;
            lastEnd = end;
        }
        sendLargeSlotsToClient();
        super.broadcastChanges();
    }

    private void sendLargeSlotsToClient() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        for (Slot slot : slots) {
            if (!(slot instanceof GhostSlot)) continue;
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()) {
                PacketHandler.sendToPlayer(
                        new PacketItemToClientContainer(containerId, slot.index, stack),
                        serverPlayer);
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public void setSearchText(String text) {
        this.searchText = text == null ? "" : text;
        this.start = 0;
        refreshItemList(true);
    }

    public void requestItem(ItemStack stack, int amount) {
        if (stack == null || stack.isEmpty() || amount <= 0) return;

        CopyOnWriteArrayList<SealEntity> seals = SealHandler.getSealsInRange(level, player.blockPosition(), 32);
        String ownerUuid = player.getUUID().toString();

        for (SealEntity seal : seals) {
            if (!(seal.getSeal() instanceof SealProvide)) continue;
            if (seal.getOwner() == null || !ownerUuid.equals(seal.getOwner())) continue;
            if (seal.getSealPos() == null) continue;

            IItemHandler handler = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
            if (handler == null) continue;

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack slotStack = handler.getStackInSlot(slot);
                if (slotStack.isEmpty()) continue;
                if (ItemStack.isSameItemSameTags(slotStack, stack)) {
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