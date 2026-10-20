package arcaneresearch.common.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.menu.slot.GhostSlot;
import arcaneresearch.init.ModMenuTypes;

public class SealMenu extends AbstractContainerMenu {

    private static final int DATA_PRIORITY = 0;
    private static final int DATA_AREA_X = 1;
    private static final int DATA_AREA_Y = 2;
    private static final int DATA_AREA_Z = 3;
    private static final int DATA_COLOR = 4;
    private static final int DATA_LOCKED = 5;
    private static final int DATA_REDSTONE = 6;
    private static final int DATA_BLACKLIST = 7;
    private final ISealEntity seal;
    private final Player player;
    private final ContainerData data;
    private Container filterContainer;
    private int currentCategory;
    private int[] availableCategories;

    public SealMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, readSealFromBuffer(playerInventory.player, extraData));
    }

    public SealMenu(int containerId, Inventory playerInventory, ISealEntity seal) {
        super(ModMenuTypes.SEAL.get(), containerId);
        this.seal = seal;
        this.player = playerInventory.player;

        this.data = new SimpleContainerData(8);
        addDataSlots(this.data);

        if (seal.getSeal() instanceof ISealGui sealGui) {
            this.availableCategories = sealGui.getGuiCategories();
        } else {
            this.availableCategories = new int[]{ISealGui.CAT_PRIORITY};
        }

        this.currentCategory = availableCategories.length > 0 ? availableCategories[0] : 0;

        setupSlotsForCategory();

        addPlayerInventory(playerInventory);
    }

    private static ISealEntity readSealFromBuffer(Player player, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        Direction face = Direction.values()[buffer.readByte()];
        SealPos sealPos = new SealPos(pos, face);
        ISealEntity seal = SealHandler.getSealEntity(player.level().dimension(), sealPos);
        if (seal == null) {

            Arcaneresearch.LOGGER.warn("Seal not found on client at {}, face {}", pos, face);
            return new SealEntity(player.level(), sealPos, null);
        }
        return seal;
    }

    public static void applyGhostSlotClick(ISealEntity seal, Slot slot, ItemStack carried, int button, ContainerInput mode) {
        ISealConfigFilter filter = (ISealConfigFilter) seal.getSeal();
        ItemStack ic = carried.isEmpty() ? ItemStack.EMPTY : carried.copy();
        boolean sizeLimiters = filter.hasStacksizeLimiters();
        if (button == 1) {
            if (!sizeLimiters) {
                slot.set(ItemStack.EMPTY);
                filter.setFilterSlotSize(slot.index, 0);
            } else if (ic.isEmpty()) {
                if (slot.hasItem()) {
                    filter.setFilterSlotSize(slot.index,
                            filter.getFilterSlotSize(slot.index) - (mode == ContainerInput.QUICK_MOVE ? 10 : 1));
                    if (filter.getFilterSlotSize(slot.index) < 0) {
                        slot.set(ItemStack.EMPTY);
                        filter.setFilterSlotSize(slot.index, 0);
                    }
                }
            } else if (slot.hasItem() && slot.getItem().getCount() == 0) {
                slot.set(ItemStack.EMPTY);
                filter.setFilterSlotSize(slot.index, 0);
            } else if (slot.hasItem() && ItemStack.isSameItemSameComponents(ic, slot.getItem())) {
                filter.setFilterSlotSize(slot.index, filter.getFilterSlotSize(slot.index) - ic.getCount());
                if (filter.getFilterSlotSize(slot.index) < 0) {
                    slot.set(ItemStack.EMPTY);
                    filter.setFilterSlotSize(slot.index, 0);
                }
            }
        } else if (ic.isEmpty()) {
            if (sizeLimiters && slot.hasItem()) {
                filter.setFilterSlotSize(slot.index,
                        filter.getFilterSlotSize(slot.index) + (mode == ContainerInput.QUICK_MOVE ? 10 : 1));
            }
        } else {
            if (!sizeLimiters) {
                ic.setCount(1);
                filter.setFilterSlotSize(slot.index, 0);
            } else {
                int os = ic.getCount();
                ic.setCount(1);
                if (slot.hasItem() && ItemStack.isSameItemSameComponents(ic, slot.getItem())) {
                    filter.setFilterSlotSize(slot.index, filter.getFilterSlotSize(slot.index) + os);
                } else {
                    filter.setFilterSlotSize(slot.index, 0);
                }
            }
            slot.set(ic);
        }
        if (slot.hasItem() && slot.getItem().getCount() < 0) {
            slot.getItem().setCount(0);
        }
    }

    private void setupSlotsForCategory() {

        if (seal.getSeal() instanceof ISealConfigFilter filter) {
            int filterSize = filter.getFilterSize();
            NonNullList<ItemStack> filterInv = filter.getInv();

            filterContainer = new SimpleContainer(filterSize) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    for (int i = 0; i < getContainerSize(); i++) {
                        filter.setFilterSlot(i, getItem(i));
                    }
                }
            };

            for (int i = 0; i < filterSize; i++) {
                ((SimpleContainer) filterContainer).setItem(i, filterInv.get(i).copy());
            }

            int s = filterSize;
            int sx = 16 + (s - 1) % 3 * 12;
            int sy = 16 + (s - 1) / 3 * 12;
            int middleX = 88;
            int middleY = 72;

            for (int i = 0; i < filterSize; i++) {
                int col = i % 3;
                int row = i / 3;
                addSlot(new GhostSlot(filterContainer, i, middleX + col * 24 - sx + 8, middleY + row * 24 - sy + 8,
                        Integer.MAX_VALUE, () -> currentCategory == ISealGui.CAT_FILTER));
            }
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 150 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 208));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        data.set(DATA_PRIORITY, seal.getPriority());
        data.set(DATA_AREA_X, seal.getArea().getX());
        data.set(DATA_AREA_Y, seal.getArea().getY());
        data.set(DATA_AREA_Z, seal.getArea().getZ());
        data.set(DATA_COLOR, seal.getColor());
        data.set(DATA_LOCKED, seal.isLocked() ? 1 : 0);
        data.set(DATA_REDSTONE, seal.isRedstoneSensitive() ? 1 : 0);

        if (seal.getSeal() instanceof ISealConfigFilter filter) {
            data.set(DATA_BLACKLIST, filter.isBlacklist() ? 1 : 0);
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        boolean result = handleButton(buttonId);
        if (result && !player.level().isClientSide()) {
            syncSeal(player);
        }
        return result;
    }

    private boolean handleButton(int buttonId) {

        if (buttonId >= 0 && buttonId < availableCategories.length) {
            setCurrentCategory(availableCategories[buttonId]);
            return true;
        }

        if (currentCategory == ISealGui.CAT_TOGGLES && seal.getSeal() instanceof ISealConfigToggles toggles) {
            ISealConfigToggles.SealToggle[] sealToggles = toggles.getToggles();
            if (buttonId >= 30 && buttonId < 30 + sealToggles.length) {
                toggles.setToggle(buttonId - 30, true);
                return true;
            }
            if (buttonId >= 60 && buttonId < 60 + sealToggles.length) {
                toggles.setToggle(buttonId - 60, false);
                return true;
            }
        }

        if (currentCategory == ISealGui.CAT_PRIORITY && (buttonId == 25 || buttonId == 26)) {
            seal.setLocked(buttonId == 25);
            return true;
        }

        if (buttonId == 27) {
            seal.setRedstoneSensitive(true);
            return true;
        }
        if (buttonId == 28) {
            seal.setRedstoneSensitive(false);
            return true;
        }

        if (currentCategory == ISealGui.CAT_FILTER && seal.getSeal() instanceof ISealConfigFilter filter) {
            if (buttonId == 20) {
                filter.setBlacklist(true);
                return true;
            }
            if (buttonId == 21) {
                filter.setBlacklist(false);
                return true;
            }
        }

        if (buttonId == 80 && seal.getPriority() > -5) {
            seal.setPriority((byte) (seal.getPriority() - 1));
            return true;
        }
        if (buttonId == 81 && seal.getPriority() < 5) {
            seal.setPriority((byte) (seal.getPriority() + 1));
            return true;
        }

        if (buttonId == 82 && seal.getColor() > 0) {
            seal.setColor((byte) (seal.getColor() - 1));
            return true;
        }
        if (buttonId == 83 && seal.getColor() < 16) {
            seal.setColor((byte) (seal.getColor() + 1));
            return true;
        }

        if (seal.getSeal() instanceof ISealConfigArea) {
            BlockPos area = seal.getArea();
            switch (buttonId) {
                case 90 -> {
                    if (area.getY() > 1) seal.setArea(area.offset(0, -1, 0));
                    return true;
                }
                case 91 -> {
                    if (area.getY() < 8) seal.setArea(area.offset(0, 1, 0));
                    return true;
                }
                case 92 -> {
                    if (area.getX() > 1) seal.setArea(area.offset(-1, 0, 0));
                    return true;
                }
                case 93 -> {
                    if (area.getX() < 8) seal.setArea(area.offset(1, 0, 0));
                    return true;
                }
                case 94 -> {
                    if (area.getZ() > 1) seal.setArea(area.offset(0, 0, -1));
                    return true;
                }
                case 95 -> {
                    if (area.getZ() < 8) seal.setArea(area.offset(0, 0, 1));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {

        if (slotId >= 0 && slotId < slots.size()
                && currentCategory == arcaneresearch.api.golems.seals.ISealGui.CAT_FILTER) {
            Slot slot = slots.get(slotId);
            if (slot instanceof GhostSlot && seal.getSeal() instanceof ISealConfigFilter) {
                if (!player.level().isClientSide()) {
                    handleGhostSlotClick(slot, button, clickType, player);
                }
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleGhostSlotClick(Slot slot, int button, ContainerInput mode, Player player) {
        applyGhostSlotClick(seal, slot, getCarried(), button, mode);
        broadcastChanges();
        syncSeal(player);
    }

    // NEOFORGE_PORT (Forge 1:1): broadcastChanges syncs menu DataSlots only;
    // the SealEntity itself must be pushed to tracking clients + chunk dirtied.
    private void syncSeal(Player player) {
        net.minecraft.world.level.Level level = player.level();
        seal.syncToClient(level);
        SealHandler.markChunkAsDirty(level.dimension(), seal.getSealPos().pos);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {

        return ItemStack.EMPTY;
    }

    public ISealEntity getSeal() {
        return seal;
    }

    public int getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(int category) {
        this.currentCategory = category;
    }

    public int[] getAvailableCategories() {
        return availableCategories;
    }

    public int getPriority() {
        return data.get(DATA_PRIORITY);
    }

    public BlockPos getArea() {
        return new BlockPos(data.get(DATA_AREA_X), data.get(DATA_AREA_Y), data.get(DATA_AREA_Z));
    }

    public int getColor() {
        return data.get(DATA_COLOR);
    }

    public boolean isLocked() {
        return data.get(DATA_LOCKED) != 0;
    }

    public boolean isRedstoneSensitive() {
        return data.get(DATA_REDSTONE) != 0;
    }

    public boolean isBlacklist() {
        return data.get(DATA_BLACKLIST) != 0;
    }
}
