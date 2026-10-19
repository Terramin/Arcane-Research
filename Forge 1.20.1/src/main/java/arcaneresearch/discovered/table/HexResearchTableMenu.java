package arcaneresearch.discovered.table;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.common.items.tools.ItemScribingTools;
public class HexResearchTableMenu extends AbstractContainerMenu {
    private final Container tableInv;
    private final boolean owned;
    private final BlockPos tilePos;
    public HexResearchTableMenu(MenuType<?> type, int id, Inventory playerInventory,
            HexNoteTableTile tile) {
        this(type, id, playerInventory, tile, false,
                tile == null ? null : tile.getBlockPos());
    }
    public HexResearchTableMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(HexTableRegistry.MENU, id, playerInventory, new ClientInit(playerInventory, buf));
    }
    private HexResearchTableMenu(MenuType<?> type, int id, Inventory playerInventory,
            ClientInit init) {
        this(type, id, playerInventory, init.container,
                init.container instanceof SimpleContainer, init.pos);
    }
    private HexResearchTableMenu(MenuType<?> type, int id, Inventory playerInventory,
            Container tableInv, boolean owned, BlockPos tilePos) {
        super(type, id);
        this.tableInv = tableInv;
        this.owned = owned;
        this.tilePos = tilePos;
        this.addSlot(new Slot(tableInv, 0, 14, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isInkStack(stack);
            }
        });
        this.addSlot(new Slot(tableInv, 1, 70, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isNoteStack(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, 175 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 233));
        }
    }
    public static boolean isInkStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemScribingTools;
    }
    public static boolean isNoteStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()
                || !(stack.getItem() instanceof arcaneresearch.discovered.note.ItemDiscoveredResearchNote)) {
            return false;
        }
        try {
            HexResearchNoteData data = HexResearchNoteData.getData(stack);
            return data == null || !data.isComplete();
        } catch (Exception e) {
            return true;
        }
    }
    public ItemStack getNoteStack() {
        return getSlot(1).getItem();
    }
    public ItemStack getInkStack() {
        return getSlot(0).getItem();
    }
    public Container tableInventory() {
        return tableInv;
    }
    private static final class ClientInit {
        final Container container;
        final BlockPos pos;
        ClientInit(Inventory playerInventory, FriendlyByteBuf buf) {
            Container found = new SimpleContainer(HexNoteTableTile.SIZE);
            BlockPos foundPos = null;
            try {
                foundPos = buf.readBlockPos();
                if (foundPos != null && playerInventory.player.level()
                        .getBlockEntity(foundPos) instanceof HexNoteTableTile tile) {
                    found = tile;
                }
            } catch (Exception ignored) {
            }
            this.container = found;
            this.pos = foundPos;
        }
    }
    @Override
    public boolean stillValid(Player player) {
        if (owned || tilePos == null) {
            return true;
        }
        return player.distanceToSqr(
                tilePos.getX() + 0.5D, tilePos.getY() + 0.5D,
                tilePos.getZ() + 0.5D) <= 64.0D;
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 2, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide || !owned) {
            return;
        }
        for (int i = 0; i < 2; i++) {
            ItemStack stack = getSlot(i).getItem();
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
                if (!stack.isEmpty() && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.drop(stack, false);
                }
                getSlot(i).set(ItemStack.EMPTY);
            }
        }
        broadcastChanges();
    }
}
