package arcaneresearch.common.golems.seals;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.ProvisionRequest;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

public class SealProvide extends SealFiltered implements ISealConfigToggles {

    private static final int TOGGLE_SINGLE = 4;
    private static final int TOGGLE_LEAVE = 5;
    private static final int DATA_PICKUP = 0;
    private static final int DATA_DELIVER_ENTITY = 1;
    private static final int DATA_DELIVER_POS = 2;
    private static final int RANGE_SQ = 64 * 64;
    private static final int DELAY_RANGE = 88;
    private static final int CLEANUP_PERIOD = 100;
    private static final int PROCESS_PERIOD = 20;
    private static final short SHORT_LIFESPAN = 10;
    private static final short LONG_LIFESPAN = 31000;

    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_provider");
    private static final int[] GUI_CATEGORIES = {1, 3, 0, 4};
    private static final EnumGolemTrait[] REQUIRED_TAGS = new EnumGolemTrait[0];
    private static final EnumGolemTrait[] FORBIDDEN_TAGS = {EnumGolemTrait.CLUMSY};

    protected SealToggle[] props;
    private int delay;

    public SealProvide() {
        this.delay = ThreadLocalRandom.current().nextInt(DELAY_RANGE);
        this.props = new SealToggle[]{
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

    private boolean isInRange(ProvisionRequest pr, BlockPos sealPos) {
        ISealEntity prSeal = pr.getSeal();
        if (prSeal != null && prSeal.getSealPos() != null
                && prSeal.getSealPos().pos.distSqr(sealPos) < RANGE_SQ) {
            return true;
        }
        if (pr.getEntity() != null
                && sealPos.distToCenterSqr(pr.getEntity().getX(), pr.getEntity().getY(), pr.getEntity().getZ()) < RANGE_SQ) {
            return true;
        }
        return pr.getPos() != null && sealPos.distSqr(pr.getPos()) < RANGE_SQ;
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        var dimKey = level.dimension();

        if (delay % CLEANUP_PERIOD == 0) {
            List<ProvisionRequest> cleanupList = GolemHelper.provisionRequests.get(dimKey);
            if (cleanupList != null) {
                long now = System.currentTimeMillis();
                cleanupList.removeIf(pr -> pr.isInvalid()
                        || pr.getLinkedTask() == null
                        || pr.getLinkedTask().isSuspended()
                        || pr.getLinkedTask().isCompleted()
                        || pr.getTimeout() < now);
            }
        }

        if (delay++ % PROCESS_PERIOD != 0) {
            return;
        }

        SealPos sealPos = seal.getSealPos();
        IItemHandler inv = getItemHandler(level, sealPos.pos, sealPos.face);
        if (inv == null) {
            return;
        }
        List<ProvisionRequest> requests = GolemHelper.provisionRequests.get(dimKey);
        if (requests == null) {
            return;
        }

        boolean leaveMode = props[TOGGLE_LEAVE].getValue();
        ListIterator<ProvisionRequest> it = requests.listIterator();
        while (it.hasNext()) {
            ProvisionRequest pr = it.next();
            if (pr.isInvalid()) {
                it.remove();
                continue;
            }
            if (pr.getLinkedTask() != null) {
                continue;
            }
            if (!isInRange(pr, sealPos.pos)) {
                continue;
            }
            ItemStack prStack = pr.getStack();
            if (prStack == null || prStack.isEmpty()) {
                continue;
            }
            NonNullList<ItemStack> stacks = NonNullList.withSize(1, prStack);
            boolean filterMatch = !InventoryUtils.findFirstMatchFromFilterTuple(
                    getInv(), getSizes(), blacklist, stacks, invFilter(props)).getA().isEmpty();
            int available = ArcaneresearchInvHelper.countTotalItemsIn(inv, prStack, ArcaneresearchInvHelper.InvFilter.STRICT);
            int minRequired = leaveMode ? 2 : 1;
            if (filterMatch && available >= minRequired) {
                Task task = new Task(sealPos, sealPos.pos);
                ISealEntity prSeal = pr.getSeal();
                task.setPriority(prSeal != null ? prSeal.getPriority() : 5);
                task.setLifespan(prSeal != null ? SHORT_LIFESPAN : LONG_LIFESPAN);
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
        if (pr == null) {
            task.setSuspended(true);
            return true;
        }
        SealPos sealPos = task.getSealPos();
        int data = task.getData();

        if (data == DATA_PICKUP) {
            handlePickup(level, golem, task, pr, sealPos);
        } else if (data == DATA_DELIVER_ENTITY || data == DATA_DELIVER_POS) {
            handleDelivery(level, golem, task, pr);
        }
        task.setSuspended(true);
        return true;
    }

    private void handlePickup(Level level, IGolemAPI golem, Task task, ProvisionRequest pr, SealPos sealPos) {
        IItemHandler inv = getItemHandler(level, sealPos.pos, sealPos.face);
        if (inv == null) {
            return;
        }
        ItemStack prStack = pr.getStack();
        if (prStack == null || prStack.isEmpty()) {
            return;
        }
        ItemStack stack = prStack.copy();
        if (props[TOGGLE_SINGLE].getValue()) {
            stack.setCount(1);
        }
        if (props[TOGGLE_LEAVE].getValue()) {
            int sa = ArcaneresearchInvHelper.countTotalItemsIn(inv, stack, ArcaneresearchInvHelper.InvFilter.STRICT);
            if (sa <= stack.getCount()) {
                stack.setCount(Math.max(0, sa - 1));
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        int limit = golem.canCarryAmount(stack);
        if (limit <= 0) {
            return;
        }
        ItemStack s = golem.holdItem(InventoryUtils.removeStackFrom(inv,
                InventoryUtils.copyLimitedStack(stack, limit), ArcaneresearchInvHelper.InvFilter.STRICT, false));
        if (s != null && !s.isEmpty()) {
            InventoryUtils.ejectStackAt(level, sealPos.pos.relative(sealPos.face),
                    sealPos.face.getOpposite(), s);
        }
        LivingEntity entity = golem.getGolemEntity();
        if (entity != null) {
            entity.playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 2.0f);
        }
        golem.addRankXp(1);
        golem.swingArm();
        if (pr.getEntity() != null || pr.getPos() != null) {
            Task deliveryTask;
            if (pr.getEntity() != null) {
                deliveryTask = new Task(sealPos, pr.getEntity());
                deliveryTask.setData(DATA_DELIVER_ENTITY);
            } else {
                deliveryTask = new Task(sealPos, pr.getPos());
                deliveryTask.setData(DATA_DELIVER_POS);
            }
            deliveryTask.setPriority(task.getPriority());
            deliveryTask.setLifespan(LONG_LIFESPAN);
            TaskHandler.addTask(level.dimension(), deliveryTask);
            pr.setLinkedTask(deliveryTask);
            deliveryTask.setLinkedProvision(pr);
        }
    }

    private void handleDelivery(Level level, IGolemAPI golem, Task task, ProvisionRequest pr) {
        ItemStack cs = pr.getStack();
        if (cs == null || cs.isEmpty()) {
            pr.setInvalid(true);
            return;
        }
        ItemStack s = golem.dropItem(cs);
        if (!s.isEmpty() && s.getCount() < cs.getCount()) {
            ItemStack ps = cs.copy();
            ps.setCount(cs.getCount() - s.getCount());
            if (task.getData() == DATA_DELIVER_ENTITY && pr.getEntity() != null) {
                GolemHelper.requestProvisioning(level, pr.getEntity(), ps);
            } else if (pr.getPos() != null && pr.getSide() != null) {
                GolemHelper.requestProvisioning(level, pr.getPos(), pr.getSide(), ps);
            }
        }
        if (task.getData() == DATA_DELIVER_ENTITY && pr.getEntity() != null) {
            InventoryUtils.dropItemAtEntity(level, s, pr.getEntity());
        } else if (pr.getPos() != null && pr.getSide() != null) {
            ItemStack back = InventoryUtils.ejectStackAt(level, pr.getPos().relative(pr.getSide()),
                    pr.getSide().getOpposite(), s, true);
            if (!back.isEmpty()) {
                golem.holdItem(back);
            }
        }
        LivingEntity entity = golem.getGolemEntity();
        if (entity != null) {
            entity.playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                    ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * 1.0f);
        }
        golem.swingArm();
        pr.setInvalid(true);
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ProvisionRequest pr = task.getLinkedProvision();
        if (pr == null || !(golem instanceof EntityArcaneresearchGolem tcGolem)) {
            return false;
        }
        boolean inRestriction =
                (pr.getSeal() != null && pr.getSeal().getSealPos() != null
                        && tcGolem.isWithinRestriction(pr.getSeal().getSealPos().pos))
                        || (pr.getEntity() != null && tcGolem.isWithinRestriction(pr.getEntity().blockPosition()))
                        || (pr.getPos() != null && tcGolem.isWithinRestriction(pr.getPos()));
        if (!inRestriction || !areGolemTagsValidForTask(pr.getSeal(), golem)) {
            return false;
        }
        ItemStack prStack = pr.getStack();
        if (prStack == null || prStack.isEmpty()) {
            return false;
        }
        if (task.getData() == DATA_PICKUP) {
            return !golem.isCarrying(prStack) && golem.canCarry(prStack, true);
        }
        return golem.isCarrying(prStack);
    }

    private boolean areGolemTagsValidForTask(ISealEntity se, IGolemAPI golem) {
        if (se == null) {
            return true;
        }
        if (se.isLocked()) {
            String owner = "";
            if (golem instanceof EntityArcaneresearchGolem tcGolem && tcGolem.getOwnerUUID() != null) {
                owner = tcGolem.getOwnerUUID().toString();
            }
            if (!owner.equals(se.getOwner())) {
                return false;
            }
        }
        var seal = se.getSeal();
        if (seal == null) {
            return true;
        }
        var traits = golem.getProperties().getTraits();
        EnumGolemTrait[] required = seal.getRequiredTags();
        if (required != null) {
            for (EnumGolemTrait tag : required) {
                if (!traits.contains(tag)) {
                    return false;
                }
            }
        }
        EnumGolemTrait[] forbidden = seal.getForbiddenTags();
        if (forbidden != null) {
            for (EnumGolemTrait tag : forbidden) {
                if (traits.contains(tag)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
        ProvisionRequest pr = task.getLinkedProvision();
        if (pr != null) {
            pr.setLinkedTask(null);
        }
        task.setLinkedProvision(null);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return getItemHandler(level, pos, side) != null;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return ICON;
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