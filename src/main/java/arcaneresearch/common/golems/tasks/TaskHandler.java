package arcaneresearch.common.golems.tasks;

import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.seals.SealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TaskHandler {

    private static final int TASK_LIMIT = 10000;
    private static final int TASK_EVICT_BATCH = TASK_LIMIT / 10;

    public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, Task>> tasks = new ConcurrentHashMap<>();

    private record ScoredTask(Task task, double score) {}

    private static String getDimKey(ResourceKey<Level> dim) {
        return dim.location().toString();
    }

    public static void addTask(ResourceKey<Level> dim, Task task) {
        if (task == null) {
            return;
        }
        ConcurrentHashMap<Integer, Task> dimTasks =
                tasks.computeIfAbsent(getDimKey(dim), k -> new ConcurrentHashMap<>());
        int size = dimTasks.size();
        if (size >= TASK_LIMIT) {
            int toEvict = Math.min(size - (TASK_LIMIT - TASK_EVICT_BATCH), TASK_EVICT_BATCH);
            Iterator<Task> iter = dimTasks.values().iterator();
            while (toEvict > 0 && iter.hasNext()) {
                iter.next();
                iter.remove();
                toEvict--;
            }
        }
        dimTasks.put(task.getId(), task);
    }

    public static Task getTask(ResourceKey<Level> dim, int id) {
        return getTasks(dim).get(id);
    }

    public static ConcurrentHashMap<Integer, Task> getTasks(ResourceKey<Level> dim) {
        return tasks.computeIfAbsent(getDimKey(dim), k -> new ConcurrentHashMap<>());
    }

    public static ArrayList<Task> getBlockTasksSorted(ResourceKey<Level> dim, UUID golemUUID, Entity golem) {
        return getTasksSortedInternal(dim, golemUUID, golem, 0);
    }

    public static ArrayList<Task> getEntityTasksSorted(ResourceKey<Level> dim, UUID golemUUID, Entity golem) {
        return getTasksSortedInternal(dim, golemUUID, golem, 1);
    }

    private static ArrayList<Task> getTasksSortedInternal(
            ResourceKey<Level> dim, UUID golemUUID, Entity golem, int type) {
        ConcurrentHashMap<Integer, Task> dimTasks = getTasks(dim);
        double gx = golem.getX();
        double gy = golem.getY();
        double gz = golem.getZ();
        boolean filterByGolem = golemUUID != null;

        List<ScoredTask> scored = new ArrayList<>();
        for (Task task : dimTasks.values()) {
            if (task.isReserved()) continue;
            if (task.getType() != type) continue;
            if (filterByGolem) {
                UUID tg = task.getGolemUUID();
                if (tg != null && !golemUUID.equals(tg)) continue;
            }
            if (type == 1) {
                Entity e = task.getEntity();
                if (e == null || !e.isAlive()) {
                    task.setSuspended(true);
                    continue;
                }
            }
            BlockPos pos = task.getPos();
            if (pos == null) continue;
            double score = pos.distToCenterSqr(gx, gy, gz) - task.getPriority() * 256.0;
            scored.add(new ScoredTask(task, score));
        }
        scored.sort(Comparator.comparingDouble(ScoredTask::score));

        ArrayList<Task> out = new ArrayList<>(scored.size());
        for (ScoredTask st : scored) {
            out.add(st.task());
        }
        return out;
    }

    public static void completeTask(Task task, EntityArcaneresearchGolem golem) {
        if (task == null || task.isCompleted() || task.isSuspended()) {
            return;
        }
        Level level = golem.level();
        if (level.isClientSide) {
            return;
        }
        ISealEntity sealEntity = SealHandler.getSealEntity(level.dimension(), task.getSealPos());
        if (sealEntity != null && sealEntity.getSeal() != null) {
            boolean completed = sealEntity.getSeal().onTaskCompletion(level, golem, task);
            task.setCompletion(completed);
        } else {
            task.setCompletion(true);
        }
    }

    public static void clearSuspendedOrExpiredTasks(Level level) {
        ResourceKey<Level> dim = level.dimension();
        ConcurrentHashMap<Integer, Task> dimTasks = getTasks(dim);

        List<Task> toSuspend = new ArrayList<>();
        List<Integer> toRemove = new ArrayList<>();

        for (Task task : dimTasks.values()) {
            if (!task.isSuspended() && task.getLifespan() > 0) {
                task.setLifespan((short) (task.getLifespan() - 1));
            } else {
                toRemove.add(task.getId());
                toSuspend.add(task);
            }
        }

        for (Task task : toSuspend) {
            ISealEntity sealEntity = SealHandler.getSealEntity(dim, task.getSealPos());
            if (sealEntity != null && sealEntity.getSeal() != null) {
                sealEntity.getSeal().onTaskSuspension(level, task);
            }
        }

        for (Integer id : toRemove) {
            dimTasks.remove(id);
        }
    }

    public static void clearDimension(ResourceKey<Level> dim) {
        tasks.remove(getDimKey(dim));
    }

    public static void removeTask(ResourceKey<Level> dim, int taskId) {
        ConcurrentHashMap<Integer, Task> dimTasks = tasks.get(getDimKey(dim));
        if (dimTasks != null) {
            dimTasks.remove(taskId);
        }
    }
}