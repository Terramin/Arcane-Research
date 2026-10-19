package arcaneresearch.common.golems.tasks;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.seals.SealHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class TaskHandler {
    private static final int TASK_LIMIT = 10000;
    public static ConcurrentHashMap<String, ConcurrentHashMap<Integer, Task>> tasks = new ConcurrentHashMap<>();
    private static String getDimKey(ResourceKey<Level> dim) {
        return dim.location().toString();
    }
    public static void addTask(ResourceKey<Level> dim, Task task) {
        String dimKey = getDimKey(dim);
        tasks.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());
        ConcurrentHashMap<Integer, Task> dimTasks = tasks.get(dimKey);
        if (dimTasks.size() > TASK_LIMIT) {
            try {
                Iterator<Task> iter = dimTasks.values().iterator();
                if (iter.hasNext()) {
                    iter.next();
                    iter.remove();
                }
            } catch (Exception ignored) {
            }
        }
        dimTasks.put(task.getId(), task);
    }
    public static Task getTask(ResourceKey<Level> dim, int id) {
        return getTasks(dim).get(id);
    }
    public static ConcurrentHashMap<Integer, Task> getTasks(ResourceKey<Level> dim) {
        String dimKey = getDimKey(dim);
        return tasks.computeIfAbsent(dimKey, k -> new ConcurrentHashMap<>());
    }
    public static ArrayList<Task> getBlockTasksSorted(ResourceKey<Level> dim, UUID golemUUID, Entity golem) {
        ConcurrentHashMap<Integer, Task> dimTasks = getTasks(dim);
        ArrayList<Task> out = new ArrayList<>();
        taskLoop:
        for (Task task : dimTasks.values()) {
            if (task.isReserved()) continue;
            if (task.getType() != 0) continue;
            if (golemUUID != null && task.getGolemUUID() != null && !golemUUID.equals(task.getGolemUUID())) {
                continue;
            }
            if (out.isEmpty()) {
                out.add(task);
            } else {
                double d = task.getPos().distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                d -= task.getPriority() * 256;
                for (int i = 0; i < out.size(); i++) {
                    double d2 = out.get(i).getPos().distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                    d2 -= out.get(i).getPriority() * 256;
                    if (d < d2) {
                        out.add(i, task);
                        continue taskLoop;
                    }
                }
                out.add(task);
            }
        }
        return out;
    }
    public static ArrayList<Task> getEntityTasksSorted(ResourceKey<Level> dim, UUID golemUUID, Entity golem) {
        ConcurrentHashMap<Integer, Task> dimTasks = getTasks(dim);
        ArrayList<Task> out = new ArrayList<>();
        taskLoop:
        for (Task task : dimTasks.values()) {
            if (task.isReserved()) continue;
            if (task.getType() != 1) continue;
            if (golemUUID != null && task.getGolemUUID() != null && !golemUUID.equals(task.getGolemUUID())) {
                continue;
            }
            if (task.getEntity() == null || !task.getEntity().isAlive()) {
                task.setSuspended(true);
                continue;
            }
            if (out.isEmpty()) {
                out.add(task);
            } else {
                double d = task.getPos().distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                d -= task.getPriority() * 256;
                for (int i = 0; i < out.size(); i++) {
                    double d2 = out.get(i).getPos().distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                    d2 -= out.get(i).getPriority() * 256;
                    if (d < d2) {
                        out.add(i, task);
                        continue taskLoop;
                    }
                }
                out.add(task);
            }
        }
        return out;
    }
    public static void completeTask(Task task, EntityArcaneresearchGolem golem) {
        if (task.isCompleted() || task.isSuspended()) {
            return;
        }
        if (golem.level().isClientSide) {
            return;
        }
        ISealEntity sealEntity = SealHandler.getSealEntity(golem.level().dimension(), task.getSealPos());
        if (sealEntity != null) {
            boolean completed = sealEntity.getSeal().onTaskCompletion(golem.level(), golem, task);
            task.setCompletion(completed);
        } else {
            task.setCompletion(true);
        }
    }
    public static void clearSuspendedOrExpiredTasks(Level level) {
        ResourceKey<Level> dim = level.dimension();
        ConcurrentHashMap<Integer, Task> dimTasks = getTasks(dim);
        ConcurrentHashMap<Integer, Task> remaining = new ConcurrentHashMap<>();
        for (Task task : dimTasks.values()) {
            if (!task.isSuspended() && task.getLifespan() > 0) {
                task.setLifespan((short) (task.getLifespan() - 1));
                remaining.put(task.getId(), task);
            } else {
                ISealEntity sealEntity = SealHandler.getSealEntity(dim, task.getSealPos());
                if (sealEntity != null) {
                    sealEntity.getSeal().onTaskSuspension(level, task);
                }
            }
        }
        tasks.put(getDimKey(dim), remaining);
    }
    public static void clearDimension(ResourceKey<Level> dim) {
        tasks.remove(getDimKey(dim));
    }
    public static void removeTask(ResourceKey<Level> dim, int taskId) {
        getTasks(dim).remove(taskId);
    }
}
