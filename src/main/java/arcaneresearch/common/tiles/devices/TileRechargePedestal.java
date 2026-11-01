package arcaneresearch.common.tiles.devices;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.items.IRechargable;
import arcaneresearch.api.items.RechargeHelper;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;

import static arcaneresearch.api.items.RechargeHelper.*;

public class TileRechargePedestal extends TileArcaneresearchInventory implements IAspectContainer, WorldlyContainer {

    private static final int[] SLOTS = {0};

    private int counter = 0;

    @Nullable
    private AABB renderBox;

    public TileRechargePedestal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
        syncedSlots = new int[]{0};
    }

    public TileRechargePedestal(BlockPos pos, BlockState state) {
        this(ModBlockEntities.RECHARGE_PEDESTAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileRechargePedestal tile) {
        tile.counter++;
        if (tile.counter % 10 != 0) return;
        ItemStack stack = tile.getItem(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable)) return;
        float recharged = rechargeItem(level, stack, pos, null, 5);
        if (recharged <= 0) return;
        tile.markDirtyAndSync();
        ArrayList<Aspect> primals = Aspect.getPrimalAspects();
        if (primals.isEmpty()) return;
        int color = primals.get(level.random.nextInt(primals.size())).getColor();
        level.blockEvent(pos, state.getBlock(), 5, color);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileRechargePedestal tile) {
    }

    public void setInventorySlotContentsFromInfusion(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof IRechargable;
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction direction) {
        return slot == 0 && stack.getItem() instanceof IRechargable;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public AspectList getAspects() {
        ItemStack stack = (level == null || level.isClientSide) ? getSyncedStackInSlot(0) : getItem(0);
        if (!stack.isEmpty() && stack.getItem() instanceof IRechargable) {
            int charge = Math.round(getCharge(stack));
            if (charge > 0) {
                return new AspectList().add(Aspect.ENERGY, charge);
            }
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

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 5) {
            if (level != null && level.isClientSide) {
                spawnVisSparkle(param);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnVisSparkle(int param) {
        int ox = worldPosition.getX();
        int oy = worldPosition.getY();
        int oz = worldPosition.getZ();
        int ny = oy + 1;
        assert level != null;
        FXDispatcher.INSTANCE.visSparkle(
                ox + level.random.nextInt(3) - level.random.nextInt(3),
                ny + level.random.nextInt(3),
                oz + level.random.nextInt(3) - level.random.nextInt(3),
                ox, ny, oz, param);
    }

    @Override
    public AABB getRenderBoundingBox() {
        AABB box = renderBox;
        if (box == null) {
            box = new AABB(worldPosition).inflate(2.0);
            renderBox = box;
        }
        return box;
    }
}