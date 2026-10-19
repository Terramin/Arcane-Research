package arcaneresearch.common.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftCard;
import arcaneresearch.common.container.slot.SlotLimitedByClass;
import arcaneresearch.common.container.slot.SlotLimitedByItemstack;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;

import java.util.HashMap;

@Deprecated
public class ContainerResearchTable extends AbstractContainerMenu {
    static HashMap<Integer, Long> antiSpam = new HashMap<Integer, Long>();
    private final ContainerLevelAccess access;
    private final ContainerData data;
    public TileResearchTable tileEntity;
    String[] aspects;
    Player player;

    public ContainerResearchTable(Inventory playerInventory, TileResearchTable tile) {
        super(ModMenuTypes.RESEARCH_TABLE.get(), containerIdOf(playerInventory));
        player = playerInventory.player;
        tileEntity = tile;
        aspects = Aspect.aspects.keySet().toArray(new String[0]);
        access = ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos());
        data = new SimpleContainerData(4);
        addDataSlots(data);
        addSlot(new SlotLimitedByClass(IScribeTools.class, tileEntity, 0, 16, 15));
        addSlot(new SlotLimitedByItemstack(new ItemStack(Items.PAPER), tileEntity, 1, 224, 16));
        bindPlayerInventory(playerInventory);
    }

    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }

    protected void bindPlayerInventory(Inventory inventoryPlayer) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 77 + j * 18, 190 + i * 18));
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                addSlot(new Slot(inventoryPlayer, i + j * 3, 20 + i * 18, 190 + j * 18));
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        switch (buttonId) {
            case 1 -> {
                ResearchTableData data = tileEntity.getResearchData();
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
                    tileEntity.syncTile(false);
                    tileEntity.setChanged();
                    return true;
                }
            }
            case 2, 3 -> {
                ResearchTableData data = tileEntity.getResearchData();
                if (data != null && !data.isComplete() && tileEntity.consumePaperFromTable()) {
                    data.drawCards(buttonId, player);
                    tileEntity.syncTile(false);
                    tileEntity.setChanged();
                }
                return true;
            }
            case 4, 5, 6 -> {
                long now = System.currentTimeMillis();
                long last = antiSpam.getOrDefault(player.getId(), 0L);
                if (now - last < 333L) {
                    return false;
                }
                antiSpam.put(player.getId(), now);
                ResearchTableData data = tileEntity.getResearchData();
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
                        tileEntity.consumeInkFromTable();
                        data.cardChoices.get(cardIndex).selected = true;
                        data.addInspiration(-card.getInspirationCost());
                        tileEntity.syncTile(false);
                        tileEntity.setChanged();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case 7 -> {
                ResearchTableData data = tileEntity.getResearchData();
                if (data != null && data.isComplete()) {
                    tileEntity.finishTheory(player);
                    tileEntity.syncTile(false);
                    return true;
                }
            }
            case 9 -> {
                ResearchTableData data = tileEntity.getResearchData();
                if (data != null && !data.isComplete()) {
                    tileEntity.data = null;
                    tileEntity.syncTile(false);
                    tileEntity.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (slotIndex < 2) {
                if (!tileEntity.canPlaceItem(slotIndex, stackInSlot) || !moveItemStackTo(stackInSlot, 2, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!tileEntity.canPlaceItem(slotIndex, stackInSlot) || !moveItemStackTo(stackInSlot, 0, 2, false)) {
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
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        ResearchTableData rtd = tileEntity.getResearchData();
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

    public TileResearchTable getBlockEntity() {
        return tileEntity;
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
        ItemStack tools = tileEntity.getItem(TileResearchTable.SLOT_SCRIBING_TOOLS);
        return !tools.isEmpty() && tools.getItem() instanceof IScribeTools;
    }

    public boolean hasPaper() {
        ItemStack paper = tileEntity.getItem(TileResearchTable.SLOT_PAPER);
        return !paper.isEmpty() && paper.is(Items.PAPER);
    }
}
