package arcaneresearch.common.golems.seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import java.util.Random;
public class SealFill extends SealFiltered implements ISealConfigToggles {
    protected SealToggle[] props;
    private int delay;
    private int watchedTask = Integer.MIN_VALUE;
    private ResourceLocation icon;
    public SealFill() {
        delay = new Random(System.nanoTime()).nextInt(50);
        icon = new ResourceLocation(Arcaneresearch.MODID, "item/seals/seal_fill");
        props = new SealToggle[]{
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
        if (delay++ % 20 != 0) {
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
        arcaneresearch.api.ArcaneresearchInvHelper.InvFilter invFilter = invFilter(props);
        net.minecraft.util.Tuple<ItemStack, Integer> tuple =
                arcaneresearch.common.lib.utils.InventoryUtils.findFirstMatchFromFilterTuple(
                        getInv(), getSizes(), isBlacklist(), golem.getCarrying(), invFilter);
        if (tuple.getA() != null && !tuple.getA().isEmpty()) {
            IItemHandler inv = arcaneresearch.api.ArcaneresearchInvHelper.getItemHandlerAt(
                    level, task.getSealPos().pos, task.getSealPos().face);
            int limit = tuple.getA().getCount();
            if (hasStacksizeLimiters() && tuple.getB() != null && tuple.getB() > 0) {
                int c = (inv == null)
                        ? arcaneresearch.common.lib.utils.InventoryUtils.countStackInWorld(
                        golem.getGolemWorld(), task.getSealPos().pos, tuple.getA(), 1.5, invFilter)
                        : arcaneresearch.api.ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), invFilter);
                if (c < tuple.getB()) {
                    limit = tuple.getB() - c;
                } else {
                    limit = 0;
                }
            }
            if (limit > 0) {
                ItemStack t = tuple.getA().copy();
                t.setCount(limit);
                ItemStack s = golem.dropItem(t);
                if (inv == null) {
                    BlockPos p = task.getSealPos().pos;
                    Direction f = task.getSealPos().face;
                    ItemEntity ie = new ItemEntity(level,
                            p.getX() + 0.5 + f.getStepX(),
                            p.getY() + 0.5 + f.getStepY(),
                            p.getZ() + 0.5 + f.getStepZ(), s);
                    Vec3 motion = ie.getDeltaMovement();
                    ie.setDeltaMovement(motion.x / 5.0, motion.y / 2.0, motion.z / 5.0);
                    level.addFreshEntity(ie);
                } else {
                    golem.holdItem(ItemHandlerHelper.insertItemStacked(inv, s, false));
                }
                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                        ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 1.0f);
                golem.addRankXp(1);
                golem.swingArm();
            }
        }
        task.setSuspended(true);
        return true;
    }
    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        arcaneresearch.api.ArcaneresearchInvHelper.InvFilter invFilter = invFilter(props);
        net.minecraft.util.Tuple<ItemStack, Integer> tuple =
                arcaneresearch.common.lib.utils.InventoryUtils.findFirstMatchFromFilterTuple(
                        getInv(), getSizes(), isBlacklist(), golem.getCarrying(), invFilter);
        if (tuple.getA() != null && !tuple.getA().isEmpty()) {
            IItemHandler inv = arcaneresearch.api.ArcaneresearchInvHelper.getItemHandlerAt(
                    golem.getGolemWorld(), task.getSealPos().pos, task.getSealPos().face);
            if (inv != null) {
                if (!tuple.getA().isEmpty() && props[4].getValue()
                        && arcaneresearch.api.ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), invFilter) <= 0) {
                    return false;
                }
                if (!tuple.getA().isEmpty()
                        && arcaneresearch.api.ArcaneresearchInvHelper.hasRoomForSome(
                        golem.getGolemWorld(), task.getSealPos().pos, task.getSealPos().face, tuple.getA())) {
                    if (hasStacksizeLimiters() && tuple.getB() != null && tuple.getB() > 0) {
                        return arcaneresearch.api.ArcaneresearchInvHelper.countTotalItemsIn(inv, tuple.getA(), invFilter)
                                < tuple.getB();
                    }
                    return true;
                }
            } else if (!tuple.getA().isEmpty()) {
                if (hasStacksizeLimiters() && tuple.getB() != null && tuple.getB() > 0) {
                    return arcaneresearch.common.lib.utils.InventoryUtils.countStackInWorld(
                            golem.getGolemWorld(), task.getSealPos().pos, tuple.getA(), 1.5, invFilter)
                            < tuple.getB();
                }
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.getBlockState(pos).isAir();
    }
    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }
    @Override
    public int[] getGuiCategories() {
        return new int[]{1, 0, 4};
    }
    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return null;
    }
    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.CLUMSY};
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
