package arcaneresearch.common.menu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.common.menu.slot.OutputSlot;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
public class GolemBuilderMenu extends AbstractContainerMenu {
    private final TileGolemBuilder blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;
    public GolemBuilderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }
    public GolemBuilderMenu(int containerId, Inventory playerInventory, TileGolemBuilder blockEntity) {
        super(ModMenuTypes.GOLEM_BUILDER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = new SimpleContainerData(2);
        addDataSlots(this.data);
        addSlot(new OutputSlot(blockEntity, 0, 160, 104));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 24 + col * 18, 142 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 24 + col * 18, 200));
        }
    }
    private static TileGolemBuilder getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof TileGolemBuilder tileGolemBuilder) {
            return tileGolemBuilder;
        }
        throw new IllegalStateException("Block entity is not a TileGolemBuilder at " + pos);
    }
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.GOLEM_BUILDER.get());
    }
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (slotIndex == 0) {
                if (!moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, result);
            } else if (!blockEntity.canPlaceItem(0, stackInSlot)
                    || !moveItemStackTo(stackInSlot, 0, 1, false)) {
                return ItemStack.EMPTY;
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
        data.set(0, blockEntity.cost);
        data.set(1, blockEntity.maxCost);
    }
    public TileGolemBuilder getBlockEntity() {
        return blockEntity;
    }
    public int getCost() {
        return data.get(0);
    }
    public int getMaxCost() {
        return data.get(1);
    }
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        return buttonId == 99;
    }
}
