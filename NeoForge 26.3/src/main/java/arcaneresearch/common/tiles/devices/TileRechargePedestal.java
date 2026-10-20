package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;

import java.util.ArrayList;

public class TileRechargePedestal extends TileArcaneresearchInventory implements IAspectContainer, WorldlyContainer {

    private static final int[] SLOTS = {0};
    private int counter = 0;

    public TileRechargePedestal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);

        syncedSlots = new int[]{0};
    }

    public TileRechargePedestal(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileRechargePedestal tile) {
        tile.counter++;

        if (tile.counter % 10 == 0) {
            ItemStack stack = tile.getItem(0);
            if (!stack.isEmpty() && stack.getItem() instanceof IRechargable) {
                float recharged = RechargeHelper.rechargeItem(level, stack, pos, null, 5);
                if (recharged > 0) {
                    tile.syncTile(false);
                    tile.setChanged();

                    ArrayList<Aspect> primals = Aspect.getPrimalAspects();
                    int color = primals.get(level.getRandom().nextInt(primals.size())).getColor();
                    level.blockEvent(pos, state.getBlock(), 5, color);
                }
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileRechargePedestal tile) {

    }

    public void setInventorySlotContentsFromInfusion(int slot, ItemStack stack) {
        setItem(slot, stack);
        setChanged();
        if (level != null && !level.isClientSide()) {
            syncTile(false);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof IRechargable;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return stack.getItem() instanceof IRechargable;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public AspectList getAspects() {

        ItemStack stack = (level == null || level.isClientSide()) ? getSyncedStackInSlot(0) : getItem(0);
        if (!stack.isEmpty() && stack.getItem() instanceof IRechargable) {
            float charge = RechargeHelper.getCharge(stack);
            return new AspectList().add(Aspect.ENERGY, Math.round(charge));
        }
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {

    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    public boolean triggerEvent(int id, int param) {
        if (id == 5) {
            if (level != null && level.isClientSide()) {
                spawnVisSparkle(param);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnVisSparkle(int param) {
        FXDispatcher.INSTANCE.visSparkle(
                worldPosition.getX() + level.getRandom().nextInt(3) - level.getRandom().nextInt(3),
                worldPosition.above().getY() + level.getRandom().nextInt(3),
                worldPosition.getZ() + level.getRandom().nextInt(3) - level.getRandom().nextInt(3),
                worldPosition.getX(),
                worldPosition.above().getY(),
                worldPosition.getZ(),
                param);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(2.0);
    }
}
