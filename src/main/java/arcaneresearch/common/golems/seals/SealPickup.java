package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.GolemHelper;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.seals.ISealConfigArea;
import arcaneresearch.api.golems.seals.ISealConfigToggles;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.tasks.TaskHandler;

import java.util.*;

public class SealPickup extends SealFiltered implements ISealConfigArea, ISealConfigToggles {

    protected SealToggle[] props;
    private int delay;
    private HashMap<Integer, Integer> itemEntities = new HashMap<>();
    private Identifier icon;

    public SealPickup() {
        delay = new Random(System.nanoTime()).nextInt(100);
        icon = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_pickup");
        props = new SealToggle[]{
                new SealToggle(true, "pmeta", "golem.prop.meta"),
                new SealToggle(true, "pnbt", "golem.prop.nbt"),
                new SealToggle(false, "pore", "golem.prop.ore"),
                new SealToggle(false, "pmod", "golem.prop.mod")
        };
    }

    @Override
    public String getKey() {
        return "arcaneresearch:pickup";
    }

    @Override
    public void tickSeal(Level level, ISealEntity seal) {
        if (delay++ % 5 != 0) {
            return;
        }

        AABB area = GolemHelper.getBoundsForArea(seal);
        
        
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        if (!items.isEmpty()) {
            for (ItemEntity itemEntity : items) {
                if (itemEntity != null &&
                        itemEntity.onGround() &&
                        !itemEntity.hasPickUpDelay() &&
                        !itemEntity.getItem().isEmpty() &&
                        !itemEntities.containsValue(itemEntity.getId())) {

                    if (matchesFilter(itemEntity.getItem())) {
                        Task task = new Task(seal.getSealPos(), itemEntity);
                        task.setPriority(seal.getPriority());
                        itemEntities.put(task.getId(), itemEntity.getId());
                        TaskHandler.addTask(level.dimension(), task);
                        break;
                    }
                }
            }
        }

        if (delay % 100 != 0) {
            Iterator<Integer> it = itemEntities.values().iterator();
            while (it.hasNext()) {
                Entity e = level.getEntity(it.next());
                if (e == null || !e.isAlive()) {
                    try {
                        it.remove();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private boolean matchesFilter(ItemStack stack) {
        return arcaneresearch.common.lib.utils.InventoryUtils.matchesFilters(
                filter, isBlacklist(), stack, invFilter(props));
    }

    @Override
    public boolean onTaskCompletion(Level level, IGolemAPI golem, Task task) {
        ItemEntity itemEntity = getItemEntity(level, task);

        if (itemEntity != null && !itemEntity.getItem().isEmpty()) {
            if (matchesFilter(itemEntity.getItem())) {
                ItemStack remaining = golem.holdItem(itemEntity.getItem());

                if (!remaining.isEmpty() && remaining.getCount() > 0) {
                    itemEntity.setItem(remaining);
                } else {
                    itemEntity.discard();
                }

                ((Entity) golem).playSound(SoundEvents.ITEM_PICKUP, 0.125f,
                        ((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                golem.swingArm();
            }
        }

        task.setSuspended(true);
        itemEntities.remove(task.getId());

        ArrayList<Task> localTasks = TaskHandler.getEntityTasksSorted(level.dimension(), null, (Entity) golem);
        for (Task ticket : localTasks) {
            if (itemEntities.containsKey(ticket.getId()) &&
                    ticket.canGolemPerformTask(golem) &&
                    
                    
                    (!((EntityArcaneresearchGolem) golem).hasHome() ||
                            ((EntityArcaneresearchGolem) golem).isWithinHome(ticket.getEntity().blockPosition()))) {

                ((EntityArcaneresearchGolem) golem).setTask(ticket);
                ((EntityArcaneresearchGolem) golem).getTask().setReserved(true);
                if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                    level.broadcastEntityEvent((Entity) golem, (byte) 5);
                break;
            }
        }

        return true;
    }

    protected ItemEntity getItemEntity(Level level, Task task) {
        Integer entityId = itemEntities.get(task.getId());
        if (entityId != null) {
            Entity entity = level.getEntity(entityId);
            if (entity instanceof ItemEntity itemEntity) {
                return itemEntity;
            }
        }
        return null;
    }

    @Override
    public boolean canGolemPerformTask(IGolemAPI golem, Task task) {
        ItemEntity itemEntity = getItemEntity(golem.getGolemWorld(), task);
        if (itemEntity == null || itemEntity.getItem().isEmpty()) {
            return false;
        }
        if (!itemEntity.isAlive()) {
            task.setSuspended(true);
            return false;
        }
        return golem.canCarry(itemEntity.getItem(), true);
    }

    @Override
    public boolean canPlaceAt(Level level, BlockPos pos, Direction side) {
        return !level.isEmptyBlock(pos);
    }

    @Override
    public Identifier getSealIcon() {
        return icon;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{2, 1, 0, 4};
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
