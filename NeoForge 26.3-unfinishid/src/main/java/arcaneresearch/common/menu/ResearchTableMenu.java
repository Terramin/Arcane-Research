package arcaneresearch.common.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;

import java.util.HashMap;
import java.util.Map;

public class ResearchTableMenu extends AbstractContainerMenu {

    private static final Map<Integer, Long> antiSpam = new HashMap<>();
    private final TileResearchTable blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ResearchTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public ResearchTableMenu(int containerId, Inventory playerInventory, TileResearchTable blockEntity) {
        super(ModMenuTypes.RESEARCH_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

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
        if (be instanceof TileResearchTable tile) {
            return tile;
        }
        throw new IllegalStateException("Block entity is not a TileResearchTable");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (slotIndex < 2) {
                if (!moveItemStackTo(stackInSlot, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if (stackInSlot.getItem() instanceof IScribeTools || stackInSlot.isDamageableItem()) {
                    if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.is(Items.PAPER)) {
                    if (!moveItemStackTo(stackInSlot, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotIndex < 29) {
                    if (!moveItemStackTo(stackInSlot, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!moveItemStackTo(stackInSlot, 2, 29, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        ResearchTableData rtd = blockEntity.getResearchData();
        if (rtd != null) {
            data.set(0, rtd.inspiration);
            data.set(1, rtd.inspirationStart);
            data.set(2, 1);
            data.set(3, rtd.cardChoices.size());
        } else {
            data.set(0, 0);
            data.set(1, 0);
            data.set(2, 0);
            data.set(3, 0);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case 1 -> {

                ResearchTableData data = blockEntity.getResearchData();
                if (data != null) {
                    if (data.lastDraw != null) {
                        data.savedCards.add(data.lastDraw.card.getSeed());
                    }
                    for (ResearchTableData.CardChoice cc : data.cardChoices) {
                        if (cc.selected) {
                            data.lastDraw = cc;
                            break;
                        }
                    }
                    data.cardChoices.clear();
                    blockEntity.syncTile(false);
                    blockEntity.setChanged();
                    return true;
                }
            }
            case 2, 3 -> {

                ResearchTableData data = blockEntity.getResearchData();
                if (data != null && !data.isComplete() && blockEntity.consumePaperFromTable()) {
                    data.drawCards(buttonId, player);
                    blockEntity.syncTile(false);
                    blockEntity.setChanged();
                    return true;
                }
            }
            case 4, 5, 6 -> {

                long now = System.currentTimeMillis();
                long last = antiSpam.getOrDefault(player.getId(), 0L);
                if (now - last < 333L) {
                    return false;
                }
                antiSpam.put(player.getId(), now);

                ResearchTableData data = blockEntity.getResearchData();
                if (data == null) {
                    return false;
                }
                int cardIndex = buttonId - 4;
                if (cardIndex >= data.cardChoices.size()) {
                    return false;
                }
                try {
                    TheorycraftCard card = data.cardChoices.get(cardIndex).card;

                    if (card.getRequiredItems() != null) {
                        for (ItemStack stack : card.getRequiredItems()) {
                            if (stack != null && !stack.isEmpty()
                                    && !InventoryUtils.isPlayerCarryingAmount(player, stack, true)) {
                                return false;
                            }
                        }

                        if (card.getRequiredItemsConsumed() != null
                                && card.getRequiredItemsConsumed().length == card.getRequiredItems().length) {
                            for (int a = 0; a < card.getRequiredItems().length; a++) {
                                if (card.getRequiredItemsConsumed()[a]
                                        && card.getRequiredItems()[a] != null
                                        && !card.getRequiredItems()[a].isEmpty()) {
                                    InventoryUtils.consumePlayerItem(player, card.getRequiredItems()[a], true, true);
                                }
                            }
                        }
                    }

                    if (card.activate(player, data)) {
                        blockEntity.consumeInkFromTable();
                        data.cardChoices.get(cardIndex).selected = true;
                        data.addInspiration(-card.getInspirationCost());
                        blockEntity.syncTile(false);
                        blockEntity.setChanged();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case 7 -> {

                ResearchTableData data = blockEntity.getResearchData();
                if (data != null && data.isComplete()) {
                    blockEntity.finishTheory(player);
                    return true;
                }
            }
            case 9 -> {

                ResearchTableData data = blockEntity.getResearchData();
                if (data != null && !data.isComplete()) {
                    blockEntity.data = null;
                    blockEntity.syncTile(false);
                    blockEntity.setChanged();
                    return true;
                }
            }
        }
        return false;
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
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof IScribeTools;
        }
    }

    private static class PaperSlot extends Slot {
        public PaperSlot(TileResearchTable container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(Items.PAPER);
        }
    }
}
