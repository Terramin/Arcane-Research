package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.GolemInteractionHelper;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.utils.InventoryUtils;

import java.util.Random;

public class SealUse extends SealFiltered implements ISealConfigToggles {

    protected SealToggle[] props;
    private int delay;
    private int watchedTask = Integer.MIN_VALUE;
    private Identifier icon;

    public SealUse() {
        delay = new Random(System.nanoTime()).nextInt(49);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_use");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod"),
                new SealToggle(false, "pleft", "golem.prop.left"),
                new SealToggle(false, "pempty", "golem.prop.empty"),
                new SealToggle(false, "pemptyhand", "golem.prop.emptyhand"),
                new SealToggle(false, "psneak", "golem.prop.sneak"),
                new SealToggle(false, "ppro", "golem.prop.provision.wl")
        };
    }

    @Override
    public String getKey() {
        return "arcaneresearch:use";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % 5 != 0) {
            return;
        }

        Task oldTask = TaskHandler.getTask(level.dimension(), watchedTask);
        if (oldTask == null || oldTask.isSuspended() || oldTask.isCompleted()) {
            boolean isEmpty = level.isEmptyBlock(seal.getSealPos().pos);
            if (props[5].getValue() != isEmpty) {
                return;
            }

            Task task = new Task(seal.getSealPos(), seal.getSealPos().pos);
            task.setPriority(seal.getPriority());
            TaskHandler.addTask(level.dimension(), task);
            watchedTask = task.getId();
        }
    }

    @Override
    public void onTaskStarted(Level level, IGolemAPI golem, Task task) {
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        boolean isEmpty = level.isEmptyBlock(task.getPos());
        if (props[5].getValue() == isEmpty) {

            ItemStack clickStack = ItemStack.EMPTY;
            if (!golem.getCarrying().isEmpty()) {
                if (!filter.get(0).isEmpty()) {
                    clickStack = findMatchingCarried(golem);
                } else {
                    clickStack = golem.getCarrying().get(0);
                }
            }

            if (!clickStack.isEmpty() || props[6].getValue()) {
                ItemStack ss = ItemStack.EMPTY;
                if (!clickStack.isEmpty()) {
                    ss = clickStack.copy();
                    golem.dropItem(clickStack.copy());
                }

                GolemInteractionHelper.golemClick(
                        level, golem, task.getPos(), task.getSealPos().face,
                        props[6].getValue() ? ItemStack.EMPTY : ss,
                        props[7].getValue(),
                        !props[4].getValue()
                );

            }
        }

        task.setSuspended(true);
        return true;
    }

    private ItemStack findMatchingCarried(IGolemAPI golem) {
        return InventoryUtils.findFirstMatchFromFilterTuple(filter, filterSize, blacklist,
                golem.getCarrying(), invFilter(props)).getFirst();
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        if (props[6].getValue()) {
            return true;
        }

        boolean found = !InventoryUtils.findFirstMatchFromFilterTuple(filter, filterSize, blacklist,
                golem.getCarrying(), invFilter(props)).getFirst().isEmpty();

        if (!found && props[8].getValue() && !blacklist && !filter.get(0).isEmpty()) {
            ISealEntity sealEntity = SealHandler.getSealEntity(golem.getGolemWorld().dimension(), task.getSealPos());
            if (sealEntity != null) {
                ItemStack requested = filter.get(0).copy();

                if (!props[0].getValue() && requested.isDamageableItem()) {
                    requested.setDamageValue(32767);
                }
                GolemHelper.requestProvisioning(golem.getGolemWorld(), sealEntity, requested);
            }
        }

        return found;
    }

    @Override
    public void onTaskSuspension(Level level, Task task) {
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public Identifier getSealIcon() {
        return icon;
    }

    @Override
    public void onRemoval(Level level, BlockPos pos, Direction side) {
    }

    @Override
    public Object returnContainer(Level level, Player player, BlockPos pos, Direction side, ISealEntity seal) {
        return null;
    }

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
        return new EnumGolemTrait[]{EnumGolemTrait.DEFT, EnumGolemTrait.SMART};
    }

    @Override
    public EnumGolemTrait[] getForbiddenTags() {
        return null;
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