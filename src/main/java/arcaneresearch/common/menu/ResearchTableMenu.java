package arcaneresearch.common.menu;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class ResearchTableMenu extends AbstractContainerMenu {

    private static final int PLAYER_INV_START = 2;
    private static final int PLAYER_INV_END = 29;
    private static final int PLAYER_HOTBAR_START = 29;
    private static final int PLAYER_HOTBAR_END = 38;

    private static final long ANTI_SPAM_WINDOW_MS = 333L;
    private static final int ANTI_SPAM_CLEANUP_THRESHOLD = 512;

    private static final Int2LongOpenHashMap antiSpam = new Int2LongOpenHashMap();

    private final TileResearchTable blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ResearchTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public ResearchTableMenu(int containerId, Inventory playerInventory, TileResearchTable blockEntity) {
        super(ModMenuTypes.RESEARCH_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        Level level = blockEntity.getLevel();
        this.access = level != null
                ? ContainerLevelAccess.create(level, blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;
        this.data = new SimpleContainerData(4);
        addDataSlots(this.data);

        addSlot(new ScribingToolsSlot(blockEntity, TileResearchTable.SLOT_SCRIBING_TOOLS, 16, 15));
        addSlot(new PaperSlot(blockEntity, TileResearchTable.SLOT_PAPER, 224, 16));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 77 + col * 18, 190 + row * 18));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                addSlot(new Slot(playerInventory, i + j * 3, 20 + i * 18, 190 + j * 18));
            }
        }
    }

    private static TileResearchTable getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileResearchTable tile) return tile;
        throw new IllegalStateException("Block entity is not a TileResearchTable: " + be);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        final int total = this.slots.size();
        if (slotIndex < 0 || slotIndex >= total) return ItemStack.EMPTY;

        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (slotIndex < 2) {
            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (stackInSlot.getItem() instanceof IScribeTools || stackInSlot.isDamageableItem()) {
            if (!moveItemStackTo(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
        } else if (stackInSlot.is(Items.PAPER)) {
            if (!moveItemStackTo(stackInSlot, 1, 2, false)) return ItemStack.EMPTY;
        } else if (slotIndex < PLAYER_INV_END) {
            if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_INV_END, false)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public void broadcastChanges() {
        ResearchTableData rtd = blockEntity != null ? blockEntity.getResearchData() : null;
        if (rtd != null) {
            data.set(0, rtd.inspiration);
            data.set(1, rtd.inspirationStart);
            data.set(2, 1);
            data.set(3, rtd.cardChoices != null ? rtd.cardChoices.size() : 0);
        } else {
            data.set(0, 0);
            data.set(1, 0);
            data.set(2, 0);
            data.set(3, 0);
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        switch (buttonId) {
            case 1 -> {
                ResearchTableData rtd = blockEntity.getResearchData();
                if (rtd == null) return false;
                if (rtd.lastDraw != null && rtd.lastDraw.card != null && rtd.savedCards != null) {
                    rtd.savedCards.add(rtd.lastDraw.card.getSeed());
                }
                if (rtd.cardChoices != null) {
                    for (ResearchTableData.CardChoice cc : rtd.cardChoices) {
                        if (cc.selected) {
                            rtd.lastDraw = cc;
                            break;
                        }
                    }
                    rtd.cardChoices.clear();
                }
                blockEntity.syncTile(false);
                blockEntity.setChanged();
                return true;
            }
            case 2, 3 -> {
                ResearchTableData rtd = blockEntity.getResearchData();
                if (rtd != null && !rtd.isComplete() && blockEntity.consumePaperFromTable()) {
                    rtd.drawCards(buttonId, player);
                    blockEntity.syncTile(false);
                    blockEntity.setChanged();
                    return true;
                }
            }
            case 4, 5, 6 -> {
                long now = System.currentTimeMillis();
                long last;
                synchronized (antiSpam) {
                    last = antiSpam.getOrDefault(player.getId(), 0L);
                    if (now - last < ANTI_SPAM_WINDOW_MS) return false;
                    antiSpam.put(player.getId(), now);
                    if (antiSpam.size() > ANTI_SPAM_CLEANUP_THRESHOLD) {
                        long threshold = now - ANTI_SPAM_WINDOW_MS * 10L;
                        antiSpam.int2LongEntrySet().removeIf(e -> e.getLongValue() < threshold);
                    }
                }

                ResearchTableData rtd = blockEntity.getResearchData();
                if (rtd == null || rtd.cardChoices == null) return false;
                int cardIndex = buttonId - 4;
                if (cardIndex >= rtd.cardChoices.size()) return false;

                try {
                    TheorycraftCard card = rtd.cardChoices.get(cardIndex).card;
                    if (card == null) return false;

                    ItemStack[] required = card.getRequiredItems();
                    if (required != null) {
                        for (ItemStack stack : required) {
                            if (stack != null && !stack.isEmpty()
                                    && !InventoryUtils.isPlayerCarryingAmount(player, stack, true)) {
                                return false;
                            }
                        }
                        boolean[] consumed = card.getRequiredItemsConsumed();
                        if (consumed != null && consumed.length == required.length) {
                            for (int a = 0; a < required.length; a++) {
                                if (consumed[a] && required[a] != null && !required[a].isEmpty()) {
                                    InventoryUtils.consumePlayerItem(player, required[a], true, true);
                                }
                            }
                        }
                    }

                    if (card.activate(player, rtd)) {
                        blockEntity.consumeInkFromTable();
                        rtd.cardChoices.get(cardIndex).selected = true;
                        rtd.addInspiration(-card.getInspirationCost());
                        blockEntity.syncTile(false);
                        blockEntity.setChanged();
                        return true;
                    }
                } catch (Exception e) {
                    Arcaneresearch.LOGGER.error("Error activating card {} for player {}",
                            cardId(cardIndex), player.getGameProfile().getName(), e);
                }
            }
            case 7 -> {
                ResearchTableData rtd = blockEntity.getResearchData();
                if (rtd != null && rtd.isComplete()) {
                    blockEntity.finishTheory(player);
                    return true;
                }
            }
            case 9 -> {
                ResearchTableData rtd = blockEntity.getResearchData();
                if (rtd != null && !rtd.isComplete()) {
                    blockEntity.data = null;
                    blockEntity.syncTile(false);
                    blockEntity.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    private static String cardId(int index) {
        return "index=" + index;
    }

    public TileResearchTable getBlockEntity() {
        return blockEntity;
    }

    public int getInspiration() {
        return data.get(0);
    }

    public int getInspirationStart() {
        return data.get(1);
    }

    public boolean hasActiveTheory() {
        return data.get(2) == 1;
    }

    public int getCardChoiceCount() {
        return data.get(3);
    }

    public boolean hasScribingTools() {
        ItemStack tools = blockEntity.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
        return !tools.isEmpty() && tools.getItem() instanceof IScribeTools;
    }

    public boolean hasPaper() {
        ItemStack paper = blockEntity.getItem(TileResearchTable.SLOT_PAPER);
        return !paper.isEmpty() && paper.is(Items.PAPER);
    }

    private static class ScribingToolsSlot extends Slot {
        public ScribingToolsSlot(TileResearchTable container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof IScribeTools;
        }
    }

    private static class PaperSlot extends Slot {
        public PaperSlot(TileResearchTable container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.is(Items.PAPER);
        }
    }
}