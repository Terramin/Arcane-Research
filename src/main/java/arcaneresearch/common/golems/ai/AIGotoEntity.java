package arcaneresearch.common.golems.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.tasks.TaskHandler;

import java.util.List;

public class AIGotoEntity extends AIGoto {

    public AIGotoEntity(EntityArcaneresearchGolem golem) {
        super(golem, (byte) 1);
    }

    @Override
    public void tick() {
        super.tick();
        if (golem.getLookControl() != null && golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getLookControl().setLookAt(
                    golem.getTask().getEntity(),
                    10.0f,
                    (float) golem.getMaxHeadXRot()
            );
        }
    }

    @Override
    protected void moveTo() {
        if (golem.getNavigation() != null && golem.getTask() != null && golem.getTask().getEntity() != null) {
            golem.getNavigation().moveTo(golem.getTask().getEntity(), golem.getGolemMoveSpeed());
        }
    }

    @Override
    protected boolean findDestination() {
        List<Task> list = TaskHandler.getEntityTasksSorted(golem.level().dimension(), golem.getUUID(), golem);

        for (Task task : list) {
            if (task.getEntity() == null || !task.getEntity().isAlive()) {
                continue;
            }

            if (areGolemTagsValidForTask(task) &&
                    task.canGolemPerformTask(golem) &&
                    golem.isWithinHome(task.getEntity().blockPosition())    &&
                    isValidDestination(golem.level(), task.getEntity().blockPosition()) &&
                    canEasilyReach(task.getEntity())) {

                golem.setTask(task);
                golem.getTask().setReserved(true);

                minDist = 3.5 + task.getEntity().getBbWidth() / 2.0f * (task.getEntity().getBbWidth() / 2.0f);

                if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                    golem.level().broadcastEntityEvent(golem, (byte) 5);

                return true;
            }
        }
        return false;
    }

    private boolean canEasilyReach(Entity entity) {
        if (golem.distanceToSqr(entity) < minDist) {
            return true;
        }

        Path path = golem.getNavigation().createPath(entity, 1);
        if (path == null) {
            return false;
        }

        Node endNode = path.getEndNode();
        if (endNode == null) {
            return false;
        }

        int dx = endNode.x - Mth.floor(entity.getX());
        int dz = endNode.z - Mth.floor(entity.getZ());
        return dx * dx + dz * dz < minDist;
    }
}
