package thaumictinker.kami.gateway;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
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

 



 
public class WarpGateMenu extends AbstractContainerMenu {

    public static final int PLAYER_SLOTS_END = WarpGateBlockEntity.SIZE + 36;

    private final Container container;
    private final ContainerData data;
    @Nullable
    private final WarpGateBlockEntity gate;

    public WarpGateMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, resolveGate(inventory, extraData.readBlockPos()));
    }

    public WarpGateMenu(int containerId, Inventory inventory, @Nullable WarpGateBlockEntity gate) {
        this(containerId, inventory, gate == null ? new SimpleContainer(WarpGateBlockEntity.SIZE) : gate,
                gateData(gate), gate);
    }

    private WarpGateMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                         @Nullable WarpGateBlockEntity gate) {
        super(GatewayFeature.WARP_GATE_MENU, containerId);
        this.container = container;
        this.data = data;
        this.gate = gate;
        if (!inventory.player.level().isClientSide()) {
            checkContainerSize(container, WarpGateBlockEntity.SIZE);
        }
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 5; column++) {
                addSlot(new SlotSkyPearl(container, row * 5 + column, 30 + column * 25, 27 + row * 25));
            }
        }
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
    private static WarpGateBlockEntity resolveGate(Inventory inventory, BlockPos pos) {
        Level level = inventory.player.level();
        if (!level.isClientSide) {
            throw new IllegalStateException("Warp gate menu requires a position buffer");
        }
        return level.getBlockEntity(pos) instanceof WarpGateBlockEntity gate ? gate : null;
    }

    private static ContainerData gateData(@Nullable WarpGateBlockEntity gate) {
        if (gate == null) {
            return new SimpleContainerData(1);
        }
        return new ContainerData() {
            @Override
            public int get(int index) {
                return gate.isLocked() ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
                gate.setLocked(value != 0);
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    public boolean isLocked() {
        return data.get(0) != 0;
    }

    public void setLocked(boolean locked) {
        if (gate != null) {
            gate.setLocked(locked);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < WarpGateBlockEntity.SIZE) {
            if (!moveItemStackTo(stack, WarpGateBlockEntity.SIZE, PLAYER_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!(stack.getItem() instanceof SkyPearlItem)
                || !moveItemStackTo(stack, 0, WarpGateBlockEntity.SIZE, false)) {
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
}
