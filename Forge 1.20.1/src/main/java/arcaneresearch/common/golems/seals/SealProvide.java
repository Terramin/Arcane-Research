package arcaneresearch.common.golems.seals;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.ProvisionRequest;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.InventoryUtils;
import java.util.*;
public class SealProvide extends SealFiltered implements ISealConfigToggles {
    protected SealToggle[] props;
    private int delay;
    private ResourceLocation icon;
    public SealProvide() {
        delay = new Random(System.nanoTime()).nextInt(88);
        icon = new ResourceLocation(Arcaneresearch.MODID, "item/seals/seal_provider");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod"),
                new SealToggle(false, "psing", "golem.prop.single"),
                new SealToggle(false, "pleave", "golem.prop.leave")
        };
    }
    @Override
    public String getKey() {
        return "arcaneresearch:provider";
    }
    @Override
    public int getFilterSize() {
        return 9;
    }
    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        var dimKey = level.dimension();
        if (delay % 100 == 0 && GolemHelper.provisionRequests.containsKey(dimKey)) {
            Iterator<ProvisionRequest> it = GolemHelper.provisionRequests.get(dimKey).iterator();
            while (it.hasNext()) {
                ProvisionRequest pr = it.next();
                if (pr.isInvalid() ||
                        pr.getLinkedTask() == null ||
                        pr.getLinkedTask().isSuspended() ||
                        pr.getLinkedTask().isCompleted() ||
                        pr.getTimeout() < System.currentTimeMillis()) {
                    it.remove();
                }
            }
        }
        if (delay++ % 20 != 0) {
            return;
        }
        IItemHandler inv = getItemHandler(level, seal.getSealPos().pos, seal.getSealPos().face);
        if (inv == null) return;
        if (!GolemHelper.provisionRequests.containsKey(dimKey)) return;
        ArrayList<ProvisionRequest> requests = GolemHelper.provisionRequests.get(dimKey);
        ListIterator<ProvisionRequest> it = requests.listIterator();
        while (it.hasNext()) {
            ProvisionRequest pr = it.next();
            if (pr.isInvalid()) {
                it.remove();
                continue;
            }
            if (pr.getLinkedTask() != null) continue;
            BlockPos sealPos = seal.getSealPos().pos;
            boolean inRange = false;
            if (pr.getSeal() != null && pr.getSeal().getSealPos().pos.distSqr(sealPos) < 4096) {
                inRange = true;
            } else if (pr.getEntity() != null &&
                    sealPos.distToCenterSqr(pr.getEntity().getX(), pr.getEntity().getY(), pr.getEntity().getZ()) < 4096) {
                inRange = true;
            } else if (pr.getPos() != null && sealPos.distSqr(pr.getPos()) < 4096) {
                inRange = true;
            }
            if (!inRange) continue;
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, pr.getStack());
            if (!InventoryUtils.findFirstMatchFromFilterTuple(getInv(), getSizes(), blacklist, stacks, invFilter(props)).getA().isEmpty() &&
                    ArcaneresearchInvHelper.countTotalItemsIn(inv, pr.getStack(), ArcaneresearchInvHelper.InvFilter.STRICT) > (props[5].getValue() ? 1 : 0)) {
                Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
                task.setPriority(pr.getSeal() != null ? pr.getSeal().getPriority() : (byte) 5);
                task.setLifespan((short) (pr.getSeal() != null ? 10 : 31000));
                TaskHandler.addTask(level.dimension(), task);
                pr.setLinkedTask(task);
                task.setLinkedProvision(pr);
                break;
            }
        }
    }
    private IItemHandler getItemHandler(Level level, BlockPos pos, Direction face) {
        return ArcaneresearchInvHelper.getItemHandlerAt(level, pos, face);
    }
    public boolean matchesFilters(ItemStack stack) {
        return InventoryUtils.matchesFilters(getInv(), blacklist, stack, invFilter(props));
    }
    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ProvisionRequest pr = task.getLinkedProvision();
        if (pr != null) {
            if (task.getData() == 0) {
                IItemHandler inv = getItemHandler(level, task.getSealPos().pos, task.getSealPos().face);
                if (inv != null) {
                    ItemStack stack = ItemStack.EMPTY;
                    try {
                        stack = pr.getStack().copy();
                    } catch (Exception ignored) {
                    }
                    if (stack != null && props[4].getValue()) {
                        stack.setCount(1);
                    }
                    int sa = 0;
                    if (stack != null && !stack.isEmpty() && props[5].getValue() &&
                            (sa = ArcaneresearchInvHelper.countTotalItemsIn(inv, stack, ArcaneresearchInvHelper.InvFilter.STRICT)) <= stack.getCount()) {
                        stack.setCount(sa - 1);
                    }
                    if (stack != null && !stack.isEmpty()) {
                        int limit = golem.canCarryAmount(stack);
                        if (limit > 0) {
                            ItemStack s = golem.holdItem(InventoryUtils.removeStackFrom(inv,
                                    InventoryUtils.copyLimitedStack(stack, limit), ArcaneresearchInvHelper.InvFilter.STRICT, false));
                            if (s != null && !s.isEmpty()) {
                                InventoryUtils.ejectStackAt(level, task.getSealPos().pos.relative(task.getSealPos().face),
                                        task.getSealPos().face.getOpposite(), s);
                            }
                            ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
                            golem.addRankXp(1);
                            golem.swingArm();
                            if (pr.getEntity() != null || pr.getPos() != null) {
                                Task deliveryTask;
                                if (pr.getEntity() != null) {
                                    deliveryTask = new Task(task.getSealPos(), pr.getEntity());
                                } else {
                                    deliveryTask = new Task(task.getSealPos(), pr.getPos());
                                }
                                deliveryTask.setPriority(task.getPriority());
                                deliveryTask.setData(pr.getEntity() != null ? 1 : 2);
                                deliveryTask.setLifespan((short) 31000);
                                TaskHandler.addTask(level.dimension(), deliveryTask);
                                pr.setLinkedTask(deliveryTask);
                                deliveryTask.setLinkedProvision(pr);
                            }
                        }
                    }
                }
            } else if (task.getLinkedProvision() != null) {
                ProvisionRequest pr2 = task.getLinkedProvision();
                ItemStack cs = pr2.getStack();
                ItemStack s = golem.dropItem(cs);
                if (s.getCount() < cs.getCount()) {
                    ItemStack ps = cs.copy();
                    ps.setCount(cs.getCount() - s.getCount());
                    if (task.getData() == 1) {
                        GolemHelper.requestProvisioning(level, pr2.getEntity(), ps);
                    } else {
                        GolemHelper.requestProvisioning(level, pr2.getPos(), pr2.getSide(), ps);
                    }
                }
                if (task.getData() == 1) {
                    InventoryUtils.dropItemAtEntity(level, s, pr2.getEntity());
                } else {
                    ItemStack back = InventoryUtils.ejectStackAt(level, pr2.getPos().relative(pr2.getSide()),
                            pr2.getSide().getOpposite(), s, true);
                    if (!back.isEmpty()) {
                        golem.holdItem(back);
                    }
                }
                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                        ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 1.0f);
                golem.swingArm();
                pr2.setInvalid(true);
            }
        }
        task.setSuspended(true);
        return true;
    }
    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ProvisionRequest pr = task.getLinkedProvision();
        boolean b = pr != null && golem instanceof EntityArcaneresearchGolem tcGolem &&
                ((pr.getSeal() != null && tcGolem.isWithinRestriction(pr.getSeal().getSealPos().pos)) ||
                        (pr.getEntity() != null && tcGolem.isWithinRestriction(pr.getEntity().blockPosition())) ||
                        (pr.getPos() != null && tcGolem.isWithinRestriction(pr.getPos())));
        if (task.getData() == 0) {
            return b && areGolemTagsValidForTask(pr.getSeal(), golem) &&
                    pr.getStack() != null && !golem.isCarrying(pr.getStack()) && golem.canCarry(pr.getStack(), true);
        } else {
            return b && areGolemTagsValidForTask(pr.getSeal(), golem) &&
                    pr.getStack() != null && golem.isCarrying(pr.getStack());
        }
    }
    private boolean areGolemTagsValidForTask(ISealEntity se, IGolemAPI golem) {
        if (se != null) {
            if (se.isLocked()) {
                String owner = "";
                if (golem instanceof EntityArcaneresearchGolem tcGolem && tcGolem.getOwnerUUID() != null) {
                    owner = tcGolem.getOwnerUUID().toString();
                }
                if (!owner.equals(se.getOwner())) return false;
            }
            if (se.getSeal().getRequiredTags() != null &&
                    !golem.getProperties().getTraits().containsAll(Arrays.asList(se.getSeal().getRequiredTags())))
                return false;
            if (se.getSeal().getForbiddenTags() != null)
                for (EnumGolemTrait tag : se.getSeal().getForbiddenTags()) {
                    if (golem.getProperties().getTraits().contains(tag)) return false;
                }
        } else {
            return true;
        }
        return true;
    }
    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }
    @Override
    public void onTaskSuspension(Level level, Task task) {
        if (task.getLinkedProvision() != null) {
            task.getLinkedProvision().setLinkedTask(null);
        }
        task.setLinkedProvision(null);
    }
    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return getItemHandler(level, pos, side) != null;
    }
    @Override
    public ResourceLocation getSealIcon() {
        return icon;
    }
    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }
    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public Object returnGui(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }
    @Override
    public int[] getGuiCategories() {
        return new int[]{1, 3, 0, 4};
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
