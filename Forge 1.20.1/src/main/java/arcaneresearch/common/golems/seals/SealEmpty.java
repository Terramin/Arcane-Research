package arcaneresearch.common.golems.seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.InventoryUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
public class SealEmpty extends SealFiltered implements ISealConfigToggles {
    protected SealToggle[] props;
    private int delay;
    private int filterInc = 0;
    private HashMap<Integer, ItemStack> cache = new HashMap<>();
    private ResourceLocation icon;
    public SealEmpty() {
        delay = new Random(System.nanoTime()).nextInt(30);
        icon = new ResourceLocation(Arcaneresearch.MODID, "item/seals/seal_empty");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod"),
                new SealToggle(false, "pcycle", "golem.prop.cycle"),
                new SealToggle(false, "pleave", "golem.prop.leave")
        };
    }
    @Override
    public String getKey() {
        return "arcaneresearch:empty";
    }
    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay % 100 == 0) {
            Iterator<Integer> it = cache.keySet().iterator();
            while (it.hasNext()) {
                Task task = TaskHandler.getTask(level.dimension(), it.next());
                if (task == null) {
                    it.remove();
                }
            }
        }
        if (delay++ % 20 != 0) {
            return;
        }
        ItemStack stack = InventoryUtils.findFirstMatchFromFilter(getInv(filterInc), blacklist,
                ArcaneresearchInvHelper.getItemHandlerAt(level, seal.getSealPos().pos, seal.getSealPos().face),
                seal.getSealPos().face, invFilter(props), props[5].getValue());
        if (stack != null && !stack.isEmpty()) {
            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            task.setLifespan((short) 5);
            TaskHandler.addTask(level.dimension(), task);
            cache.put(task.getId(), stack);
        }
    }
    public NonNullList<ItemStack> getInv(int c) {
        return getInv();
    }
    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        int sa = ArcaneresearchInvHelper.countTotalItemsIn(level, task.getSealPos().pos,
                task.getSealPos().face, stack, invFilter(props));
        if (stack != null && !stack.isEmpty() && props[5].getValue() && sa <= stack.getCount()) {
            stack = stack.copy();
            stack.setCount(Math.max(0, sa - 1));
        }
        if (stack != null && !stack.isEmpty()) {
            int limit = golem.canCarryAmount(stack);
            if (limit > 0) {
                ItemStack s = golem.holdItem(
                        InventoryUtils.removeStackFrom(level, task.getSealPos().pos, task.getSealPos().face,
                                InventoryUtils.copyLimitedStack(stack, limit), invFilter(props), false));
                if (!s.isEmpty()) {
                    InventoryUtils.ejectStackAt(level, task.getSealPos().pos.relative(task.getSealPos().face),
                            task.getSealPos().face.getOpposite(), s);
                }
                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                        ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
                golem.swingArm();
            }
        }
        cache.remove(task.getId());
        filterInc++;
        task.setSuspended(true);
        return true;
    }
    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemStack stack = cache.get(task.getId());
        return stack != null && !stack.isEmpty() && golem.canCarry(stack, true);
    }
    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return ArcaneresearchInvHelper.getItemHandlerAt(level, pos, side) != null;
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
    }
    @Override
    public void onTaskSuspension(Level level, Task task) {
        cache.remove(task.getId());
    }
    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
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
