package thaumictinker.tablet;

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
import net.minecraftforge.common.extensions.IForgeMenuType;

 


 
public class DynamismTabletMenu extends AbstractContainerMenu {

    public static final int PLAYER_SLOTS_END = 37;

    private final Container container;
    private final ContainerData data;
    private final @Nullable DynamismTabletBlockEntity tablet;

    public DynamismTabletMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, resolveTablet(inventory, extraData.readBlockPos()));
    }

    public DynamismTabletMenu(int containerId, Inventory inventory, @Nullable DynamismTabletBlockEntity tablet) {
        this(containerId, inventory, tablet, tabletData(tablet), tablet);
    }

    private DynamismTabletMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                               @Nullable DynamismTabletBlockEntity tablet) {
        super(TabletFeature.TABLET_MENU, containerId);
        this.container = container;
        this.data = data;
        this.tablet = tablet;
        boolean server = !inventory.player.level().isClientSide();
        if (server) {
            checkContainerSize(container, 1);
        }
        addSlot(new Slot(container, 0, 80, 15) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                
                
                return !stack.isEmpty();
            }
        });
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
    private static DynamismTabletBlockEntity resolveTablet(Inventory inventory, BlockPos pos) {
        Level level = inventory.player.level();
        if (!level.isClientSide) {
            throw new IllegalStateException("Dynamism tablet menu requires a position buffer");
        }
        return level.getBlockEntity(pos) instanceof DynamismTabletBlockEntity tablet ? tablet : null;
    }

    private static ContainerData tabletData(@Nullable DynamismTabletBlockEntity tablet) {
        if (tablet == null) {
            return new SimpleContainerData(2);
        }
        return new ContainerData() {
            @Override
            public int get(int index) {
                return (index == 0 ? tablet.isLeftClick() : tablet.isRedstone()) ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    tablet.setModes(value != 0, tablet.isRedstone());
                } else {
                    tablet.setModes(tablet.isLeftClick(), value != 0);
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public boolean isLeftClick() {
        return data.get(0) != 0;
    }

    public boolean isRedstone() {
        return data.get(1) != 0;
    }

    public void setModes(boolean leftClick, boolean redstone) {
        if (tablet != null) {
            tablet.setModes(leftClick, redstone);
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
        if (index < 1) {
            if (!moveItemStackTo(stack, 1, PLAYER_SLOTS_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
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
