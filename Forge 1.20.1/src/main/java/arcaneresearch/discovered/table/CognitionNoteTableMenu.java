package arcaneresearch.discovered.table;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
public class CognitionNoteTableMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 18;
    public static final int BUTTON_RESEARCH = 0;
    public static final int BUTTON_TOGGLE_SAFE = 1;
    private final CognitionNoteTableTile tile;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    public CognitionNoteTableMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, getTile(playerInventory, buf));
    }
    public CognitionNoteTableMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, getTile(playerInventory, pos));
    }
    public CognitionNoteTableMenu(int id, Inventory playerInventory, CognitionNoteTableTile tile) {
        super(DiscoveredTableSetup.getMenuType(), id);
        this.tile = tile;
        this.access = ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos());
        this.data = new SimpleContainerData(DATA_COUNT);
        addDataSlots(data);
        try {
            if (tile.getLevel() != null && !tile.getLevel().isClientSide) {
                tile.recalcIfNeeded();
            }
        } catch (Exception ignored) {
        }
        for (int i = 0; i < CognitionNoteTableTile.INPUT_COUNT; i++) {
            addSlot(new Slot(tile, i, 15, 16 + i * 24));
        }
        addSlot(new DiscoveredNoteSlot(tile, CognitionNoteTableTile.NOTE_SLOT, 75, 65, playerInventory.player));
        addSlot(new DiscoveredPaperSlot(tile, CognitionNoteTableTile.PAPER_SLOT, 75, 97));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 40 + col * 18, 160 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 40 + col * 18, 218));
        }
    }
    private static CognitionNoteTableTile getTile(Inventory playerInventory, FriendlyByteBuf buf) {
        return getTile(playerInventory, buf.readBlockPos());
    }
    private static CognitionNoteTableTile getTile(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof CognitionNoteTableTile tile) {
            return tile;
        }
        throw new IllegalStateException("No CognitionNoteTableTile at " + pos);
    }
    public Aspect getTagAspect(int i) {
        return DiscoveredTableResearch.aspectByIndex(data.get(i));
    }
    public int getTagAmount(int i) {
        return data.get(5 + i);
    }
    public float getTagBonus(int i) {
        return data.get(10 + i) / 100.0F;
    }
    public boolean isSafe() {
        return data.get(15) != 0;
    }
    public int getChance() {
        return data.get(16);
    }
    public ItemStack getNoteStack() {
        return getSlot(CognitionNoteTableTile.NOTE_SLOT).getItem();
    }
    public CognitionNoteTableTile getTable() {
        return tile;
    }
    @Override
    public void broadcastChanges() {
        if (tile.getLevel() != null && !tile.getLevel().isClientSide) {
            tile.recalcIfNeeded();
            for (int i = 0; i < CognitionNoteTableTile.INPUT_COUNT; i++) {
                data.set(i, DiscoveredTableResearch.aspectIndex(tile.getTag(i)));
                data.set(5 + i, tile.getTagAmount(i));
                data.set(10 + i, Math.round(tile.getTagBonus(i) * 100.0F));
            }
            data.set(15, tile.isSafe() ? 1 : 0);
            data.set(16, tile.getBaseChance());
            data.set(17, tile.getBaseLoss());
        }
        super.broadcastChanges();
    }
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && tile.canResearch()) {
            tile.startResearch(player);
            return true;
        }
        if (id == 1) {
            tile.toggleSafe();
            return true;
        }
        return false;
    }
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, DiscoveredTableSetup.getBlock());
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack inSlot = slot.getItem();
            result = inSlot.copy();
            if (index < CognitionNoteTableTile.SIZE) {
                if (!moveItemStackTo(inSlot, CognitionNoteTableTile.SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (inSlot.getItem() instanceof ItemDiscoveredResearchNote) {
                    if (!moveItemStackTo(inSlot, CognitionNoteTableTile.NOTE_SLOT,
                            CognitionNoteTableTile.NOTE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (inSlot.is(Items.PAPER)) {
                    if (!moveItemStackTo(inSlot, CognitionNoteTableTile.PAPER_SLOT,
                            CognitionNoteTableTile.PAPER_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(inSlot, 0, CognitionNoteTableTile.INPUT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }
}
