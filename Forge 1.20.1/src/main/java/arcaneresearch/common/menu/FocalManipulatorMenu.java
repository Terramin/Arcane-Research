package arcaneresearch.common.menu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.menu.slot.FocusSlot;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModSounds;
public class FocalManipulatorMenu extends AbstractContainerMenu {
    private final TileFocalManipulator blockEntity;
    private final Player player;
    public FocalManipulatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }
    public FocalManipulatorMenu(int containerId, Inventory playerInventory, TileFocalManipulator blockEntity) {
        super(ModMenuTypes.FOCAL_MANIPULATOR.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        addSlot(new FocusSlot(blockEntity, 0, 31, 191));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, row * 18 - 62, 64 + col * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(playerInventory, row + col * 3, row * 18 - 62, col * 18 + 7));
            }
        }
    }
    private static TileFocalManipulator getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileFocalManipulator tile) {
            return tile;
        }
        throw new IllegalStateException("Block entity is not a TileFocalManipulator");
    }
    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button == 0) {
            if (!blockEntity.startCraft(button, player)) {
                player.level().playSound(player, blockEntity.getBlockPos(),
                        ModSounds.CRAFT_FAIL.get(), SoundSource.BLOCKS, 0.33f, 1.0f);
            }
        }
        return false;
    }
    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index == 0) {
                if (!moveItemStackTo(stackInSlot, 1, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stackInSlot.getItem() instanceof ItemFocus) {
                if (!moveItemStackTo(stackInSlot, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index < 28) {
                if (!moveItemStackTo(stackInSlot, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 28 && index < 37) {
                if (!moveItemStackTo(stackInSlot, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return result;
    }
    public TileFocalManipulator getBlockEntity() {
        return blockEntity;
    }
}
