package arcaneresearch.common.golems.seals;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.concurrent.ThreadLocalRandom;

public class SealFill extends SealFiltered implements ISealConfigToggles {

    private static final int TOGGLE_EXIST = 4;
    private static final int DELAY_RANGE = 50;
    private static final int TICK_PERIOD = 20;
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_fill");
    private static final int[] GUI_CATEGORIES = {1, 0, 4};
    private static final EnumGolemTrait[] REQUIRED_TAGS = new EnumGolemTrait[0];
    private static final EnumGolemTrait[] FORBIDDEN_TAGS = {EnumGolemTrait.CLUMSY};

    protected SealToggle[] props;
    private int delay;
    private int watchedTask = Integer.MIN_VALUE;

    public SealFill() {
        this.delay = ThreadLocalRandom.current().nextInt(DELAY_RANGE);
        this.props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod"),
                new SealToggle(false, "pexist", "golem.prop.exist")
        };
    }

    @Override
    public String getKey() {
        return "arcaneresearch:fill";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % TICK_PERIOD != 0) {
            return;
        }
        Task oldTask = TaskHandler.getTask(level.dimension(), watchedTask);
        if (oldTask == null || oldTask.isReserved() || oldTask.isSuspended() || oldTask.isCompleted()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level.dimension(), task);
            watchedTask = task.getId();
        }
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ArcaneresearchInvHelper.InvFilter filter = invFilter(props);
        Tuple<ItemStack, Integer> tuple = InventoryUtils.findFirstMatchFromFilterTuple(
                getInv(), getSizes(), isBlacklist(), golem.getCarrying(), filter);

        if (tuple.getA().isEmpty()) {
            task.setSuspended(true);
            return true;
        }

        BlockPos sealPos = task.getSealPos().pos;
        Direction sealFace = task.getSealPos().face;
        IItemHandler inv = ArcaneresearchInvHelper.getItemHandlerAt(level, sealPos, sealFace);

        int limit = tuple.getA().getCount();
        if (hasStacksizeLimiters() && tuple.getB() > 0) {
            int c = (inv == null)
                    ? InventoryUtils.countStackInWorld(
                    golem.getGolemWorld(), sealPos, tuple.getA(), 1.5, filter)
                    : ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), filter);
            limit = (c < tuple.getB()) ? tuple.getB() - c : 0;
        }
        limit = Math.min(limit, tuple.getA().getMaxStackSize());

        if (limit > 0) {
            ItemStack t = tuple.getA().copy();
            t.setCount(limit);
            ItemStack s = golem.dropItem(t);

            if (!s.isEmpty()) {
                if (inv == null) {
                    ItemEntity ie = new ItemEntity(level,
                            sealPos.getX() + 0.5 + sealFace.getStepX(),
                            sealPos.getY() + 0.5 + sealFace.getStepY(),
                            sealPos.getZ() + 0.5 + sealFace.getStepZ(), s);
                    Vec3 motion = ie.getDeltaMovement();
                    ie.setDeltaMovement(motion.x / 5.0, motion.y / 2.0, motion.z / 5.0);
                    level.addFreshEntity(ie);
                } else {
                    golem.holdItem(ItemHandlerHelper.insertItemStacked(inv, s, false));
                }

                if (golem.getGolemEntity() != null) {
                    golem.getGolemEntity().playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                            ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 1.0f);
                }
                golem.addRankXp(1);
                golem.swingArm();
            }
        }

        task.setSuspended(true);
        return true;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ArcaneresearchInvHelper.InvFilter filter = invFilter(props);
        Tuple<ItemStack, Integer> tuple = InventoryUtils.findFirstMatchFromFilterTuple(
                getInv(), getSizes(), isBlacklist(), golem.getCarrying(), filter);

        if (tuple.getA().isEmpty()) {
            return false;
        }

        Level world = golem.getGolemWorld();
        BlockPos sealPos = task.getSealPos().pos;
        Direction sealFace = task.getSealPos().face;
        IItemHandler inv = ArcaneresearchInvHelper.getItemHandlerAt(world, sealPos, sealFace);

        if (inv != null) {
            if (props[TOGGLE_EXIST].getValue()
                    && ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), filter) <= 0) {
                return false;
            }
            if (ArcaneresearchInvHelper.hasRoomForSome(world, sealPos, sealFace, tuple.getA())) {
                if (hasStacksizeLimiters() && tuple.getB() > 0) {
                    return ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), filter)
                            < tuple.getB();
                }
                return true;
            }
            return false;
        }

        if (hasStacksizeLimiters() && tuple.getB() > 0) {
            return InventoryUtils.countStackInWorld(world, sealPos, tuple.getA(), 1.5, filter)
                    < tuple.getB();
        }
        return true;
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }

    @Override
    public ResourceLocation getSealIcon() {
        return ICON;
    }

    @Override
    public int[] getGuiCategories() {
        return GUI_CATEGORIES;
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return REQUIRED_TAGS;
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return FORBIDDEN_TAGS;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
        ISealEntity se = SealHandler.getSealEntity(level.dimension(), task.getSealPos());
        if (se != null && !se.isStoppedByRedstone(level)) {
            Task newTask = new Task(task.getSealPos(), task.getSealPos().pos);
            newTask.setPriority(se.getPriority());
            TaskHandler.addTask(level.dimension(), newTask);
            watchedTask = newTask.getId();
        }
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }

    @Override
    public boolean hasStacksizeLimiters() {
        return !isBlacklist();
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        if (index >= 0 && index < props.length) {
            props[index].setValue(value);
        }
    }
}