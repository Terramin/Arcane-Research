package thaumictinker.enchanting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import arcaneresearch.api.aspects.Aspect;

 

 
public class EnchanterMenu extends AbstractContainerMenu {

    private static final int DATA_WORKING = 12;

    private final BlockPos pos;
    private final @Nullable EnchanterBlockEntity enchanter;
    private final ContainerData data = new SimpleContainerData(13);
    private final boolean server;

    public EnchanterMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, resolveEnchanter(inventory, extraData));
    }

    public EnchanterMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, resolveEnchanter(inventory.player.level().getBlockEntity(pos), inventory));
    }

    private EnchanterMenu(int containerId, Inventory inventory, @Nullable EnchanterBlockEntity enchanter) {
        super(EnchantingFeature.ENCHANTER_MENU, containerId);
        this.pos = enchanter != null ? enchanter.getBlockPos() : BlockPos.ZERO;
        this.enchanter = enchanter;
        this.server = !inventory.player.level().isClientSide();
        net.minecraft.world.Container container = enchanter != null ? enchanter : new SimpleContainer(2);
        addSlot(new SlotTool(container, enchanter, EnchanterBlockEntity.TOOL, 6, 6));
        addSlot(new SlotWand(container, EnchanterBlockEntity.WAND, 6, 31));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    @Nullable
    private static EnchanterBlockEntity resolveEnchanter(Inventory inventory, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        net.minecraft.world.level.Level level = inventory.player.level();
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnchanterBlockEntity enchanter) {
            return enchanter;
        }
        if (!level.isClientSide) {
            throw new IllegalStateException("Block entity is not an EnchanterBlockEntity at " + pos);
        }
        return null;
    }

    @Nullable
    private static EnchanterBlockEntity resolveEnchanter(net.minecraft.world.level.block.entity.BlockEntity blockEntity, Inventory inventory) {
        if (blockEntity instanceof EnchanterBlockEntity enchanter) {
            return enchanter;
        }
        if (!inventory.player.level().isClientSide) {
            throw new IllegalStateException("Block entity is not an EnchanterBlockEntity at " + blockEntity.getBlockPos());
        }
        return null;
    }

    public BlockPos pos() {
        return pos;
    }

    public @Nullable EnchanterBlockEntity enchanter() {
        return enchanter;
    }

    public int total(Aspect aspect) {
        return data.get(EnchantmentManager.PRIMAL_ASPECTS.indexOf(aspect));
    }

    public int current(Aspect aspect) {
        return data.get(EnchantmentManager.PRIMAL_ASPECTS.indexOf(aspect) + 6);
    }

    public boolean isWorking() {
        return data.get(DATA_WORKING) != 0;
    }

    @Override
    public void broadcastChanges() {
        if (server && enchanter != null) {
            for (int index = 0; index < EnchantmentManager.PRIMAL_ASPECTS.size(); index++) {
                Aspect aspect = EnchantmentManager.PRIMAL_ASPECTS.get(index);
                data.set(index, enchanter.getTotalAspects().getAmount(aspect));
                data.set(index + 6, enchanter.getCurrentAspects().getAmount(aspect));
            }
            data.set(DATA_WORKING, enchanter.isWorking() ? 1 : 0);
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (stack.is(Items.BOOK)) {
            return ItemStack.EMPTY;
        }
        boolean wand = slots.get(EnchanterBlockEntity.WAND).mayPlace(stack);
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
        } else if (wand) {
            if (!moveItemStackTo(stack, EnchanterBlockEntity.WAND, EnchanterBlockEntity.WAND + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (EnchantmentManager.isTool(stack)
                && !moveItemStackTo(stack, EnchanterBlockEntity.TOOL, EnchanterBlockEntity.TOOL + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return enchanter != null && enchanter.stillValid(player);
    }
}
